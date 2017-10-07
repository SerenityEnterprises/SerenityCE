package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.player.MovePlayer;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.Listener;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Speed extends Module {
    public Speed() {
        super("Speed", 0x3BFF84, ModuleCategory.MOVEMENT);
        registerToggleKeybinding(Keyboard.KEY_G);

        registerMode(new ModuleMode("Lemon") {
            private int stage;
            private double moveSpeed;
            private double lastDist;
            private boolean speed = true;
            private int delay2 = 0;


            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<MovePlayer>() {
                    @Override
                    public void call(MovePlayer event) {
                        if (mc.thePlayer.isSneaking())
                            return;

                        if (mc.thePlayer.fallDistance > 4)
                            return;
                        ((MinecraftExtension) mc).getTimer().timerSpeed = 1.08F;
                        if (round(mc.thePlayer.posY - (int) mc.thePlayer.posY, 3) == round(0.138, 3)) {
                            mc.thePlayer.motionY -= 1;
                            event.setY(event.getY() - 0.0931);
                        }
                        if (stage == 2 && (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0)) {
                            event.setY(0.4);
                            mc.thePlayer.motionY = 0.39936;
                            speed = !speed;
                            moveSpeed *= speed ? 1.685 : 1.395;
                        } else if (stage == 3) {
                            final double difference = (speed ? 0.66 : 0.66) * (lastDist - getBaseMoveSpeed());
                            moveSpeed = lastDist - difference;
                            ((MinecraftExtension) mc).getTimer().timerSpeed = speed ? 1.125f : 1.0088f;
                        } else {
                            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, mc.thePlayer.motionY, 0.0)).size() > 0 || mc.thePlayer.isCollidedVertically) {
                                stage = 1;
                            }
                            moveSpeed = lastDist - lastDist / 159.0;
                        }

                        moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
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

                        if (strafe > 0) {
                            strafe = 1;
                        } else if (strafe < 0) {
                            strafe = -1;
                        }

                        final double mx = Math.cos(Math.toRadians(yaw + 90));
                        final double mz = Math.sin(Math.toRadians(yaw + 90));

                        event.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
                        event.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);

                        stage++;
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
                        final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;

                        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                    }
                });
            }

            @Override
            public void onEnable() {
                ((MinecraftExtension) mc).getTimer().timerSpeed = 1;
                moveSpeed = getBaseMoveSpeed();
                lastDist = 0;
                stage = 4;
                if (mc.theWorld != null) {
                    mc.thePlayer.motionX = 0;
                    mc.thePlayer.motionZ = 0;
                }
            }

            private double getBaseMoveSpeed() {
                double baseSpeed = 0.2873;
                if (mc.theWorld != null) {
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        final int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                        baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
                    }
                }

                return baseSpeed;
            }

            public double round(final double value, final int places) {
                if (places < 0) {
                    throw new IllegalArgumentException();
                }
                BigDecimal bd = new BigDecimal(value);
                bd = bd.setScale(places, RoundingMode.HALF_UP);
                return bd.doubleValue();
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        registerMode(new ModuleMode("Timer") {
            @ValueDescription("Adjusts the timer speed.")
            private DoubleValue timer = new DoubleValue("Timer", 2, 0.01, 20);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        ((MinecraftExtension) mc).getTimer().timerSpeed = timer.getValue().floatValue();
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[] { timer };
            }

            @Override
            public void onDisable() {
                ((MinecraftExtension) mc).getTimer().timerSpeed = 1;
            }
        });

        registerMode(new ModuleMode("Vanilla") {

            @ValueDescription("Adjusts the player speed.")
            private DoubleValue speed = new DoubleValue("Speed", 2, 0, 10);

            @ValueDescription("Adjusts the timer speed.")
            private DoubleValue timer = new DoubleValue("Timer", 1, 0.01, 20);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        ((MinecraftExtension) mc).getTimer().timerSpeed = timer.getValue().floatValue();
                    }
                });

                listeners.add(new Listener<MovePlayer>() {
                    @Override
                    public void call(MovePlayer event) {
                        boolean moving = Math.abs(mc.thePlayer.movementInput.moveForward) > 0.1 || Math.abs(mc.thePlayer.movementInput.moveStrafe) > 0.1;
                        if (moving) {
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

                            event.setX(forward * speed.getValue() * mx + strafe * speed.getValue() * mz);
                            event.setZ(forward * speed.getValue() * mz - strafe * speed.getValue() * mx);

                            mc.thePlayer.motionX = event.getX();
                            mc.thePlayer.motionZ = event.getZ();
                        } else {
                            event.setX(0);
                            event.setZ(0);
                        }
                        if (!mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX + event.getX(), mc.thePlayer.posY + event.getY(), mc.thePlayer.posZ + event.getZ())))
                            event.setY(mc.thePlayer.motionY = 0);
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[] { speed, timer };
            }
        });

        setActiveMode("Lemon");
    }

    @Override
    protected void onDisable() {
        ((MinecraftExtension) mc).getTimer().timerSpeed = 1;
    }
}
