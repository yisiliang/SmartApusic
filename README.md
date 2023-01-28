# SmartApusic
<!-- Plugin description -->
## The Apusic plugin for Intellij IDEA.
Run/Debug Apusic Application Server with IDEA.
Apusic Application Server(AAS) is a Web Application Server developed by KingDee.

## User Guide
### 1. In Run/Debug, choose SmartApusic
### 2. Config the run/debug config</br>
* Apusic Server: config or choose Apusic server
* Domain : default domain is $APUSIC_HOME/domains/mydomain
* Deployment directory: the web application deployment directory, the plugin will find it automatically.
* Context path: the web application context path, it will be project name by default.
* Add libraries and classes: if the Deployment directory does not contain the dependencies libraries and classes, please select it.
* VM Options : input the vm options, e.g. "-server -Xmx1024m -Xms1024m"
* Env Options : extract apusic env parameters, e.g. "e.g. param1=value1"
### 3. Start run/debug
<!-- Plugin description end -->
