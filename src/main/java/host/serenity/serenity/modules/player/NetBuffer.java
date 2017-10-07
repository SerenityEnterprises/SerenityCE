package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.PushPacketToNetwork;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.math.Vector3;
import host.serenity.synapse.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class NetBuffer extends Module {
    private Queue<Packet> outboundPackets = new ArrayDeque<>();
    private boolean isFlushing = false;

    @ModuleValue
    private IntValue bufferSize = new IntValue("Buffer Size", 40);

    private Vector3 lastFlushPosition;

    public NetBuffer() {
        super("Net Buffer", 0xFF8193, ModuleCategory.PLAYER);

        listeners.add(new Listener<PushPacketToNetwork>() {
            @Override
            public void call(PushPacketToNetwork event) {
                if (!mc.thePlayer.isEntityAlive())
                    return;

                if (!isFlushing) {
                    if (event.getPacket() instanceof C02PacketUseEntity || event.getPacket() instanceof C08PacketPlayerBlockPlacement || event.getPacket() instanceof C07PacketPlayerDigging) {
                        flush();
                    } else {
                        event.setCancelled(true);
                        outboundPackets.add(event.getPacket());

                        if (outboundPackets.size() > bufferSize.getValue()) {
                            flush();
                        }
                    }
                }

                setTag(String.valueOf(outboundPackets.size()));
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (!mc.thePlayer.isEntityAlive() || lastFlushPosition == null)
                    return;

                if (mc.thePlayer.getDistanceSq(lastFlushPosition.x, lastFlushPosition.y, lastFlushPosition.z) < 4*4)
                    return;

                //noinspection unchecked
                for (EntityPlayer player : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    if (player == mc.thePlayer)
                        continue;

                    if (player.getDistanceSq(lastFlushPosition.x, lastFlushPosition.y, lastFlushPosition.z) < 4*4) {
                        flush();
                    }
                }
            }
        });
    }

    @Override
    protected void onEnable() {
        if (mc.thePlayer == null) {
            setState(false);
            return;
        }
        lastFlushPosition = new Vector3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    @Override
    protected void onDisable() {
        flush();
    }

    private void flush() {
        // lastFlushPosition = null;
        if (isFlushing)
            return;

        lastFlushPosition = new Vector3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        isFlushing = true;

        for (Packet packet : outboundPackets) {
            mc.getNetHandler().getNetworkManager().sendPacket(packet);

            if (packet instanceof C03PacketPlayer) {
                C03PacketPlayer player = (C03PacketPlayer) packet;

                if (player.isMoving()) {
                    lastFlushPosition = new Vector3(player.getPositionX(), player.getPositionY(), player.getPositionZ());
                }
            }
        }

        outboundPackets.clear();

        isFlushing = false;
    }

    private void setTag(String tag) {
        setDisplay(String.format("%s %s[%s]", this.getName(), EnumChatFormatting.GRAY, tag));
    }
}
