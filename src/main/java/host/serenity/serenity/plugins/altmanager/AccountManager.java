package host.serenity.serenity.plugins.altmanager;

import host.serenity.serenity.plugins.altmanager.account.Account;
import host.serenity.serenity.plugins.altmanager.account.MigratedAccount;
import host.serenity.serenity.plugins.altmanager.account.UnmigratedAccount;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private List<Account> accounts = new ArrayList<>();

    public Account createAccount(String username, String password) {
        Account account;
        if (username.contains("@")) {
            account = new MigratedAccount(username, password);
        } else {
            account = new UnmigratedAccount(username, password);
        }

        return account;
    }

    public List<Account> getAccounts() {
        return accounts;
    }
}