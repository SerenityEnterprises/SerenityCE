package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.PostReceivePacket;
import host.serenity.serenity.event.player.*;
import host.serenity.serenity.event.render.Culling;
import host.serenity.serenity.event.render.RenderHand;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

public class Freecam extends Module {
    private AxisAlignedBB boundingBox;
    private double motionX, motionY, motionZ;

    @ModuleValue
    @ValueDescription("The speed at which to fly at.")
    private DoubleValue speed = new DoubleValue("Speed", 0.5);

    @ModuleValue
    @ValueDescription("Allows you to instant mine.")
    private BooleanValue instantMine = new BooleanValue("Instant Mine", true);

    public Freecam() {
        super("Freecam", 0xFBFF9A, ModuleCategory.PLAYER);
        registerToggleKeybinding(Keyboard.KEY_B);

        addCommandBranch(new CommandBranch("reset_motion", ctx -> {
            motionX = motionZ = 0;
            motionY = -0.0625;
        }));

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                event.setCancelled(true);
            }
        });

        listeners.add(new Listener<PostReceivePacket>() {
            @Override
            public void call(PostReceivePacket event) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    boundingBox = mc.thePlayer.getEntityBoundingBox();
                    motionX = motionY = motionZ = 0;
                }
            }
        });

        listeners.add(new Listener<InsideOpaqueBlock>() {
            @Override
            public void call(InsideOpaqueBlock event) {
                event.setInsideOpaqueBlock(false);
            }
        });

        listeners.add(new Listener<PushOutOfBlocks>() {
            @Override
            public void call(PushOutOfBlocks event) {
                event.setCancelled(true);
            }
        });

        listeners.add(new Listener<BlockDigging>() {
            @Override
            public void call(BlockDigging event) {
                mc.thePlayer.onGround = true;
                if (instantMine.getValue()) {
                    mc.theWorld.setBlockToAir(event.getPos());
                }
            }
        });

        listeners.add(new Listener<Culling>() {
            @Override
            public void call(Culling event) {
                event.setCancelled(true);
            }
        });

        listeners.add(new Listener<MovePlayer>() {
            @Override
            public void call(MovePlayer event) {
                mc.thePlayer.onGround = true;
                event.setX(mc.thePlayer.motionX = 0);
                event.setY(mc.thePlayer.motionY = 0);
                event.setZ(mc.thePlayer.motionZ = 0);

                double x = 0, y = 0, z = 0;
                double multiplier = speed.getValue();

                if (mc.gameSettings.keyBindForward.isKeyDown()) {
                    float dir = (float) Math.toRadians(mc.thePlayer.rotationYaw);
                    x += -Math.sin(dir) * multiplier;
                    z += Math.cos(dir) * multiplier;
                }
                if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                    float dir = (float) Math.toRadians(mc.thePlayer.rotationYaw - 90);
                    x += -Math.sin(dir) * multiplier;
                    z += Math.cos(dir) * multiplier;
                }
                if (mc.gameSettings.keyBindBack.isKeyDown()) {
                    float dir = (float) Math.toRadians(mc.thePlayer.rotationYaw + 180);
                    x += -Math.sin(dir) * multiplier;
                    z += Math.cos(dir) * multiplier;
                }
                if (mc.gameSettings.keyBindRight.isKeyDown()) {
                    float dir = (float) Math.toRadians(mc.thePlayer.rotationYaw + 90);
                    x += -Math.sin(dir) * multiplier;
                    z += Math.cos(dir) * multiplier;
                }

                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    y += multiplier;
                }

                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    y -= multiplier;
                }

                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(x, y, z));
            }
        });

        listeners.add(new Listener<RenderHand>() {
            @Override
            public void call(RenderHand event) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    protected void onEnable() {
        boundingBox = mc.thePlayer.getEntityBoundingBox();
        mc.renderChunksMany = false;

        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
    }

    @Override
    protected void onDisable() {
        setDisplay(getName());

        mc.thePlayer.setEntityBoundingBox(boundingBox);
        mc.renderChunksMany = true;

        mc.thePlayer.motionX = motionX;
        mc.thePlayer.motionY = motionY;
        mc.thePlayer.motionZ = motionZ;
    }
}
