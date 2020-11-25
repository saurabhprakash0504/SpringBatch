package com.sample;

import com.sample.exceptions.RecordException;
import com.sample.models.FlatFileDetails;
import com.sample.models.FlatFileHeader;
import com.sample.properties.FileLayout;
import com.sample.properties.FlatFileLayout;
import com.sample.utils.FileUtils;
import com.sample.utils.ValidationUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(Parameterized.class)
@SpringBootTest
public class FlatFileValidatorTest {

    @Spy
    ValidationUtils validationUtils;

    public static final SpringClassRule scr = new SpringClassRule();
    public static final long expectedSeqNo = 1L;
    private static final Collection<String> validFirstName =
            Arrays.asList(
                    "AAAABB",
                    "BBBCC",
                    "ASASASAS",
                    "ADUISIS"
            );
    private static final Collection<String> inValidFirstName =
            Arrays.asList(
                    "A111%",
                    "SER£$%^",
                    "A12344"
            );

    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();
    private final String validationField;
    private final FieldType fieldType;
    private final TestType testType;
    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Mock
    FileUtils fileUtils;

    @Mock
    private FlatFileLayout fileLayout;

    @Autowired
    private FlatFileLayout fileLayout2;

    @InjectMocks
    FlatFileValidateIncomingFile flatFileValidateIncomingFile;

    public FlatFileValidatorTest(FlatFileValidatorTest.FieldType fieldType, FlatFileValidatorTest.TestType testType, String validationField){
        this.validationField = validationField;
        this.fieldType = fieldType;
        this.testType = testType;
    }

    @Before
    public void setUpMocks() throws ConfigurationException {
        Mockito.when(fileUtils.getSequenceNumber("","inbound.sequence")).thenReturn(expectedSeqNo);
        Mockito.when(fileLayout.getDetailField()).thenReturn(fileLayout2.getDetailField());
        Mockito.when(fileLayout.getHeaderLength()).thenReturn(fileLayout2.getHeaderLength());
        Mockito.when(fileLayout.getDetailLength()).thenReturn(fileLayout2.getDetailLength());
        Mockito.when(fileLayout.getTrailerLength()).thenReturn(fileLayout2.getTrailerLength());
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        List<Object[]> validFirstNameList = validFirstName.stream().map(ele -> new Object[] {FieldType.FIRST_NAME, TestType.PASS, ele})
                .collect(Collectors.toList());

        List<Object[]> inValidFirstNameList = inValidFirstName.stream().map(ele -> new Object[] {FieldType.FIRST_NAME, TestType.FAIL, ele})
                .collect(Collectors.toList());

        return Stream.of(validFirstNameList,inValidFirstNameList).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Test
    public void processFirstName() throws Exception {
        Assume.assumeTrue(fieldType == FieldType.FIRST_NAME);

        Mockito.when(fileLayout.getHeaderLength()).thenReturn("16");
        Mockito.when(fileLayout.getDetailLength()).thenReturn("27");
        flatFileValidateIncomingFile.process(createCommonHeader());
        FlatFileDetails details = createCommonDetail();
        details.setFirstName(validationField);

        if(testType == TestType.PASS){
            FlatFileDetails detailsRecord = flatFileValidateIncomingFile.process(details);
            Assert.assertNotNull(detailsRecord);
            Assert.assertEquals(validationField, detailsRecord.getFirstName());
        }else {
            expectedException.expect(RecordException.class);
            expectedException.expectMessage("Invalid Record - validation failed on line [0]");
            flatFileValidateIncomingFile.process(details);
        }

    }

    FlatFileHeader createCommonHeader(){
        FlatFileHeader flatFileHeader = new FlatFileHeader();
        flatFileHeader.setOriginalRecord("0020200305000000");
        flatFileHeader.setLineNumber(1);
        flatFileHeader.setFileName("00");
        flatFileHeader.setDate("20200305");
        flatFileHeader.setRecordType("00");
        flatFileHeader.setSequenceNumber("000000");
        return flatFileHeader;
    }

    FlatFileDetails createCommonDetail(){
        FlatFileDetails flatFileDetails = new FlatFileDetails();
        flatFileDetails.setCity("Patna");
        flatFileDetails.setLastName("Praaksh");
        flatFileDetails.setPostCode("90");
        flatFileDetails.setRecordType("01");
        flatFileDetails.setOriginalRecord("01SAURABH  PRAKASH  21PATNA");
        return flatFileDetails;

    }


    enum FieldType{
        FIRST_NAME
    }

    enum TestType{
        PASS,
        FAIL
    }

}
