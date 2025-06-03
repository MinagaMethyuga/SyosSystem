package Common;

public class ScannerInstance {
    private static final java.util.Scanner scanner = new java.util.Scanner(System.in);

    // Private constructor to prevent instantiation
    private ScannerInstance() {
    }

    // Method to get the singleton instance of Scanner
    public static java.util.Scanner getScanner() {
        return scanner;
    }
}
