package host.serenity.serenity.modules.tweaks;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.SendChat;
import host.serenity.synapse.Listener;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Map;

public class DashNames extends Module {
    public DashNames() {
        super("Dash Names", 0x85FF8F, ModuleCategory.TWEAKS);

        listeners.add(new Listener<SendChat>() {
            @Override
            public void call(SendChat event) {
                String changed = event.getMessage();
                for (Object o : mc.getNetHandler().getPlayerInfoMap()) {
                    NetworkPlayerInfo info = (NetworkPlayerInfo) o;
                    String name = info.getGameProfile().getName();
                    for (Map.Entry<String, String> entry : Serenity.getInstance().getFriendManager().getFriends().entrySet()) {
                        if (entry.getKey().equals(entry.getValue()))
                            continue;

                        if (name.equalsIgnoreCase(entry.getKey())) {
                            changed = changed.replaceAll("(?i)-" + entry.getValue(), name);
                        }
                    }
                }

                event.setMessage(changed);

            }
        });

        setHidden(true);
        setState(true);
    }
}
