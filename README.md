# SpringBatch
mvn pmd:cpd-check -Dcpd.failOnViolation=true -Dpmd.verbose=true
mvn pmd:check -Dpmd.skipPmdError=false -Dpmd.failOnViolation=true -Dpmd.failurePriority=4 -Dpmd.verbose=true
mvn -Dcheckstyle.consoleOutput=true -Dcheckstyle.logViolationsToConsole=true -Dcheckstyle.violationSeverity=warning -Dcheckstyle.failOnViolation=true checkstyle:check
mvn spotbugs:check -Dspotbugs.failOnError=true
mvn -Pfindbugs findbugs:check -Dfindbugs.failOnError=true
