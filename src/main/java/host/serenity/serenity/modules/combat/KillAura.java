package host.serenity.serenity.modules.combat;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.*;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.player.PlayerUpdate;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.modules.minigames.AntiBot;
import host.serenity.serenity.modules.movement.NoSlowdown;
import host.serenity.serenity.util.BotDetector;
import host.serenity.serenity.util.ChatColor;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import me.jordin.deltoid.utils.ProjectionUtilities;
import me.jordin.deltoid.vector.Rotation;
import me.jordin.deltoid.vector.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.*;
import java.util.function.DoubleSupplier;

public class KillAura extends Module {

    @ModuleValue
    private DoubleValue range = new DoubleValue("Range", 3.8, 0, 8);

    @ModuleValue
    private BooleanValue players = new BooleanValue("Players", true);

    @ModuleValue
    private BooleanValue mobs = new BooleanValue("Mobs", true);

    @ModuleValue
    private BooleanValue animals = new BooleanValue("Animals", false);

    @ModuleValue
    private BooleanValue teams = new BooleanValue("Teams", false);

    private enum TargetingType {
        FOV("FOV"), HEALTH("Health"), RANGE("Range");

        private final String display;

        TargetingType(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    @ModuleValue
    private EnumValue<TargetingType> targetingType = new EnumValue<>("Targeting Type", TargetingType.FOV);

    private enum InvisibilityTargetingType {
        ALWAYS("Always"), ARMOURED("Armoured"), NEVER("Never");

        private final String display;

        InvisibilityTargetingType(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    @ModuleValue
    private EnumValue<InvisibilityTargetingType> invisibilityType = new EnumValue<>("Invisibility Type", InvisibilityTargetingType.ALWAYS);

    @ModuleValue
    private DoubleValue fov = new DoubleValue("FOV", 360, 0, 360);

    @ModuleValue
    private BooleanValue disableOnDeath = new BooleanValue("Disable on Death", false);

    @ModuleValue
    private IntValue baseDelay = new IntValue("Base Delay", 125, 0, 5000);

    @ModuleValue
    private IntValue randomDelay = new IntValue("Extra Random Delay", 50, 0, 2000);

    @ModuleValue
    private BooleanValue autoblock = new BooleanValue("Autoblock", true);

    private List<EntityLivingBase> potentialTargets = new ArrayList<>();
    private BotDetector botDetector = new BotDetector();

    private Map<Integer, Integer> idToLastHitMap = new HashMap<>();

    private TimeHelper time = new TimeHelper();
    private long calculatedDelay = 0;

    private Random random = new Random();

    private enum TweeningState {
        STAGE_ONE,
        STAGE_TWO,
        SNAP
    }

    private TweeningState tweeningState;

    public KillAura() {
        super("Kill Aura", 0xFF3831, ModuleCategory.COMBAT);

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                potentialTargets.clear();

                Vec3 eyePosition = getEntityPositionVector(mc.thePlayer).up(mc.thePlayer.getEyeHeight());
                Rotation currentAngles = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

                //noinspection unchecked
                for (Entity entity : (List<Entity>) mc.theWorld.loadedEntityList) {
                    if (entity == mc.thePlayer)
                        continue;

                    if (!entity.isEntityAlive())
                        continue;

                    if (entity instanceof EntityLivingBase) {
                        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                        if (!entityMatchesTargetTypes(entityLivingBase))
                            continue;

                        if (mc.thePlayer.getDistanceSqToEntity(entityLivingBase) > range.getValue() * range.getValue()) // TODO: Ping prediction (?)
                            continue;

                        if (!invisibilityCheck(entityLivingBase))
                            continue;

                        if (entity instanceof EntityPlayer) {
                            if (Serenity.getInstance().getFriendManager().isFriend(entity.getCommandSenderName()))
                                continue;

                            if (teams.getValue() && isOnSameTeam((EntityPlayer) entity))
                                continue;
                        }

                        Rotation targetAngles = ProjectionUtilities.faceOffsetDeg(getEntityPositionVector(entityLivingBase).subtract(eyePosition));
                        Rotation delta = targetAngles.subtract(currentAngles).wrapDegrees();
                        if (Math.abs(delta.rotationYaw) * 2 > fov.getValue())
                            continue;

                        potentialTargets.add(entityLivingBase);
                    }
                }

                if (!potentialTargets.isEmpty()) {
                    potentialTargets.sort(Comparator.comparing(entityLivingBase -> -calculateTargetingWeight(entityLivingBase)));

                    if (Serenity.getInstance().getModuleManager().getModule(AntiBot.class).isEnabled()) {
                        potentialTargets.removeIf(entityLivingBase -> {
                            return entityLivingBase instanceof EntityPlayer
                                    && !botDetector.isValid((EntityPlayer) entityLivingBase);
                        });
                    }

                    if (!potentialTargets.isEmpty()) {
                        tweeningState = TweeningState.STAGE_ONE;

                        if (time.hasReached(calculatedDelay - 100)) {
                            tweeningState = TweeningState.STAGE_TWO;
                        }


                        if (time.hasReached(calculatedDelay)) {
                            tweeningState = TweeningState.SNAP;
                        }

                        EntityLivingBase target = potentialTargets.get(0);
                        assert target != null; // We already checked if the targets list is empty

                        Vec3 targetLocation = getEntityPositionVector(target).up(target.getEyeHeight() / 1.2);

                        DoubleSupplier randomSupplier = () -> (random.nextGaussian() - 0.5) * 0.125;

                        targetLocation = targetLocation.add(new Vec3(randomSupplier.getAsDouble(), randomSupplier.getAsDouble(), randomSupplier.getAsDouble()));

                        Rotation targetAngles = ProjectionUtilities.faceOffsetDeg(targetLocation.subtract(eyePosition));

                        Rotation delta = targetAngles.subtract(currentAngles.wrapDegrees()).wrapDegrees();

                        double scaleFactor;
                        switch (tweeningState) {
                            default:
                            case STAGE_ONE:
                                scaleFactor = 0.2;
                                break;
                            case STAGE_TWO:
                                scaleFactor = 0.4;
                                break;
                            case SNAP:
                                scaleFactor = 1;
                        }
                        targetAngles = currentAngles.add(new Rotation(delta.rotationYaw * scaleFactor, delta.rotationPitch * scaleFactor));

                        event.setYaw((float) targetAngles.rotationYaw);
                        event.setPitch((float) targetAngles.rotationPitch);
                    }
                }
            }
        });

        listeners.add(new Listener<PlayerUpdate>() {
            @Override
            public void call(PlayerUpdate event) {
                if (!potentialTargets.isEmpty()) {
                    if (autoblock.getValue()) {
                        ItemStack stack = mc.thePlayer.getHeldItem();
                        if (stack != null && stack.getItem() instanceof ItemSword) {
                            mc.thePlayer.setItemInUse(stack, 999);
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (!potentialTargets.isEmpty()) {
                    if (potentialTargets.size() == 1) {
                        EntityLivingBase target = potentialTargets.get(0);

                        if (target.hurtTime > 0 && idToLastHitMap.getOrDefault(target.getEntityId(), 0) > mc.thePlayer.ticksExisted - 10)
                            return;
                    }

                    boolean noSlowdownEnabled = Serenity.getInstance().getModuleManager().getModule(NoSlowdown.class).isEnabled();

                    if (!noSlowdownEnabled && mc.thePlayer.isBlocking()) {
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                BlockPos.ORIGIN,
                                EnumFacing.UP));
                    }

                    if (time.hasReached(calculatedDelay)) {
                        calculatedDelay = baseDelay.getValue() + (randomDelay.getValue() > 0 ? random.nextInt(randomDelay.getValue()) : 0); // We can't nextInt() zero
                        time.reset();

                        // Vec3 eyePosition = getEntityPositionVector(mc.thePlayer).up(mc.thePlayer.getEyeHeight());

                        // TODO: Extra targets (perhaps split into a 'multi' mode?)

                        EntityLivingBase target = potentialTargets.get(0);

                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                        mc.thePlayer.swingItem();

                        idToLastHitMap.put(target.getEntityId(), mc.thePlayer.ticksExisted);
                    }

                    if (!noSlowdownEnabled && mc.thePlayer.isBlocking()) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    }
                }
            }
        });

        listeners.add(new Listener<SendPacket>(disableOnDeath::getValue) {
            @Override
            public void call(SendPacket event) {
                Packet packet = event.getPacket();
                if (packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.PERFORM_RESPAWN) {
                    setState(false);
                }
            }
        });
    }

    private boolean entityMatchesTargetTypes(EntityLivingBase entityLivingBase) {
        boolean matches = false;

        if (entityLivingBase instanceof EntityPlayer) {
            matches = players.getValue();
        } else if (entityLivingBase instanceof IMob) {
            matches = mobs.getValue();
        } else if (entityLivingBase instanceof IAnimals) {
            matches = animals.getValue();
        }

        if (mobs.getValue()) {
            if (entityLivingBase instanceof EntityWolf) {
                EntityWolf wolf = (EntityWolf) entityLivingBase;
                matches = wolf.isAngry();
            }
        }

        return matches;
    }

    private boolean invisibilityCheck(EntityLivingBase entity) {
        if (invisibilityType.getValue() == InvisibilityTargetingType.ALWAYS)
            return true;

        if (invisibilityType.getValue() == InvisibilityTargetingType.NEVER)
            return !entity.isInvisible();

        if (invisibilityType.getValue() == InvisibilityTargetingType.ARMOURED) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer p = (EntityPlayer) entity;
                boolean hasArmour = false;
                for (ItemStack stack : p.inventory.armorInventory) {
                    if (stack != null) {
                        hasArmour = true;
                    }
                }
                return !(entity.isInvisible() && entity.getHeldItem() == null && !hasArmour);
            }
            return !(entity.isInvisible() && entity.getHeldItem() == null);
        }

        return true;
    }

    private boolean isOnSameTeam(EntityPlayer player) {
        boolean teamChecks = false;

        ChatColor myCol = null;
        ChatColor enemyCol = null;
        for (ChatColor col : ChatColor.values()) {
            if (col == ChatColor.RESET)
                continue;
            if (mc.thePlayer.getDisplayName().getFormattedText().contains(col.toString()) && myCol == null) {
                myCol = col;
            }
            if (player.getDisplayName().getFormattedText().contains(col.toString()) && enemyCol == null) {
                enemyCol = col;
            }
        }
        try {
            if (myCol != null && enemyCol != null) {
                teamChecks = myCol != enemyCol;
            } else {
                if (mc.thePlayer.getTeam() != null) {
                    teamChecks = !mc.thePlayer.isOnSameTeam(player);
                } else {
                    if (mc.thePlayer.inventory.armorInventory[3].getItem() instanceof ItemBlock) {
                        teamChecks = !ItemStack.areItemStacksEqual(mc.thePlayer.inventory.armorInventory[3], player.inventory.armorInventory[3]);
                    }
                }
            }
        } catch (Exception e) {}

        return !teamChecks;
    }

    private double calculateTargetingWeight(EntityLivingBase entityLivingBase) {
        Vec3 eyePosition = getEntityPositionVector(mc.thePlayer).up(mc.thePlayer.getEyeHeight());
        Rotation currentAngles = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

        double weight = 0;

        switch (targetingType.getValue()) {
            case FOV:
                Rotation targetAngles = ProjectionUtilities.faceOffsetDeg(getEntityPositionVector(entityLivingBase).up(entityLivingBase.getEyeHeight()).subtract(eyePosition));
                Rotation delta = targetAngles.subtract(currentAngles).wrapDegrees();
                weight =  180 / delta.length();
                break;
            case RANGE:
                weight = 64 / mc.thePlayer.getDistanceSqToEntity(entityLivingBase);
                break;
            case HEALTH:
                weight = entityLivingBase.getHealth() + entityLivingBase.getAbsorptionAmount();
                break;
        }

        weight -= Math.min((idToLastHitMap.getOrDefault(entityLivingBase.getEntityId(), 0) - mc.thePlayer.ticksExisted), 25);

        return weight;
    }

    /* private static Vec3 predict(EntityPlayer player) {
        int pingTicks = (int) Math.ceil(Minecraft.getMinecraft().getNetHandler()
                .getPlayerInfo(Minecraft.getMinecraft().thePlayer.getUniqueID()).getResponseTime() / 50D);

        Vec3 predicted = Serenity.getInstance().getPredictionEngine().predictPlayerLocation(player, pingTicks);
        if (predicted != null)
            return predicted;

        return new Vec3(player.posX, player.posY, player.posZ);
    } */

    private static Vec3 getEntityPositionVector(EntityLivingBase entityLivingBase) {
        return new Vec3(entityLivingBase.posX, entityLivingBase.posY, entityLivingBase.posZ);
    }
}
