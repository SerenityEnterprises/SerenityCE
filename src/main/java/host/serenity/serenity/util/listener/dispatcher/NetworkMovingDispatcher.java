package host.serenity.serenity.util.listener.dispatcher;

import host.serenity.serenity.event.network.NetMovingUpdate;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NetworkMovingDispatcher extends Listener<SendPacket> {
    @Override
    public void call(SendPacket event) {
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

            double x = packet.isMoving() ? packet.getPositionX() : mc.thePlayer.posX;
            double y = packet.isMoving() ? packet.getPositionY() : mc.thePlayer.posY;
            double z = packet.isMoving() ? packet.getPositionZ() : mc.thePlayer.posZ;
            float yaw = packet.getRotating() ? packet.getYaw() : mc.thePlayer.rotationYaw;
            float pitch = packet.getRotating() ? packet.getPitch() : mc.thePlayer.rotationPitch;
            NetMovingUpdate updateEvent = new NetMovingUpdate(x, y, z, yaw, pitch, packet.isOnGround());
            EventManager.post(updateEvent);

            boolean moved = x != updateEvent.getX() || y != updateEvent.getY() || z != updateEvent.getZ();
            moved = moved || packet.isMoving();
            boolean rotated = yaw != updateEvent.getYaw() || pitch != updateEvent.getPitch();
            rotated = rotated || packet.getRotating();

            if (!moved && !rotated)
                event.setPacket(new C03PacketPlayer(updateEvent.getOnGround()));
            if (moved && !rotated)
                event.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(updateEvent.getX(), updateEvent.getY(), updateEvent.getZ(), updateEvent.getOnGround()));
            if (rotated && !moved)
                event.setPacket(new C03PacketPlayer.C05PacketPlayerLook(updateEvent.getYaw(), updateEvent.getPitch(), updateEvent.getOnGround()));
            if (moved && rotated)
                event.setPacket(new C03PacketPlayer.C06PacketPlayerPosLook(updateEvent.getX(), updateEvent.getY(), updateEvent.getZ(), updateEvent.getYaw(), updateEvent.getPitch(), updateEvent.getOnGround()));
        }
    }
}
