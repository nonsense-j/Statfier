import javax.crypto.Cipher;

public class TruePosTest1 {
    public static void main(String[] args) throws Exception {
        // Scenario: Direct string literal matching "IDEA.*"
        Cipher cipher = Cipher.getInstance("IDEA/CBC/PKCS5Padding");
    }
}