import javax.crypto.Cipher;

public class TruePosTest3 {
    private static final String PREFIX = "IDEA";
    
    public static void main(String[] args) throws Exception {
        // Scenario: Constant expression matching "IDEA.*"
        Cipher cipher = Cipher.getInstance("IDEA/CFB/NoPadding");
    }
}