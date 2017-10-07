package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.player.MovePlayer;
import host.serenity.synapse.Listener;
import net.minecraft.util.MathHelper;

public class Strafe extends Module {
    public Strafe() {
        super("Strafe", 0x6770FF, ModuleCategory.MOVEMENT);

        listeners.add(new Listener<MovePlayer>() {
            @Override
            public void call(MovePlayer event) {
                if (mc.thePlayer.hurtTime > 0)
                    return;

                boolean moving = Math.abs(mc.thePlayer.movementInput.moveForward) > 0.1 || Math.abs(mc.thePlayer.movementInput.moveStrafe) > 0.1;
                if (moving) {
                    double moveSpeed = MathHelper.sqrt_double(event.getX() * event.getX() + event.getZ() * event.getZ());

                    float forward = mc.thePlayer.movementInput.moveForward;
                    float strafe = mc.thePlayer.movementInput.moveStrafe;
                    float yaw = mc.thePlayer.rotationYaw;
                    if (forward == 0 && strafe == 0) {
                        event.setX(0);
                        event.setZ(0);
                    } else if (forward != 0) {
                        if (strafe >= 1.0f) {
                            yaw += ((forward > 0.0f) ? -45 : 45);
                            strafe = 0.0f;
                        } else if (strafe <= -1.0f) {
                            yaw += ((forward > 0.0f) ? 45 : -45);
                            strafe = 0.0f;
                        }
                    }

                    if (forward > 0) {
                        forward = 1;
                    } else if (forward < 0) {
                        forward = -1;
                    }

                    final double mx = Math.cos(Math.toRadians(yaw + 90));
                    final double mz = Math.sin(Math.toRadians(yaw + 90));

                    event.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
                    event.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);

                    mc.thePlayer.motionX = event.getX();
                    mc.thePlayer.motionZ = event.getZ();
                } else {
                    event.setX(0);
                    event.setZ(0);
                }
            }
        });
    }
}
