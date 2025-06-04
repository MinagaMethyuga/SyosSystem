import Auth.Authenticator;
import Dashboard.ManagerDashboard;

public class Main {
    public static void main(String[] args) {
//        Authenticator authenticator = new Authenticator();
//        authenticator.run();

        ManagerDashboard realDashboard = new ManagerDashboard();
        realDashboard.viewDashboard();
    }
}