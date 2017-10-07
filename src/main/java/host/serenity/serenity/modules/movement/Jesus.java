package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.core.BlockBB;
import host.serenity.serenity.event.network.PostSendPacket;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.serenity.util.iface.C03PacketPlayerExtension;
import host.serenity.synapse.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

public class Jesus extends Module {
    private int ticker = 0;

    @ModuleValue
    @ValueDescription("Allow sprint jumping to bypass NoCheatPlus.")
    private BooleanValue sprintJumping = new BooleanValue("Sprint Jumping", true);

    private boolean wasInWater;

    public Jesus() {
        super("Jesus", 0x2FA1FF, ModuleCategory.MOVEMENT);

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (mc.thePlayer.onGround) {
                    wasInWater = false;
                    return;
                }

                if (mc.thePlayer.fallDistance > 1 && !wasInWater && BlockHelper.isInLiquid()) {
                    mc.thePlayer.motionY = 0;
                    mc.thePlayer.fallDistance = 0;
                }

                wasInWater = mc.thePlayer.fallDistance > 1 && BlockHelper.isInLiquid();
            }
        });

        listeners.add(new Listener<BlockBB>() {
            @Override
            public void call(BlockBB event) {
                if (event.getBlock() instanceof BlockLiquid) {
                    if (!mc.thePlayer.isInWater() && !mc.thePlayer.isSneaking() && event.getY() < mc.thePlayer.posY - 0.99) {
                        if (mc.thePlayer.fallDistance < 3) {
                            event.setBoundingBox(new AxisAlignedBB(event.getX(), event.getY(), event.getZ(), event.getX() + 1, event.getY() + (1 - 0.0001), event.getZ() + 1));
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (event.getPacket() instanceof C03PacketPlayer) {
                    C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

                    ticker++;
                    if (BlockHelper.isOnLiquid()) {
                        if (ticker % 2 == 0) {
                            if (packet.isMoving())
                                ((C03PacketPlayerExtension) packet).setY(packet.getPositionY()  + 0.0001);
                            ((C03PacketPlayerExtension) packet).setOnGround(false);

                            ticker = 0;
                        } else if (mc.gameSettings.keyBindJump.isKeyDown() && sprintJumping.getValue() && !BlockHelper.isInLiquid()) {
                            mc.thePlayer.motionX *= 0.55;
                            mc.thePlayer.motionZ *= 0.55;
                        }
                    } else {
                        ticker = mc.thePlayer.onGround ? 1 : 0;
                    }

                    /* if (BlockHelper.isOnLiquid() && mc.thePlayer.isSprinting()) {
                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    } */
                }
            }
        });

        listeners.add(new Listener<PostSendPacket>() {
            @Override
            public void call(PostSendPacket event) {
                if (event.getPacket() instanceof C03PacketPlayer) {
                    /* if (BlockHelper.isOnLiquid() && mc.thePlayer.isSprinting()) {
                        mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    } */
                }
            }
        });

        listeners.add(new Listener<MoveInput>() {
            @Override
            public void call(MoveInput event) {
                if (BlockHelper.isInLiquid() && mc.thePlayer.motionY < 0.1 && !mc.thePlayer.isSneaking() && mc.thePlayer.fallDistance < 3 && !mc.thePlayer.isInWater()) {
                    mc.thePlayer.motionY = 0.05;
                }

                if (ticker % 2 == 0) {
                    if (BlockHelper.isOnLiquid() && mc.thePlayer.onGround && !mc.thePlayer.isCollidedHorizontally) {
                        event.getMovementInput().jump = false;
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        ticker = 0;
    }
}
