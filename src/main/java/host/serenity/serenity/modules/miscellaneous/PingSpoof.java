package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C00PacketKeepAlive;

public class PingSpoof extends Module {
    public PingSpoof() {
        super("Ping Spoof", 0x95FFFC, ModuleCategory.MISCELLANEOUS);
        setHidden(true);

        listeners.add(new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (event.getPacket() instanceof C00PacketKeepAlive)
                    event.setCancelled(true);
            }
        });
    }
}
