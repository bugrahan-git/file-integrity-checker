import java.util.Map;
import java.util.HashMap;

public class ichecker {
    private static String op;
    
    public static void main(String ... args) {
	Checker ch = new Checker();	
	Map<Character, String> argv = getArgs(args);
	
	if(op.equals("createCert"))
	    ch.createCert(argv.get('k'), argv.get('c'));
	else if(op.equals("createReg"))
	    ch.createReg(argv.get('r'), argv.get('p'), argv.get('l'), argv.get('h'), argv.get('k'));
	else if(op.equals("check"))
		ch.checkIntegrity(argv.get('r'), argv.get('p'), argv.get('l'), argv.get('h'), argv.get('c'));
	else
		System.out.println("Unrecognized parameter " + op);
    }


    private static Map<Character, String> getArgs(String ... args) {
	
	op = args[0];
	
	final Map<Character, String> argv= new HashMap<>();
	for(int i = 1; i < args.length; i+=2) {
	    String tmp = args[i];
	    if(tmp.charAt(0) == '-')
		argv.put(tmp.charAt(1), args[i + 1]);
	}

	return argv;
    }

}
