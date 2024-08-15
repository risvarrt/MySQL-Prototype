import controller.QueryEngine;
import model.User;
import service.AuthenticationService;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        AuthenticationService authService = new AuthenticationService();
        String filename = "C:\\Users\\risva\\IdeaProjects\\MYSQL_prototype\\src\\users.txt";

        // Load users from the file for authentication purposes
        authService.loadUsers(filename);

        System.out.println("Enter your username");
        String username = in.next();

        System.out.println("Enter your password");
        String password = in.next();

        // Attempt to authenticate the user with provided credentials
        if (authService.authenticate(username, password)) {
            User user = new User();
            boolean flag = true;
            user.setUserId(username);
            QueryEngine queryEngine = new QueryEngine();
            do {
                System.out.println("1. Write the query 2. Exit 3.Enter your choice :");
                System.out.print("mysql_by_rishi: sql>>");
                String ch = in.next();
                in.nextLine();
                switch (ch) {
                    case "1":
                        System.out.println("mysql_by_rishi: sql>>");
                        String query = in.nextLine();
                        // Split multiple queries separated by semicolon and whitespace
                        String[] queries = query.split(";\\s*");
                        // Process the queries for the current user session
                        queryEngine.processTransaction(queries, user);
                        break;
                    case "2":
                        System.out.println("mysql_by_rishi: sql>> Thank you for using me");
                        flag = false;
                        break;
                    default:
                        System.out.println("mysql_by_rishi: sql>>Please Enter a Valid Choice!");
                }
            } while (flag);
        }
    }

}

