import Auth.Authenticator;
import Dashboard.CashierDashboard;
import Dashboard.ManagerDashboard;

public class Main {
    public static void main(String[] args) {
//        Authenticator authenticator = new Authenticator();
//        authenticator.run();

        CashierDashboard realDashboard = new CashierDashboard();
        realDashboard.viewDashboard();

//        ManagerDashboard realDashboard = new ManagerDashboard();
//        realDashboard.viewDashboard();
    }
}