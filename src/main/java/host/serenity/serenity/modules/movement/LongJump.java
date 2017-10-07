package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.network.NetMovingUpdate;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.MovePlayer;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class LongJump extends Module {
    private static final double MINECRAFT_RESISTANCE = 0.9800000190734863D;
    private static final double MINECRAFT_GRAVITY = 0.08;
    private static final double MIN_GRAVITY = -0.624000000001 / 20D;

    public LongJump() {
        super("Long Jump", 0xB254FF, ModuleCategory.MOVEMENT);
        registerToggleKeybinding(Keyboard.KEY_L);

        registerMode(new ModuleMode("Long") {
            private boolean jumping;
            private int ticksOnGround;
            private boolean canJump;

            private double posX, posY, posZ;
            private boolean fixing;

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<ReceivePacket>() {
                    @Override
                    public void call(ReceivePacket event) {
                        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                            canJump = false;
                        }
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        ((MinecraftExtension) mc).getTimer().timerSpeed = 1.0F;
                        if (mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.movementInput.moveForward > 0) {
                            if (mc.thePlayer.motionY == 0.33319999363422365D) {
                                if (canJump) {
                                    double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                                    double amount = 1.261;
                                    mc.thePlayer.motionX = -(Math.sin(yaw) * amount);
                                    mc.thePlayer.motionZ = (Math.cos(yaw) * amount);
                                    jumping = true;
                                    canJump = false;
                                }
                            }
                        }

                        if (mc.thePlayer.motionY < -0.35 || mc.thePlayer.onGround || mc.thePlayer.isOnLadder()) {
                            jumping = false;
                        }
                        if (jumping) {
                            if (mc.thePlayer.motionY < 0) {
                                mc.thePlayer.motionY = (mc.thePlayer.motionY / MINECRAFT_RESISTANCE) + MINECRAFT_GRAVITY;
                                mc.thePlayer.motionY += MIN_GRAVITY;
                            }
                            if (mc.thePlayer.motionY == 0.33319999363422365D) {
                                mc.thePlayer.motionY = (mc.thePlayer.motionY / MINECRAFT_RESISTANCE) + MINECRAFT_GRAVITY;
                                mc.thePlayer.motionY += -0.0665;
                            } else {
                                double amount = 1.028;
                                mc.thePlayer.motionX *= amount;
                                mc.thePlayer.motionZ *= amount;
                            }
                        }
                    }
                });

                listeners.add(new Listener<MoveInput>() {
                    @Override
                    public void call(MoveInput event) {
                        if (mc.thePlayer.movementInput.moveForward > 0 && mc.thePlayer.movementInput.jump && !mc.thePlayer.isSprinting() && mc.thePlayer.onGround) {
                            mc.thePlayer.setSprinting(true);
                            mc.thePlayer.movementInput.jump = false;
                        }

                        if (fixing) {
                            if (!canJump && mc.thePlayer.onGround && !BlockHelper.isOnLiquid() && !mc.thePlayer.isInWater()) {
                                mc.thePlayer.movementInput.jump = false;
                                if (ticksOnGround < 5) {
                                    mc.thePlayer.movementInput.moveForward = 0;
                                    mc.thePlayer.movementInput.moveStrafe = 0;
                                }
                            }
                        }
                    }
                });

                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        posX = mc.thePlayer.posX;
                        posY = mc.thePlayer.posY;
                        posZ = mc.thePlayer.posZ;

                        if (mc.thePlayer.isInWater()) {
                            jumping = false;
                            return;
                        }
                        if (!canJump && mc.thePlayer.onGround && !BlockHelper.isOnLiquid()) {
                            fixing = true;
                            if (ticksOnGround < 5) {
                                mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                            }

                            if (ticksOnGround == 1 || ticksOnGround == 3) {
                                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                                double distance = 0.0000001;
                                mc.thePlayer.setPosition(mc.thePlayer.posX - Math.sin(yaw) * distance, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ + Math.sin(yaw) * distance);
                            }

                            ticksOnGround++;
                            mc.thePlayer.onGround = false;

                            if (ticksOnGround == 6) {
                                canJump = true;
                                ticksOnGround = 0;
                                fixing = false;
                            }
                        }
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        if (fixing) {
                            mc.thePlayer.setPosition(posX, posY, posZ);
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }

            @Override
            public void onEnable() {
                posX = mc.thePlayer.posX;
                posY = mc.thePlayer.posY;
                posZ = mc.thePlayer.posZ;

                canJump = true;
                ticksOnGround = 0;
                jumping = false;
                fixing = false;
            }

            @Override
            public void onDisable() {
                ((MinecraftExtension) mc).getTimer().timerSpeed = 1.0F;
            }
        });

        registerMode(new ModuleMode("Safe") {
            private boolean didJump, fixing;
            private int ticks;

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        if (!didJump) {
                            if (mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.movementInput.moveForward > 0) {
                                if (mc.thePlayer.motionY == 0.33319999363422365D) {
                                    double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                                    double amount = 1.261;
                                    mc.thePlayer.motionX = -(Math.sin(yaw) * amount);
                                    mc.thePlayer.motionZ = (Math.cos(yaw) * amount);
                                    didJump = true;
                                }
                            }
                        }
                    }
                });

                listeners.add(new Listener<NetMovingUpdate>() {
                    @Override
                    public void call(NetMovingUpdate event) {
                        if (fixing && ticks == 2) {
                            event.setY(event.getY() + 0.4);
                            event.setOnGround(false);
                        }
                    }
                });

                listeners.add(new Listener<MoveInput>() {
                    @Override
                    public void call(MoveInput event) {
                        if (fixing) {
                            event.getMovementInput().jump = false;
                        }
                    }
                });

                listeners.add(new Listener<MovePlayer>() {
                    @Override
                    public void call(MovePlayer event) {
                        if (!didJump)
                            ticks = 0;

                        if (mc.thePlayer.onGround && didJump) {
                            fixing = true;
                        }

                        if (fixing) {
                            ticks++;

                            event.setX(0);
                            event.setZ(0);

                            if (ticks > 6) {
                                didJump = false;
                                fixing = false;
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                                mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                                setState(false);
                            }
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }

            @Override
            public void onEnable() {
                fixing = false;
                didJump = false;
                ticks = 0;
            }
        });

        setActiveMode("Safe");
    }
}
