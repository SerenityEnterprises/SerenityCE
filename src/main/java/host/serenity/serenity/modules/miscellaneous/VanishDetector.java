package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.core.RunTick;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.iface.NetHandlerPlayClientExtension;
import host.serenity.synapse.Listener;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.io.IOException;
import java.util.*;

public class VanishDetector extends Module {
    private Set<UUID> vanishedPlayers = new HashSet<>();
    private final Map<UUID, String> uuidNameCache = new HashMap<>();

    public VanishDetector() {
        super("Vanish Detector", 0xB5FFEE, ModuleCategory.MISCELLANEOUS);

        listeners.add(new Listener<RunTick>() {
            @Override
            public void call(RunTick event) {
                if (mc.getNetHandler() != null) {
                    Map<UUID, NetworkPlayerInfo> playerInfoMap = ((NetHandlerPlayClientExtension) mc.getNetHandler()).getRealPlayerInfoMap();

                    for (NetworkPlayerInfo playerInfo : playerInfoMap.values()) {
                        if (playerInfo.getGameProfile().getName() != null) {
                            uuidNameCache.put(playerInfo.getGameProfile().getId(), playerInfo.getGameProfile().getName());
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S38PacketPlayerListItem) {
                    S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();
                    if (packet.func_179768_b() == S38PacketPlayerListItem.Action.UPDATE_LATENCY) {
                        for (final S38PacketPlayerListItem.AddPlayerData playerData : (List<S38PacketPlayerListItem.AddPlayerData>) packet.func_179767_a()) {
                            if (mc.getNetHandler().getPlayerInfo(playerData.getProfile().getId()) == null) {
                                if (!vanishedPlayers.contains(playerData.getProfile().getId())) {
                                    new Thread(() -> {
                                        try {
                                            Serenity.getInstance().addChatMessage(resolveName(playerData.getProfile().getId()) + " is now in vanish.");
                                        } catch (IOException e) {}
                                    }, "Name Resolver Thread").start();
                                    vanishedPlayers.add(playerData.getProfile().getId());
                                }
                            }
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                for (UUID uuid : new HashSet<>(vanishedPlayers)) {
                    if (mc.getNetHandler().getPlayerInfo(uuid) != null) {
                        try {
                            Serenity.getInstance().addChatMessage(resolveName(uuid) + " is no longer in vanish.");
                        } catch (IOException e) {}
                        vanishedPlayers.remove(uuid);
                    }
                }
            }
        });
    }

    public String resolveName(UUID uuid) throws IOException {
        if (uuidNameCache.containsKey(uuid)) {
            return uuidNameCache.get(uuid);
        }
        return "undefined - " + uuid.toString();
    }
}
