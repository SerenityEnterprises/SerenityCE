package host.serenity.serenity.files.accounts;

import host.serenity.serenity.api.file.ClientDataHandler;
import host.serenity.serenity.api.file.FileManager;
import host.serenity.serenity.plugins.altmanager.SerenityPluginAltManager;
import host.serenity.serenity.plugins.altmanager.account.Account;
import host.serenity.serenity.plugins.altmanager.account.MigratedAccount;

import java.io.*;

public class AccountDataHandler implements ClientDataHandler {
    private final File file = FileManager.createClientFile("accounts.txt");

    @Override
    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (Account account : SerenityPluginAltManager.getAccountManager().getAccounts()) {
            if (account.getDisplayName().equals(account.getAuthName())) {
                writer.write(account.getAuthName() + ":" + account.getAuthPassword());
            } else {
                writer.write(account.getDisplayName() + ":" + account.getAuthName() + ":" + account.getAuthPassword());
            }
            writer.newLine();
        }
        writer.close();
    }

    @Override
    public void load() throws IOException {
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String split[] = line.split(":");
                if (split.length == 2) {
                    SerenityPluginAltManager.getAccountManager().getAccounts().add(SerenityPluginAltManager.getAccountManager().createAccount(split[0], split[1]));
                } else if (split.length == 3) {
                    SerenityPluginAltManager.getAccountManager().getAccounts().add(new MigratedAccount(split[1], split[0], split[2]));
                }
            }

            reader.close();
        }
    }
}
