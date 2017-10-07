package host.serenity.serenity.util.iface;

import net.minecraft.util.Session;
import net.minecraft.util.Timer;

public interface MinecraftExtension {
    Timer getTimer();
    void setSession(Session session);

    int getRightClickDelayTimer();
    void setRightClickDelayTimer(int rightClickDelayTimer);
}
