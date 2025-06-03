package Auth;

public class Authenticator {
    public void run() {
        //import scanner instance
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        // Print welcome message
        System.out.println("Welcome to the SYOS Billing System");

        //System option choice
        System.out.println("......................................................................................");
        System.out.println("Please select your Authentication method:");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                // Call login method
                Login login = new Login();
                login.login();
                break;
            case 2:
                // Call register method
                Register register = new Register();
                register.getUserDetails();
                break;
            case 3:
                // Exit the application
                System.out.println("Exiting the application. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
    }
}
