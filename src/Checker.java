public class Checker {
    public Checker() {
    
    }
    
    public void createCert(String priKey, String pubCert) {
	String cmdKey = "-genkey " + 
			"-keyalg RSA " + 
			"-alias " + priKey +  
			" -keystore " + priKey + 	
			" -keysize 2048";
	
	String cmdCert = "-export " + 
			 "-keystore " + priKey +  
			 " -alias " + priKey +  	 
			 " -rfc " +
			 "-file " + pubCert; 
	
	try {
	    sun.security.tools.keytool.Main.main(cmdKey.split(" "));	
	    System.out.println("Private key created.");
	    sun.security.tools.keytool.Main.main(cmdCert.split(" "));
	    System.out.println("Self signed certificate created.");
	}catch(Exception e) {
	    e.printStackTrace();
	}
    }

}
