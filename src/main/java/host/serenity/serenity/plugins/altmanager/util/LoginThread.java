package host.serenity.serenity.plugins.altmanager.util;

import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import host.serenity.serenity.plugins.altmanager.account.Account;
import host.serenity.serenity.plugins.altmanager.account.MigratedAccount;
import host.serenity.serenity.util.iface.MinecraftExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import java.net.Proxy;

public class LoginThread extends Thread {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final Account account;

    public String status;

    public LoginThread(Account account) {
        super("Login Thread");
        this.account = account;
    }

    private final Session createSession(String username, String password) {
        if (password.isEmpty()) {
            return new Session(username, mc.getSession().getPlayerID(),
                    "topkek memes", "mojang");
        }
        final YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(
                Proxy.NO_PROXY, "");
        final YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) service
                .createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();
            return new Session(auth.getSelectedProfile().getName(), UUIDTypeAdapter.fromUUID(auth
                    .getSelectedProfile().getId()),
                    auth.getAuthenticatedToken(), "mojang");
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public void run() {
        status = "Logging in...";

        final Session auth = createSession(account.getAuthName(), account.getAuthPassword());
        if (auth == null) {
            status = EnumChatFormatting.RED + "Failed.";
        } else {
            status = String.format(EnumChatFormatting.GREEN + "Success. (Logged in as %s.)", auth.getUsername());

            if (account instanceof MigratedAccount) {
                ((MigratedAccount) account).setDisplay(auth.getUsername());
            }

            ((MinecraftExtension) mc).setSession(auth);
        }
    }
}