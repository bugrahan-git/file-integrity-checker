import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.security.*;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.*;

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
    
    private String getTimestamp() {
	Timestamp ts = new Timestamp(System.currentTimeMillis());
	return sdf.format(ts);
    }

    private String calculateMD5(String fPath) {
	try {
	    byte[] b = Files.readAllBytes(Paths.get(fPath));
	    byte[] hash = MessageDigest.getInstance("MD5").digest(b);
	    return DatatypeConverter.printHexBinary(hash);
	}catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private String calculateSHA256(String fPath) {
	try {
	    byte[] b = Files.readAllBytes(Paths.get(fPath));
	    byte[] hash = MessageDigest.getInstance("SHA-256").digest(b);
	    return DatatypeConverter.printHexBinary(hash);
	}catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public void createReg(String regFile, String path, String logFile, String hashFunc, String priKey) {
	/*GET ALL FILES IN GIVEN DIR /INCLUDING SUBDIRS/*/
	getFiles(path);

	String output = "";
	/*TOTAL NUMBER OF FILES IN GIVEN PATH*/
	int count = 0; 
	
	for(String fPath : files) {
	    output = "[" + fPath + "] ";
	    
	    if(hashFunc.equals("MD5"))
		output += calculateMD5(fPath) + "\n";
	    else if(hashFunc.equals("SHA-256"))
		output += calculateSHA256(fPath) + "\n";
	    else{
		System.out.println("Unsupported hash function "+ hashFunc + ". Only MD5 or SHA-256");
		return;
	    }

	    /*ADD HASH VALUE TO REGISTRY*/
	    writeFile(regFile, output);
	    count++;
	    /*APPEND CURRENT OP TO LOG FILE*/
	    output = "[" + getTimestamp() + "] " + fPath +" is added to registry.\n";
	    writeFile(logFile, output);
	}
	
	String regHash;

	/*CALCULATE THE HASH VALUE OF REGISTRY FILE*/
	if(hashFunc.equals("MD5"))
	    regHash = calculateMD5(regFile);
	else
	    regHash = calculateSHA256(regFile);
	
	System.out.println("hash value of registry file: " + regHash);
	
	/*FINISH THE PROCESS*/
	output = "[" + getTimestamp() + "] " + count + " files are added to the registry and registry creation is finished!\n";
	writeFile(logFile, output);
	
    }
    
    /*WRITES GIVEN OUTPUT TO SPECIFIED FILE WITH TIMESTAMP*/
    private void writeFile(String file, String output){
	try {
            File outputF = new File(file);
            FileWriter writer = new FileWriter(outputF, true);
            writer.write(output);
            writer.close();
        } catch(Exception e){
	    e.printStackTrace();
	}
    }


}
