package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.serenity.event.player.PostMovePlayer;
import host.serenity.synapse.Listener;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class Glide extends Module {
    public Glide() {
        super("Glide", 0x88FF7C, ModuleCategory.MOVEMENT);

        registerMode(new ModuleMode("Hypixel") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerUpdate>() {
                    @Override
                    public void call(PlayerUpdate event) {
                        mc.thePlayer.motionY = 0.0;

                        double offset = 1.0E-9;
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ);

                        BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.2, mc.thePlayer.posZ);
                        if (mc.thePlayer.ticksExisted % 3 == 0
                                && mc.theWorld.getBlockState(below).getBlock() instanceof BlockAir) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ, true));
                        }
                    }
                });

                listeners.add(new Listener<PostMovePlayer>() {
                    @Override
                    public void call(PostMovePlayer event) {
                        // mc.thePlayer.onGround = true;
                    }
                });

                listeners.add(new Listener<MoveInput>() {
                    @Override
                    public void call(MoveInput event) {
                        event.getMovementInput().jump = false;
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[0];
            }
        });

        setActiveMode(0);
    }
}
