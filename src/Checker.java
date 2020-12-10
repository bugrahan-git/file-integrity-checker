import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.security.*;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.*;


public class Checker {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
    private Cipher AES_CBC;
    private IvParameterSpec IvSpecAES;
    private Key keyAES;

	public Checker() {
		try {
			SecureRandom secRandom = new SecureRandom();
			byte[] IvAES = new byte[16];
			secRandom.nextBytes(IvAES);
			this.AES_CBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
			this.IvSpecAES = new IvParameterSpec(IvAES);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void createCert(String priKey, String pubCert){
		Base64.Encoder encoder = Base64.getEncoder();
		System.out.println("***ICHECKER***");
		String password = null;
		java.io.Console console = System.console();
		try {
			password = new String(console.readPassword("Please enter a password: "));
			byte[] pass_md5 = MessageDigest.getInstance("MD5").digest(password.getBytes());
			pass_md5 = Arrays.copyOf(pass_md5, 16);
			this.keyAES = new SecretKeySpec(pass_md5, "AES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		String cmdKey = "-genkeypair " +
			"-keyalg RSA " + 
			"-alias " + priKey +  
			" -keystore " + priKey + 	
			" -keysize 2048" +
			" -storetype pkcs12"+
			" -keypass "+password+
			" -dname CN=admin"+
			" -storepass "+ password;

		String cmdCert = "-export " +
			 "-keystore " + priKey +  
			 " -alias " + priKey +  	 
			 " -rfc " +
			 "-file " + pubCert+
			 " -storepass "+password+
			 " -srcstoretype pkcs12";
	try {
	    sun.security.tools.keytool.Main.main(cmdKey.split(" "));	
	    System.out.println("Private key created.");

	    sun.security.tools.keytool.Main.main(cmdCert.split(" "));
	    System.out.println("Self signed certificate created.");

		KeyStore keyStore;
		keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new FileInputStream(priKey), password.toCharArray());
		PrivateKey privateKey =
				(PrivateKey) keyStore.getKey(priKey, password.toCharArray());
		writeFile(priKey+".enc", encryptor(keyAES, new String(privateKey.getEncoded())+
				"KORKMAZLAR BILISIM", true), false);

	}catch(Exception e) {
	    e.printStackTrace();
	}
    }

    /*Store all files in an arraylist*/
    private List<String> files = new ArrayList<>();
    
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

    private String encryptor(Key key, String priKey, boolean isEnc)
	{
		try {
			if(isEnc){
			AES_CBC.init(1, key, IvSpecAES);
			byte[] bytes_str = AES_CBC.doFinal(priKey.getBytes());
			byte[] bytes_priKey = Base64.getEncoder().encode(bytes_str);
			return new String(bytes_priKey);
			}

			AES_CBC.init(2, key, IvSpecAES);
			byte[] decoded = Base64.getDecoder().decode(priKey);
			byte[] decrypted_priKey = AES_CBC.doFinal(decoded);
			return new String(decrypted_priKey);
		}catch (BadPaddingException ex)
		{
			System.out.println("Wrong Password.");
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    public void createReg(String regFile, String path, String logFile, String hashFunc, String priKey) {
	/*GET ALL FILES IN GIVEN DIR /INCLUDING SUBDIRECTORIES/*/
	getFiles(path);

	byte[] digitalSignature;
	Signature signature = null;
	System.out.println("***ICHECKER***");
	String password;
	java.io.Console console = System.console();
	KeyStore keyStore;
	try {
		password = new String(console.readPassword("Please enter password: "));
		byte[] pass_md5 = MessageDigest.getInstance("MD5").digest(password.getBytes());
		pass_md5 = Arrays.copyOf(pass_md5, 16);
		Key trial_key = new SecretKeySpec(pass_md5, "AES");
		String keyfile = readFile(priKey+".enc");
		assert keyfile != null;
		boolean enc = (Objects.requireNonNull(encryptor(trial_key, keyfile, false)))
				.endsWith("KORKMAZLAR BILISIM");
		if(!enc)
		{
			System.out.println("Wrong Password");
			return;
		}
		else
		{
			String decrypted = encryptor(trial_key, keyfile, false);
			assert decrypted != null;
			writeFile(priKey+".enc", decrypted, false);
		}
		keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(new FileInputStream(priKey), password.toCharArray());
		PrivateKey privateKey =
				(PrivateKey) keyStore.getKey(priKey, password.toCharArray());

		if (hashFunc.equals("MD5"))
			signature = Signature.getInstance("MD5withRSA");
		else
			signature = Signature.getInstance("SHA256withRSA");

		signature.initSign(privateKey);
		writeFile(priKey+".enc", encryptor(trial_key, readFile(priKey+".enc"), true), false);
	}
	catch (Exception e) {
		e.printStackTrace();
	}

	String output;
	/*TOTAL NUMBER OF FILES IN GIVEN PATH*/
	int count = 0; 
	
	for(String fPath : files) {
	    output =  fPath + " ";
	    
	    if(hashFunc.equals("MD5"))
			output += calculateMD5(fPath) + "\n";
	    else if(hashFunc.equals("SHA-256"))
	    	output += calculateSHA256(fPath) + "\n";
	    else{
			System.out.println("Unsupported hash function "+ hashFunc + ". Only MD5 or SHA-256");
			return;
	    }

	    /*ADD HASH VALUE TO REGISTRY*/
	    writeFile(regFile, output, true);
	    count++;
	    /*APPEND CURRENT OP TO LOG FILE*/
	    output = getTimestamp() + ": " + fPath +" is added to registry.\n";
	    writeFile(logFile, output, true);
	}
	
	String regHash;

	/*CALCULATE THE HASH VALUE OF REGISTRY FILE*/
	if(hashFunc.equals("MD5"))
	    regHash = calculateMD5(regFile);
	else
	    regHash = calculateSHA256(regFile);

	/*FINISH THE PROCESS*/
	output = getTimestamp() + ": " + count
			+ " files are added to the registry and registry creation is finished!\n";
	writeFile(logFile, output, true);

	output = readFile(regFile);
	try{
		assert signature != null;
		assert output != null;
		assert regHash != null;
		signature.update(regHash.getBytes());
		digitalSignature = signature.sign();
		output = new String(Base64.getEncoder().encode(digitalSignature));
		writeFile(regFile, output, true);
	}catch (Exception e){e.printStackTrace();}
	
    }
    
    /*WRITES GIVEN OUTPUT TO SPECIFIED FILE WITH TIMESTAMP*/
    private void writeFile(String file, String output, boolean append){
	try {
            File outputF = new File(file);
            FileWriter writer = new FileWriter(outputF, append);
            writer.write(output);
            writer.close();
        } catch(Exception e){
	    e.printStackTrace();
	}
    }

	public static String readFile(String file) {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}

			br.close();
			return sb.toString();

		} catch (Exception e) {
			System.out.print("Error while reading " + file + "\n");
		}

		return null;
	}

	public static String regReader(String file) {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				sb.append(line);sb.append("\n");
				line = br.readLine();
			}

			br.close();
			return sb.toString();

		} catch (Exception e) {
			System.out.print("Error while reading " + file + "\n");
		}
		return null;
	}

	public void checkIntegrity(String regFile, String path, String logFile, String hash, String pubKey) {
		/*CHECK REGISTRY FILE USING pubKey*/
		FileInputStream fin = null;
		boolean isVerified = false;
		Signature signature;
		try {
			fin = new FileInputStream(pubKey);
			CertificateFactory f = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate)f.generateCertificate(fin);
			PublicKey publicKey = certificate.getPublicKey();
			if(hash.equals("MD5"))
				signature = Signature.getInstance("MD5withRSA");
			else
				signature = Signature.getInstance("SHA256withRSA");

			signature.initVerify(publicKey);
			String[] lines = Objects.requireNonNull(regReader(regFile)).split("\n");
			StringBuilder reg_file = new StringBuilder();
			for(int i=0; i<lines.length-1; i++)
				reg_file.append(lines[i]).append("\n");
			String signature_line = lines[lines.length-1];
			byte[] reg_bytes;
			if(hash.equals("MD5")){
				reg_bytes = MessageDigest.getInstance("MD5").digest(reg_file.toString().getBytes());
			}
			else{
				reg_bytes = MessageDigest.getInstance("SHA-256").digest(reg_file.toString().getBytes());
			}
			reg_bytes = DatatypeConverter.printHexBinary(reg_bytes).getBytes();
			signature.update(reg_bytes);
			byte[] signal = Base64.getDecoder().decode(signature_line);
			isVerified = signature.verify(signal);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String output = "";

		if(!isVerified) {
			output = getTimestamp() + ": " + "Registry file verification is failed!" + "\n";
			writeFile(logFile, output, true);
			return;
		}

		/* CHECK ALL FILE'S HASH AGAIN
		 * THEN COMPARE WITH THE REGISTRY FILE IF IT CHANGED */

		files.clear();
		getFiles(path);

		Map<String, String> original = new HashMap<>();
		Map<String, String> toCheck = new HashMap<>();

		String[] regTokens = regReader(regFile).split("\n| ");

		System.out.println(Arrays.toString(regTokens));
		/*POPULATE THE ORIGINAL HASHMAP USING THE REGISTER FILE'S CONTENT*/
		for(int i = 0; i < regTokens.length - 1; i+=2)
			original.put(regTokens[i], regTokens[i+1]);

		for(String fPath : files) {
			if(hash.equals("MD5"))
				toCheck.put(fPath, calculateMD5(fPath));
			else if(hash.equals("SHA-256"))
				toCheck.put(fPath, calculateSHA256(fPath));
		}

		boolean isChanged = false;

		/*CHECK toCheck HASHMAP's KEYSET AND COMPARE WITH ORIGINAL HASHMAP*/
		for(String fPath : toCheck.keySet()) {
			String tmpCheck = toCheck.get(fPath);
			String tmpOrig = original.get(fPath);

			if(tmpOrig == null) {
				output = getTimestamp() + ": " + fPath + " is " + "created" + "\n";
				isChanged = true;
			}else if(!tmpOrig.equals(tmpCheck)){
				output = getTimestamp() + ": " + fPath + " is " + "altered" + "\n";
				isChanged = true;
			}

			if(isChanged)
				writeFile(logFile, output, true);
		}

		/*CHECK ORIGINAL HASHMAP'S KEYSET AND COMPARE WITH OTHER HASHMAP*/
		for(String fPath : original.keySet()) {
			String tmpCheck = toCheck.get(fPath);

			if(tmpCheck == null) {
				output = getTimestamp() + ": " + fPath + " is " + "deleted" + "\n";
				isChanged = true;
			}

			if(isChanged)
				writeFile(logFile, output, true);
		}

		/*NOTHING IS CHANGED*/
		if(!isChanged) {
			output = getTimestamp() + ": " + "The directory is checked and no change is detected!" + "\n";
			writeFile(logFile, output, true);
		}
	}
}
