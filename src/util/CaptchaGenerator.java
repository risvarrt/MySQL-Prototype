package util;

import java.util.*;

public class CaptchaGenerator {
    public static String generateCaptcha() {
        Random rn = new Random();
        // Generate a CAPTCHA of length 5 to 7.
        int length = 5 + rn.nextInt(3);
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int base = rn.nextBoolean() ? 'A' : 'a';
            captcha.append((char) (base + rn.nextInt(26)));
        }
        return captcha.toString();
    }
}