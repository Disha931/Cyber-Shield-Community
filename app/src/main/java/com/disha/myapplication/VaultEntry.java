package com.disha.myapplication;

public class VaultEntry {
    public String siteName;
    public String username;
    public String password;

    public VaultEntry(String siteName, String username, String password) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
    }
}