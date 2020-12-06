import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;
import java.lang.*;

public class Checker {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
    
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
    /*Store all files in an arraylist*/
    private List<String> files = new ArrayList<String>();
    
    private void getFiles(String path) {
	File dir = new File(path);
	File[] fList = dir.listFiles();
	if(fList != null) {
	    for(File f : fList) {
		if(f.isFile())
		    files.add(f.getAbsolutePath());
		else if(f.isDirectory())
		    getFiles(f.getAbsolutePath());
	    }
	}
    }
 
    public void createReg(String regFile, String path, String logFile, 
			    String hashFunc, String priKey) {
	
	getFiles(path);
	for(String f : files) 
	    System.out.println(f);


    }
    
    /*WRITES GIVEN OUTPUT TO SPECIFIED FILE WITH TIMESTAMP*/
    private void writeFile(String file, String output){
	Timestamp ts = new Timestamp(System.currentTimeMillis());
	output = sdf.format(ts) + " " + output;
	try {
            File outputF = new File(file);
            FileWriter writer = new FileWriter(outputF);
            writer.write(output);
            writer.close();
        } catch(Exception e){
            System.out.print("Error while writing to " + file + "\n");
        }
    }


}
