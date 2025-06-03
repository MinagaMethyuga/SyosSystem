package Dashboard;

public class CashierDashboard implements DashboardAccess {
    @Override
    public boolean viewDashboard() {
        System.out.println("......................................................................................");
        System.out.println("Welcome to the Cashier Billing System");
        return true;
    }
}
