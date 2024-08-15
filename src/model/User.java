package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String passwordHash;
    private String currentDatabase;
    private boolean dblock = false;
    private boolean dbcreationflag = false;

    public User(String userId, String password) {
        this.userId = userId;
        this.passwordHash = hashPassword(password);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserId() {
        return userId;
    }

    public boolean authenticate(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }
}