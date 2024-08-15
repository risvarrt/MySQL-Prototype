package service;

import model.User;
import util.CaptchaGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AuthenticationService {
    private Map<String, User> users = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);

    public void loadUsers(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // username and password are separated by a comma in the users.txt file
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    users.put(username, new User(username, password));
                }
            }
            System.out.println(users);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean authenticate(String userId, String password) {
        if (!users.containsKey(userId)) {
            System.out.println("src.main.java.User ID not found.");
            return false;
        }
        User user = users.get(userId);
        CaptchaGenerator captchaGenerator = new CaptchaGenerator();
        String captcha = captchaGenerator.generateCaptcha();
        System.out.println("CAPTCHA: " + captcha);
        System.out.print("Enter the CAPTCHA: ");
        String inputCaptcha = scanner.nextLine();
        if (!captcha.equalsIgnoreCase(inputCaptcha)) {
            System.out.println("Incorrect CAPTCHA.");
            return false;
        }
        if (user.authenticate(password)) {
            System.out.println("Authentication successful.");
            return true;
        } else {
            System.out.println("Incorrect password.");
            return false;
        }
    }

}