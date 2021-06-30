## Installation

```bash
mvn clean install
```

## Usage

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.vmware.devops \
  -DarchetypeArtifactId=vrealize-automation-content-generator-project \
  -DarchetypeVersion=1.0-SNAPSHOT \
  -DgroupId=<my-groupid> \
  -DartifactId=<my-artifactId> \
  -Dinstance=<vra-instance> \
  -DloginInstance=<vra-loginInstance> \
  -Dusername=<vra-username> \
  -Dpassword=<vra-password> \
  -DrefreshToken=<vra-refreshToken> \
  -Dinitialize=true
```

Available properties
* **instance** - vRA instance
* **loginInstance** - vRA login instance
* **username** - vRA username for login
* **password** - vRA password for login
* **refreshToken** - refreshToken for login
* **initialize** - if set to true it will reverse generate the vRA entities in the new project. Default: false
