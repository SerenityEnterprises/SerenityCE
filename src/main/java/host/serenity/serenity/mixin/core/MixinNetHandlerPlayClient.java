package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.network.PostSendPacket;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.util.iface.NetHandlerPlayClientExtension;
import host.serenity.synapse.EventManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements NetHandlerPlayClientExtension {
    @Shadow @Final
    private NetworkManager netManager;

    @Shadow @Final
    private Map playerInfoMap;

    /**
     * @author serenity.host
     */
    @Overwrite
    public void addToSendQueue(Packet packet) {
        SendPacket event = new SendPacket(packet);
        if (!EventManager.post(event).isCancelled()) {
            this.netManager.sendPacket(event.getPacket());
        }

        EventManager.post(new PostSendPacket(packet));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<UUID, NetworkPlayerInfo> getRealPlayerInfoMap() {
        return (Map<UUID, NetworkPlayerInfo>) playerInfoMap;
    }
}
