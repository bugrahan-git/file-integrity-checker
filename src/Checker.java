public class Checker {


    public Checker() {
	createCert("123", "123");
    
    }
    
    private void createCert(String priKey, String pubKey) {
	
	String cmd  =	"-genkey " + 
			"-keyalg RSA " + 
			"-alias selfsigned " + 
			"-keystore key.jks " + 
			"-keysize 2048 ";
	
	try {
	    sun.security.tools.keytool.Main.main(cmd);	
	}catch(Exception e) {
	    e.printStackTrace();
	}
    }

}
