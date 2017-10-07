package host.serenity.serenity.mixin.core;

import com.mojang.authlib.GameProfile;
import host.serenity.serenity.Serenity;
import host.serenity.serenity.event.player.*;
import host.serenity.serenity.modules.combat.KeepSprint;
import host.serenity.serenity.modules.movement.NoSlowdown;
import host.serenity.serenity.modules.movement.Sprint;
import host.serenity.synapse.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    /* TODO: Replace with a @Redirect() method in order to fix interop with other mods. */
    @Override
    public void moveEntity(double x, double y, double z) {
        MovePlayer event = EventManager.post(new MovePlayer(x, y, z));
        super.moveEntity(event.getX(), event.getY(), event.getZ());
        EventManager.post(new PostMovePlayer());
    }

    @Override
    public boolean isEntityInsideOpaqueBlock() {
        return EventManager.post(new InsideOpaqueBlock(super.isEntityInsideOpaqueBlock())).getInsideOpaqueBlock();
    }

    @Inject(method = "pushOutOfBlocks(DDD)Z", at = @At("HEAD"), cancellable = true)
    public void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (EventManager.post(new PushOutOfBlocks()).isCancelled())
            callbackInfo.setReturnValue(false);
    }


    private float _yaw, _pitch;

    @Inject(method = "onUpdateWalkingPlayer()V", at = @At("HEAD"), cancellable = true)
    public void preUpdateWalkingPlayer(CallbackInfo callbackInfo) {
        PlayerWalkingUpdate event = EventManager.post(new PlayerWalkingUpdate(this.rotationYaw, this.rotationPitch));

        _yaw = this.rotationYaw;
        _pitch = this.rotationPitch;

        if (event.isCancelled()) {
            callbackInfo.cancel();
        } else {
            if (event.getYaw() != _yaw || event.getPitch() != _pitch) {
                this.rotationYaw = wrapAngleTo180(event.getYaw());
                this.rotationPitch = wrapAngleTo180(event.getPitch());
            }
        }
    }

    @Inject(method = "onUpdateWalkingPlayer()V", at = @At("RETURN"))
    public void postUpdateWalkingPlayer(CallbackInfo callbackInfo) {
        this.rotationYaw = _yaw;
        this.rotationPitch = _pitch;

        EventManager.post(new PostPlayerWalkingUpdate());
        EventManager.post(new _FinalPlayerWalkingUpdate());
    }

    private static float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        while (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    @Shadow @Final
    public NetHandlerPlayClient sendQueue;

    /**
     * @author serenity.host
     */
    @Overwrite
    public void sendChatMessage(String message) {
        SendChat event = EventManager.post(new SendChat(message));
        message = event.getMessage();

        if (!event.isCancelled())
            this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
    }

    @Inject(method = "onUpdate()V", at = @At("HEAD"))
    public void onUpdateCallback(CallbackInfo callbackInfo) {
        EventManager.post(new PlayerUpdate());
    }

    @Shadow
    public int sprintingTicksLeft;

    @Shadow
    public MovementInput movementInput;

    /**
     * @author serenity.host
     */
    @Overwrite
    public void setSprinting(boolean sprinting) {
        try {
            Sprint sprint = Serenity.getInstance().getModuleManager().getModule(Sprint.class);
            if (sprint.isEnabled()) {
                sprinting = sprint.shouldSprint();
            }
        } catch (Exception e) {}
        try {
            KeepSprint keepSprint = Serenity.getInstance().getModuleManager().getModule(KeepSprint.class);

            if (keepSprint.isEnabled()) {
                Minecraft mc = Minecraft.getMinecraft();

                boolean using = mc.thePlayer.isUsingItem() && !Serenity.getInstance().getModuleManager().getModule(NoSlowdown.class).isEnabled();
                boolean checks = mc.thePlayer.moveForward > 0.1 && mc.thePlayer.getFoodStats().getFoodLevel() > 6 && !mc.thePlayer.isInWater() && !mc.thePlayer.isSneaking() && !using;

                if (!sprinting && checks)
                    return;
            }
        } catch (Exception e) {}

        super.setSprinting(sprinting);
        this.sprintingTicksLeft = sprinting ? 600 : 0;
    }

    @Override
    public void jump() {
        super.jump();

        if (this.isSprinting()) {
            try {
                Sprint sprint = Serenity.getInstance().getModuleManager().getModule(Sprint.class);
                if (sprint.isEnabled() && sprint.omnidirectional.getValue()) {
                    float f = (float) Math.toRadians(this.rotationYaw);
                    this.motionX += (double) (MathHelper.sin(f) * 0.2F);
                    this.motionZ -= (double) (MathHelper.cos(f) * 0.2F);

                    float yaw = this.rotationYaw;
                    if (this.moveForward < 0) {
                        yaw += 180;
                    }

                    if (this.moveStrafing > 0) {
                        yaw -= 90 * (this.moveForward > 0 ? 0.5F : this.moveForward < 0 ? -0.5F : 1);
                    }

                    if (this.moveStrafing < 0) {
                        yaw += 90 * (this.moveForward > 0 ? 0.5F : this.moveForward < 0 ? -0.5F : 1);
                    }

                    f = (float) Math.toRadians(yaw);
                    this.motionX -= (double) (MathHelper.sin(f) * 0.2F);
                    this.motionZ += (double) (MathHelper.cos(f) * 0.2F);
                }
            } catch (Exception e) {}
        }
    }
}
