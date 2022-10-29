#!/bin/bash


repoPath=${1}
cd ${repoPath}

projectName=${2}
version=${3}

/Users/keyon/Downloads/sonar-scanner-4.7.0.2747-macosx/bin/sonar-scanner -Dsonar.projectKey=${projectName}  -Dsonar.sources=${repoPath} -Dsonar.projectVersion=${version} -Dsonar.java.binaries=${repoPath} -Dsonar.language=java  > /Users/keyon/Downloads/sonar-scanner-4.7.0.2747-macosx/log/sonar-${projectName}.log

cat /Users/keyon/Downloads/sonar-scanner-4.7.0.2747-macosx/log/sonar-${projectName}.log >> /Users/keyon/Downloads/sonar-scanner-4.7.0.2747-macosx/log/sonar.log
result=`cat /Users/keyon/Downloads/sonar-scanner-4.7.0.2747-macosx/log/sonar-${projectName}.log | grep -E "EXECUTION SUCCESS"`

if [[ "$result" == "" ]]
then
#       echo "failed" >> /home/fdse/user/issueTracker/bin/log/sonarScanner.log
       exit 1
fi

 #echo "success" >> /home/fdse/user/issueTracker/bin/log/sonarScanner.log
exit 0

