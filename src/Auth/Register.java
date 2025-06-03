package Auth;

import Common.ScannerInstance;
import Users.User;
import Users.UserFactory;

import java.util.Scanner;

public class Register {
    public void getUserDetails(){
        // Import scanner instance
        Scanner scanner = ScannerInstance.getScanner();

        System.out.println(".......................................................................................");
        System.out.println("Welcome! Please register to create an account.");

        // Username input
        String username;
        while (true) {
            System.out.print("Enter your username: ");
            username = scanner.nextLine().trim();
            if (!username.isEmpty()) {
                break;
            }
            System.out.println("Username cannot be empty. Please try again.");
        }
        while (true) {
            // Password input
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            // Check if password is empty
            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Please try again.");
                continue;
            }

            // Confirm password input
            System.out.print("Confirm your password: ");
            String confirmPassword = scanner.nextLine().trim();

            // Check if the confirmation password is empty
            if (confirmPassword.isEmpty()) {
                System.out.println("Confirm password cannot be empty. Please try again.");
                continue;
            }

            // Validate password and confirm password match
            if (!password.equals(confirmPassword)) {
                System.out.println("Passwords do not match. Please try again.");
            }else {
                Register register = new Register();
                register.registerUserRole(username, password);
                break;
            }
        }
    }

    public void registerUserRole(String username, String password) {
        Scanner scanner = ScannerInstance.getScanner();

        // Role selection prompt
        System.out.println(".......................................................................................");

        int roleChoice = -1;

        while (true) {
            System.out.println("Select your role:");
            System.out.println("1. Manager");
            System.out.println("2. Cashier");

            System.out.print("Enter your choice (1 or 2): ");
            String input = scanner.nextLine().trim(); // Read the input as a String

            // Check if input is empty
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter 1 for Manager or 2 for Cashier.");
                continue;
            }

            // Validate if the input is numeric
            try {
                roleChoice = Integer.parseInt(input); // Attempt to parse the input to an integer
                if (roleChoice == 1 || roleChoice == 2) {
                    break; // Valid choice, exit the loop
                } else {
                    System.out.println("Invalid choice. Please enter 1 for Manager or 2 for Cashier.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1 or 2).");
            }
        }

        // Process the selected role
        processRole(roleChoice , username, password);
    }

    private int getUserRoleChoice(Scanner scanner) {
        while (true) {
            System.out.print("Enter your choice (1 or 2): ");
            int roleChoice = scanner.nextInt();

            if (roleChoice == 1 || roleChoice == 2) {
                return roleChoice;
            } else {
                System.out.println("Invalid choice. Please try again. Enter 1 for Manager or 2 for Cashier.");
            }
        }
    }

    private void processRole(int roleChoice, String username, String password) {
        User user = UserFactory.createUser(username, password, roleChoice);
        UserService.saveUser(user);
    }
}
