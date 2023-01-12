# SmartApusic (clone from SmartApusic)
<!-- Plugin description -->
The Apusic plugin for Intellij IDEA

<!-- Plugin description end -->


### User Guide
* Apusic Server Setting

        Navigate File -> Setting or Ctrl + Alt + S  Open System Settings.
        In the Setting UI, go to Apusic Server, and then add your apusic servers
 
* Run/Debug setup
        
        Navigat Run -> Edit Configrations to Open Run/Debug Configrations. 
        In the Run/Debug Configrations, add new configration, choose Smart Apusic, 
        for detail config as below
        
  
* Run/Debug config detail
    * Apusic Server
        
            choose the apusic server.
         
    * Domain
    
            default domain is $APUSIC_HOME/domains/mydomain

    * VM Options

          extract apusic VM options
          e.g. -Duser.language=en
    
    * Env Options
        
            extract apusic env parmaters
            e.g. param1=value1
