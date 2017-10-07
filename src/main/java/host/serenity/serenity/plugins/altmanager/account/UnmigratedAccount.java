package host.serenity.serenity.plugins.altmanager.account;

public class UnmigratedAccount implements Account {
    private final String username, password;

    public UnmigratedAccount(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getAuthName() {
        return username;
    }

    @Override
    public String getAuthPassword() {
        return password;
    }

    @Override
    public String getDisplayName() {
        return username;
    }
}
