package Auth;

import Common.ScannerInstance;
import Dashboard.Proxy;

import java.util.Scanner;

public class Login {
    public void login() {
        //import scanner instance
        Scanner scanner = ScannerInstance.getScanner();

        System.out.println(".......................................................................................");
        System.out.println("Welcome Back Enter Your Credentials");

        while (true) {
            //username input
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            //password input
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            //pass the username and password to the proxy class
            Proxy proxy = new Proxy(username, password);
            boolean isSuccess = proxy.viewDashboard();

            if (isSuccess) {
                break;
            }
        }
    }
}
