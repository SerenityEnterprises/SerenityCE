package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.*;
import host.serenity.serenity.event.player.MoveInput;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovementInput;

import java.util.List;
import java.util.Random;

/**
 * Created by Jordin on 4/25/2017.
 * Jordin is still best hacker.
 */
public class AntiAFK extends Module {
    @ModuleValue
    @ValueDescription("The time (in seconds) between performing each action.")
    public DoubleValue delay = new DoubleValue("Delay", 20, 0, 120);

    @ModuleValue
    @ValueDescription("Displays the units of the time remaining in the display name.")
    public BooleanValue showUnits = new BooleanValue("Show Units", true);

    private TimeHelper afkDelay = new TimeHelper();

    public AntiAFK() {
        super("Anti AFK", 0xFF6CE4FF, ModuleCategory.MISCELLANEOUS);

        Random random = new Random();

        getModuleModes().add(new ModuleMode("Jump") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>(() -> isAFK()) {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        if (mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                            afkDelay.reset();
                        }
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        getModuleModes().add(new ModuleMode("Move") {
            @ValueDescription("The distance to move.")
            public FloatValue moveDistance = new FloatValue("Distance", 0.3F, 0, 0.5F);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>(() -> isAFK()) {
                    private boolean backward = false;

                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        double offset = moveDistance.getValue();
                        backward = !backward;
                        if (!backward) {
                            offset *= -1;
                        }
                        double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                        mc.thePlayer.setPosition(mc.thePlayer.posX - Math.sin(yaw) * offset, mc.thePlayer.posY, mc.thePlayer.posZ + Math.cos(yaw) * offset);

                        afkDelay.reset();
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[] { moveDistance };
            }
        });

        getModuleModes().add(new ModuleMode("Swing") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>(() -> isAFK()) {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        mc.thePlayer.swingItem();
                        afkDelay.reset();
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        getModuleModes().add(new ModuleMode("Chat") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>(() -> isAFK()) {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        mc.thePlayer.sendChatMessage("/cmd" + random.nextInt(10000));
                        afkDelay.reset();
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        getModuleModes().add(new ModuleMode("Circle Strafe") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<MoveInput>() {
                    @Override
                    public void call(MoveInput event) {
                        MovementInput input = event.getMovementInput();
                        if (input.moveForward == 0 && input.moveStrafe == 0) {
                            float[][] states = {
                                    { 1, 0 },
                                    { 1, 1 },
                                    { 0, 1 },
                                    { -1, 1 },
                                    { -1, 0 },
                                    { -1, -1 },
                                    { 0, -1 },
                                    { 1, -1 }
                            };

                            float[] state = states[mc.thePlayer.ticksExisted % 8];
                            input.moveForward = state[0];
                            input.moveStrafe = state[1];
                        }
                    }
                });

                afkDelay.reset();
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {

                if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX ||
                        mc.thePlayer.lastTickPosY != mc.thePlayer.posY ||
                        mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
                    afkDelay.reset();
                }

                double timeRemaining = delay.getValue() - afkDelay.getDifference() / 1000d;
                if (timeRemaining < 0) {
                    timeRemaining = 0;
                }
                if (getActiveMode().getName().equals("Circle Strafe")) {
                    setDisplay(String.format("%s %s[%s]", getName(), EnumChatFormatting.GRAY, getActiveMode().getName()));
                } else {
                    setDisplay(String.format(showUnits.getValue() ? "%s %s[%s] [%.1fs]" : "%s %s[%s] [%.1f]", getName(), EnumChatFormatting.GRAY, getActiveMode().getName(), timeRemaining));
                }

            }
        });

        setActiveMode("Jump");
    }


    private boolean isAFK() {
        return mc.thePlayer.lastTickPosX == mc.thePlayer.posX &&
                mc.thePlayer.lastTickPosY == mc.thePlayer.posY &&
                mc.thePlayer.lastTickPosZ == mc.thePlayer.posZ &&
                afkDelay.hasReached((long) (delay.getValue() * 1000));
    }
}
