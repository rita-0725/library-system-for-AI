import org.mindrot.jbcrypt.BCrypt;

public class GenerateHash {
    public static void main(String[] args) {
        String password = " password\;
 String hash = BCrypt.hashpw(password, BCrypt.gensalt());
 System.out.println(hash);
 }
}
