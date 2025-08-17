import javax.crypto.Cipher;

public class TruePosTest2 {
    private static final String ALGORITHM = "IDEA/ECB/NoPadding";
    
    public static void main(String[] args) throws Exception {
        // Scenario: Final static constant matching "IDEA.*"
        Cipher cipher = Cipher.getInstance(ALGORITHM);
    }
}