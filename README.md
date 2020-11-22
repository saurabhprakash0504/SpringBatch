# SpringBatch
mvn pmd:cpd-check -Dcpd.failOnViolation=true -Dpmd.verbose=true
mvn pmd:check -Dpmd.skipPmdError=false -Dpmd.failOnViolation=true -Dpmd.failurePriority=4 -Dpmd.verbose=true
mvn -Dcheckstyle.consoleOutput=true -Dcheckstyle.logViolationsToConsole=true -Dcheckstyle.violationSeverity=warning -Dcheckstyle.failOnViolation=true checkstyle:check
mvn spotbugs:check -Dspotbugs.failOnError=true
mvn -Pfindbugs findbugs:check -Dfindbugs.failOnError=true

---------------------------------------------------------------------------------------------------------------------

git rebase -i origin/develop (branch from where you want to rebase)
 
(if there is any merge conflicts, then resolve the merge conflicts and add)
 
git add src/main/java/uk/gov/dwp/ntcg/processors/ff3/FileFlow3ValidateIncomingFile.java
( no need to commit the changes, just push the changes)

git rebase --continue
git status -s
git push --force

git stash save --keep-index --include-untracked
