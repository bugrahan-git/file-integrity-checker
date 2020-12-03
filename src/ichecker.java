import java.util.Map;
import java.util.HashMap;

public class ichecker {
    private static String op;
    
    public static void main(String ... args) {
	
	ichecker ic = new ichecker(); 
	
	Map<Character, String> argv = ic.getArgs(args);
	
	System.out.printf("Operation: %s\n", op);
	for(Character c : argv.keySet()) {
	    String key = c.toString();
	    String value = argv.get(c);
	    System.out.println(key + ": " + value);
	}
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
