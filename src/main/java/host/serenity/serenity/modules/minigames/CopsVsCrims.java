package host.serenity.serenity.modules.minigames;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.IntValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.ChatColor;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.Listener;
import me.jordin.deltoid.utils.ProjectionUtilities;
import me.jordin.deltoid.vector.Rotation;
import me.jordin.deltoid.vector.Vec3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S30PacketWindowItems;

import java.util.ArrayList;
import java.util.List;

public class CopsVsCrims extends Module {
    private List<C0EPacketClickWindow> queuedClicks = new ArrayList<>();
    private int clicks;
    private TimeHelper time = new TimeHelper();
    private EntityPlayer target;
    private List<EntityPlayer> switchList = new ArrayList<>();

    @ModuleValue
    private IntValue delay = new IntValue("delay", 300);

    public CopsVsCrims() {
        super("Cops Vs Crims", 0x426AFF, ModuleCategory.MINIGAMES);

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (mc.thePlayer.openContainer == null || !(mc.thePlayer.openContainer instanceof ContainerChest)) {
                    queuedClicks.clear();
                    return;
                }
                if (clicks < 2 && !queuedClicks.isEmpty()) {
                    C0EPacketClickWindow windowClick = queuedClicks.remove(0);
                    // force send packet
                    mc.getNetHandler().getNetworkManager().sendPacket(windowClick);
                    mc.thePlayer.openContainer.slotClick(windowClick.getSlotId(), windowClick.getUsedButton(), windowClick.getMode(), mc.thePlayer);
                    clicks++;
                } else {
                    clicks = 0;
                }
            }
        });

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S30PacketWindowItems) {
                    S30PacketWindowItems windowItems = (S30PacketWindowItems) event.getPacket();
                    queuedClicks.clear();

                    short clickNumber = 0;
                    for (int index = 0; index < windowItems.getItemStacks().length - 36; index++) {
                        ItemStack itemStack = windowItems.getItemStacks()[index];
                        if (itemStack != null && itemStack.getItem() instanceof ItemDye && itemStack.getMetadata() == 1) {
                            queuedClicks.add(new C0EPacketClickWindow(windowItems.func_148911_c(), index, 0, 0, itemStack, clickNumber++));
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                double targetWeight = Double.NEGATIVE_INFINITY;
                target = null;

                for (EntityPlayer p : (List<EntityPlayer>) mc.theWorld.playerEntities) {
                    if ((!p.equals(mc.thePlayer)) && isValidTarget(p)) {
                        if (target == null) {
                            target = p;
                            targetWeight = getTargetWeight(p);
                        } else if (getTargetWeight(p) > targetWeight) {
                            target = p;
                            targetWeight = getTargetWeight(p);
                        }
                    }
                }
                if (target != null) {
                    switchList.add(target);

                    if (time.hasReached(delay.getValue())) {
                        Rotation rotations = ProjectionUtilities.faceOffsetDeg(predict(target).subtract(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)));
                        event.setYaw((float) rotations.rotationYaw);
                        event.setPitch((float) rotations.rotationPitch);
                    }
                } else {
                    switchList.clear();
                }
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (mc.thePlayer.inventory.currentItem > 1) {
                    return;
                }
                if (time.hasReached(delay.getValue()) && target != null) {
                    time.reset();

                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                }
            }
        });
    }

    public double getTargetWeight(EntityPlayer p) {
        double weight;

        weight = -mc.thePlayer.getDistanceToEntity(p);
        if ((p.lastTickPosX == p.posX) && (p.lastTickPosY == p.posY) && (p.lastTickPosZ == p.posZ)) {
            weight += 6000.0D;
        }

        for (EntityPlayer player : switchList) {
            if (player == p) {
                weight -= 6000.0D;
            }
        }
        return weight;
    }

    public boolean isValidTarget(EntityPlayer p) {
        return (p.ticksExisted > 20)
                && (p.isEntityAlive())
                && (canPlayerSee(predict(p).up(p.getEyeHeight())))
                && (!p.isInvisible())
                && (isOnEnemyTeam(p))
                && (!Serenity.getInstance().getFriendManager().isFriend(p.getCommandSenderName()));
    }

    public boolean isOnEnemyTeam(EntityPlayer target) {
        boolean teamChecks = false;
        ChatColor myCol = null;
        ChatColor enemyCol = null;
        if (target != null) {
            for (ChatColor col : ChatColor.values()) {
                if (col == ChatColor.RESET)
                    continue;
                if (mc.thePlayer.getDisplayName().getFormattedText().contains(col.toString()) && myCol == null) {
                    myCol = col;
                }
                if (target.getDisplayName().getFormattedText().contains(col.toString()) && enemyCol == null) {
                    enemyCol = col;
                }
            }
            try {
                if (myCol != null && enemyCol != null) {
                    teamChecks = myCol != enemyCol;
                } else {
                    if (mc.thePlayer.getTeam() != null) {
                        teamChecks = !mc.thePlayer.isOnSameTeam(target);
                    } else {
                        if (mc.thePlayer.inventory.armorInventory[3].getItem() instanceof ItemBlock) {
                            teamChecks = !ItemStack.areItemStacksEqual(mc.thePlayer.inventory.armorInventory[3], target.inventory.armorInventory[3]);
                        }
                    }
                }
            } catch (Exception e) {}
        }

        return teamChecks;
    }

    private Vec3 predict(EntityPlayer player) {
        int pingTicks = (int) Math.ceil(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() / 50D) + 1;

        Vec3 predicted = Serenity.getInstance().getPredictionEngine().predictPlayerLocation(player, pingTicks);
        if (predicted != null)
            return predicted;

        return new Vec3(player.posX, player.posY, player.posZ);
    }

    private boolean canPlayerSee(Vec3 vec3) {
        return mc.theWorld.rayTraceBlocks(new net.minecraft.util.Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new net.minecraft.util.Vec3(vec3.x, vec3.y, vec3.z), false, true, false) == null;
    }
}
