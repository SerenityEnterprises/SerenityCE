package host.serenity.serenity.plugins.altmanager.account;

public class MigratedAccount implements Account {
    private final String email, password;
    private String displayname;

    public MigratedAccount(String email, String password) {
        this.email = this.displayname = email;
        this.password = password;
    }

    public MigratedAccount(String email, String displayname, String password) {
        this.email = email;
        this.displayname = displayname;
        this.password = password;
    }

    @Override
    public String getAuthName() {
        return email;
    }

    @Override
    public String getAuthPassword() {
        return password;
    }

    @Override
    public String getDisplayName() {
        return displayname;
    }

    public void setDisplay(String display) {
        this.displayname = display;
    }
}
