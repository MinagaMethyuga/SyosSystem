package Billing;

import Common.ScannerInstance;

import java.util.Scanner;

public class MakeBill extends BillingProcessTemplate {

    @Override
    protected String validateItemCode() {
        // Get the scanner instance from ScannerInstance
        Scanner sc = ScannerInstance.getScanner();
        System.out.print("Enter Item Code: ");
        String itemCode = sc.nextLine().trim();

        return itemCode;
    }

    @Override
    protected void QueryItemInfo(String itemCode) {

    }
}
