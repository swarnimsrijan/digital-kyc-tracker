@Library("ci@master") _

MavenDockerPublish(
   'enableCodeSonarSastScan': true,
   'enableCodeSonarSastGating': false,
   'tenant': 'omega',
   'jdkVersion': '17',
   'codeSonarScanCommand': "mvn sonar:sonar -Dsonar.projectKey=zea-opc-m02-lu16-ganymede -Dsonar.projectName=\"zea-opc-m02-lu16-ganymede\" -Dsonar.branch.name=${params.gitBranch ?: 'master'}",
   'mavenBuildCommand': 'mvn clean package -DskipTests',
   'mavenDeployCommand': 'mvn clean deploy -DskipTests',
   'disableImagePublish': false,
   'publishHelmChart': true,
)