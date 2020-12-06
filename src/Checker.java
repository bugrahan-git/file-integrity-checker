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
	    System.out.println("keypair is creating...");
	    sun.security.tools.keytool.Main.main(cmd.split(" "));	
	    System.out.println("Keypair created.");
	}catch(Exception e) {
	    e.printStackTrace();
	}
    }

}
