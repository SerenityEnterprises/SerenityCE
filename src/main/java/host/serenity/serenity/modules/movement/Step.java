package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.core.BlockBB;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Step extends Module {
    private double previousX, previousY, previousZ;
    private double offsetX, offsetY, offsetZ;
    private double frozenX, frozenZ;
    private byte cancelStage;

    public Step() {
        super("Step", 0x66B2FF, ModuleCategory.MOVEMENT);

        listeners.add(new Listener<BlockBB>() {
            @Override
            public void call(BlockBB event) {
                if (event.getY() >= mc.thePlayer.posY + 2 && cancelStage != 0) {
                    event.setBoundingBox(null);
                }
            }
        });

        listeners.add(new Listener<MoveInput>() {
            @Override
            public void call(MoveInput event) {
                if (cancelStage != 0)
                    event.getMovementInput().jump = false;
            }
        });

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                offsetX = 0;
                offsetY = 0;
                offsetZ = 0;

                mc.thePlayer.stepHeight = mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && cancelStage == 0 && mc.thePlayer.posY % 1 == 0 ? 1.1F : 0.5F;

                if (cancelStage == -1) {
                    cancelStage = 0;
                    return;
                }

                double yDist = mc.thePlayer.posY - previousY;
                double hDistSq = (mc.thePlayer.posX - previousX) * (mc.thePlayer.posX - previousX) + (mc.thePlayer.posZ - previousZ) * (mc.thePlayer.posZ - previousZ);

                if (yDist > 0.5 && yDist < 1.05 && hDistSq < 1 && cancelStage == 0) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(previousX, previousY + 0.42, previousZ, false));
                    offsetX = previousX - mc.thePlayer.posX;
                    offsetY = 0.755 - yDist;
                    offsetZ = previousZ - mc.thePlayer.posZ;

                    frozenX = previousX;
                    frozenZ = previousZ;
                    mc.thePlayer.stepHeight = 1.05F;
                    cancelStage = 1;
                }


                switch (cancelStage) {
                    case 1:
                        cancelStage = 2;
                        mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(frozenX - mc.thePlayer.posX, 0, frozenZ - mc.thePlayer.posZ));
                        break;
                    case 2:
                        event.setCancelled(true);
                        cancelStage = -1;
                        break;
                }

                previousX = mc.thePlayer.posX;
                previousY = mc.thePlayer.posY;
                previousZ = mc.thePlayer.posZ;

                if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                    mc.thePlayer.posX += offsetX;
                    mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0, offsetY, 0));
                    mc.thePlayer.posZ += offsetZ;
                }

            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (offsetX != 0 || offsetY != 0 || offsetZ != 0) {
                    mc.thePlayer.posX -= offsetX;
                    mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0, -offsetY, 0));
                    mc.thePlayer.posZ -= offsetZ;
                }
            }
        });
    }

    private boolean shouldStep() {
        return mc.thePlayer.onGround;
    }

    @Override
    protected void onEnable() {
        cancelStage = 0;
    }

    @Override
    protected void onDisable() {
        mc.thePlayer.stepHeight = 0.5F;
    }
}
