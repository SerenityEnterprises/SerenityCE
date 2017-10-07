package host.serenity.serenity.util.iface;

import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Map;
import java.util.UUID;

public interface NetHandlerPlayClientExtension {
    Map<UUID, NetworkPlayerInfo> getRealPlayerInfoMap();
}
