package com.example.android.distributeurdeau;

public class Utils {
    public static final String farmer = "farmer";
    public static final String supervisor = "supervisor";

    public static boolean authenticated = false;


    public static boolean isAuthenticated() {
        // TODO
        return authenticated;
    }

    public static String getAccountType() {
        return farmer;
    }
}
