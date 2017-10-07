package host.serenity.serenity.util;

import host.serenity.serenity.event.core.RunTick;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.modules.minigames.AntiBot;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S38PacketPlayerListItem;

import java.util.*;

public class BotDetector {
    private final Minecraft mc = Minecraft.getMinecraft();

    private Set<UUID> pingUpdatedUUIDs = new HashSet<>();
    private Set<UUID> sprintUpdatedUUIDs = new HashSet<>();
    private Set<UUID> sneakUpdatedUUIDs = new HashSet<>();
    private Set<UUID> hurtUUIDs = new HashSet<>();

    private Set<UUID> onFloorUUIDs = new HashSet<>();

    public BotDetector() {
        EventManager.register(new Listener<ReceivePacket>() {
            @Override
            @SuppressWarnings("unchecked")
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S38PacketPlayerListItem) {
                    S38PacketPlayerListItem packet = (S38PacketPlayerListItem) event.getPacket();

                    if (packet.func_179768_b() == S38PacketPlayerListItem.Action.UPDATE_LATENCY) {
                        ((List<S38PacketPlayerListItem.AddPlayerData>) packet.func_179767_a()).forEach(data -> {
                            pingUpdatedUUIDs.add(data.getProfile().getId());
                        });
                    }
                }
            }
        });

        EventManager.register(new Listener<RunTick>() {
            @Override
            public void call(RunTick event) {
                if (mc.theWorld != null) {
                    for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                        UUID uuid = player.getUniqueID();
                        if (!onFloorUUIDs.contains(uuid) && onFloor(player) && !inBlock(player) && player.posY % 1 == 0)
                            onFloorUUIDs.add(uuid);

                        if (!sprintUpdatedUUIDs.contains(uuid) && player.isSprinting())
                            sprintUpdatedUUIDs.add(uuid);

                        if (!sneakUpdatedUUIDs.contains(uuid) && player.isSneaking())
                            sneakUpdatedUUIDs.add(uuid);

                        if (!hurtUUIDs.contains(uuid) && player.hurtTime > 0)
                            hurtUUIDs.add(uuid);
                    }
                }
            }
        });
    }

    private boolean isOnTab(EntityPlayer player) {
        return ((Collection<NetworkPlayerInfo>) mc.getNetHandler().getPlayerInfoMap())
                .stream()
                .anyMatch(info -> info.getGameProfile().getName().equals(player.getCommandSenderName()));
    }

    private boolean onFloor(EntityPlayer player) {
        return !mc.theWorld.getCollidingBoundingBoxes(player, player.getEntityBoundingBox().offset(0, -0.0625, 0)).isEmpty();
    }

    private boolean inBlock(EntityPlayer player) {
        return !mc.theWorld.getCollidingBoundingBoxes(player, player.getEntityBoundingBox()).isEmpty();
    }

    private boolean isPlayerValid(EntityPlayer player) {
        if (player.getEntityId() >= 1000000000)
            return false;

        /* if (!onFloor(player) && player.posY == player.lastTickPosY)
            return false; */

        if (!isOnTab(player))
            return false;

        return true;
    }

    public boolean isValid(EntityPlayer player) {
        boolean valid = isPlayerValid(player);

        UUID uuid = player.getUniqueID();
        boolean suspicious = !(pingUpdatedUUIDs.contains(uuid) || sneakUpdatedUUIDs.contains(uuid) || sprintUpdatedUUIDs.contains(uuid));

        return valid && !AntiBot.isBot(player) && !suspicious && onFloorUUIDs.contains(uuid);
    }
}
