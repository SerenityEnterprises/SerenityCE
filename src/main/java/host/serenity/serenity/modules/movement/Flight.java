package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.FloatValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.network.PostReceivePacket;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.MovePlayer;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class Flight extends Module {

    public Flight() {
        super("Flight", 0x68FFFC, ModuleCategory.MOVEMENT);
        registerToggleKeybinding(Keyboard.KEY_R);

        getModuleModes().add(new ModuleMode("Creative") {
            private static final float DEFAULT_FLY_SPEED = 0.05F;
            private FloatValue speed = new FloatValue("Speed", 1);

            @Override
            public void addListeners(List<Listener<?>> listeners) {

                listeners.add(new Listener<PlayerUpdate>() {
                    @Override
                    public void call(PlayerUpdate event) {
                        mc.thePlayer.capabilities.isFlying = true;
                        mc.thePlayer.capabilities.setFlySpeed(DEFAULT_FLY_SPEED * speed.getValue());
                    }
                });

                listeners.add(new Listener<PostReceivePacket>() {
                    @Override
                    public void call(PostReceivePacket event) {
                        if (event.getPacket() instanceof S39PacketPlayerAbilities) {
                            mc.thePlayer.capabilities.isFlying = true;
                            mc.thePlayer.capabilities.setFlySpeed(DEFAULT_FLY_SPEED * speed.getValue());
                        }
                    }
                });
            }


            @Override
            public void onDisable() {
                mc.thePlayer.capabilities.isFlying = false;
                mc.thePlayer.capabilities.setFlySpeed(DEFAULT_FLY_SPEED);
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[] { speed };
            }
        });

        getModuleModes().add(new ModuleMode("Hard") {
            private DoubleValue speed = new DoubleValue("Speed", 1);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<MoveInput>() {
                    @Override
                    public void call(MoveInput event) {
                        if (event.getMovementInput().sneak && !event.getMovementInput().jump)
                            event.getMovementInput().sneak = false;
                    }
                });

                listeners.add(new Listener<MovePlayer>() {
                    @Override
                    public void call(MovePlayer event) {
                        mc.thePlayer.capabilities.isFlying = true;
                        mc.thePlayer.onGround = false;

                        double x = 0, y = 0, z = 0;
                        double multiplier = speed.getValue() * 0.5;

                        mc.thePlayer.motionX = 0;
                        mc.thePlayer.motionY = 0;
                        mc.thePlayer.motionZ = 0;

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

                        event.setX(x);
                        event.setY(y);
                        event.setZ(z);
                    }
                });
            }

            @Override
            public void onDisable() {
                mc.thePlayer.capabilities.isFlying = false;
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[] { speed };
            }
        });

        setActiveMode("Creative");
    }
}
