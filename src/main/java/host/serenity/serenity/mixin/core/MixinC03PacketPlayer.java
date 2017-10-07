package host.serenity.serenity.mixin.core;

import host.serenity.serenity.util.iface.C03PacketPlayerExtension;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(C03PacketPlayer.class)
public abstract class MixinC03PacketPlayer implements C03PacketPlayerExtension {
    @Shadow
    protected double x;
    @Shadow
    protected double y;
    @Shadow
    protected double z;

    @Shadow
    protected float yaw;
    @Shadow
    protected float pitch;

    @Shadow
    protected boolean onGround;

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
