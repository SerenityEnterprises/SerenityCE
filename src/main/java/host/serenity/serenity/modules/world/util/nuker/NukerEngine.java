package host.serenity.serenity.modules.world.util.nuker;

import host.serenity.serenity.event.player.BlockDigging;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostBlockDigging;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.serenity.util.TimeHelper;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.*;

public class NukerEngine {
    private Set<Listener<?>> listeners = new HashSet<>();
    private final BlockApprover blockApprover;

    private float currentBlockDamage;

    private static class TargetData {
        BlockPos pos;
        EnumFacing facing;

        TargetData(BlockPos pos, EnumFacing facing) {
            this.pos = pos;
            this.facing = facing;
        }
    }

    private TargetData target = null;
    private BlockPos lastTargetPosition;

    private final TimeHelper time = new TimeHelper();

    public NukerEngine(BlockApprover blockApprover) {
        this.blockApprover = blockApprover;

        listeners.add(new Listener<PlayerWalkingUpdate>() {
            @Override
            public void call(PlayerWalkingUpdate event) {
                if (lastTargetPosition != null) {
                    if (target != null) {
                        if (!target.pos.equals(lastTargetPosition)) {
                            currentBlockDamage = 0;
                        }
                    } else {
                        currentBlockDamage = 0;
                    }
                }

                target = null;

                float radius = mc.playerController.getBlockReachDistance();
                int integerRadius = (int) Math.ceil(radius);

                List<BlockPos> possibleBlockPositions = new LinkedList<>();

                for (int y = integerRadius; y >= -integerRadius; y--) {
                    for (int x = -integerRadius; x <= integerRadius; x++) {
                        for (int z = -integerRadius; z <= integerRadius; z++) {
                            int posX = (int) Math.floor(mc.thePlayer.posX) + x;
                            int posY = (int) Math.floor(mc.thePlayer.posY) + y;
                            int posZ = (int) Math.floor(mc.thePlayer.posZ) + z;

                            if (mc.thePlayer.getDistanceSq(posX + 0.5, posY + 0.5 - mc.thePlayer.getEyeHeight(), posZ + 0.5) <= radius*radius) {
                                possibleBlockPositions.add(new BlockPos(posX, posY, posZ));
                            }
                        }
                    }
                }

                possibleBlockPositions.sort(new Comparator<BlockPos>() {
                    double distanceSqTo(BlockPos position) {
                        EntityPlayer player = mc.thePlayer;
                        double dx = Math.abs(player.posX - position.getX() - 0.5);
                        double dy = Math.abs(player.posY + player.getEyeHeight() - position.getY() - 0.5);
                        double dz = Math.abs(player.posZ - position.getZ() - 0.5);
                        return dx * dx + dy * dy + dz * dz;
                    }

                    @Override
                    public int compare(BlockPos blockPos1, BlockPos blockPos2) {
                        if (blockPos1 == blockPos2) {
                            return 0;
                        }
                        EntityPlayerSP player = mc.thePlayer;
                        int underPosY = (int) Math.floor(player.posY - 1);
                        double d1 = distanceSqTo(blockPos1);
                        double d2 = distanceSqTo(blockPos2);

                        boolean directlyBelow1 = Math.floor(player.posX) == blockPos1.getX() && underPosY == blockPos1.getY() && Math.floor(player.posZ) == blockPos1.getZ();
                        boolean directlyBelow2 = Math.floor(player.posX) == blockPos2.getX() && underPosY == blockPos2.getY() && Math.floor(player.posZ) == blockPos2.getZ();

                        boolean block1Above = blockPos1.getY() >= underPosY;
                        boolean block2Above = blockPos2.getY() >= underPosY;

                        if (block1Above && !block2Above) {
                            return -1;
                        }
                        if (block2Above && !block1Above) {
                            return 1;
                        }

                        if (directlyBelow1) {
                            return 1;
                        }
                        if (directlyBelow2) {
                            return -1;
                        }

                        if (d1 == d2) {
                            return 0;
                        } else if (d1 < d2) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                for (BlockPos pos : possibleBlockPositions) {
                    Block block = BlockHelper.getBlock(pos.getX(), pos.getY(), pos.getZ());
                    if (blockApprover.approve(pos, block)) {
                        EnumFacing facing = BlockHelper.getFacing(pos);
                        float[] angles = BlockHelper.getFacingRotations(pos.getX(), pos.getY(), pos.getZ(), facing);
                        event.setYaw(angles[0]);
                        event.setPitch(angles[1]);

                        target = new TargetData(pos, facing);
                        lastTargetPosition = target.pos;
                        return;
                    }
                }
            }
        });

        listeners.add(new Listener<PostPlayerWalkingUpdate>() {
            @Override
            public void call(PostPlayerWalkingUpdate event) {
                if (target != null && time.hasReached(50)) {
                    IBlockState blockState = mc.theWorld.getBlockState(target.pos);
                    float increment = mc.thePlayer.capabilities.isCreativeMode ? 1 : blockState.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, target.pos);

                    if (currentBlockDamage == 0) {
                        EventManager.post(new BlockDigging(target.pos, target.facing, 0));
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, target.pos, target.facing));
                        EventManager.post(new PostBlockDigging(target.pos, target.facing));

                        mc.thePlayer.swingItem();
                    }

                    currentBlockDamage += increment;

                    if (currentBlockDamage >= 1) {
                        EventManager.post(new BlockDigging(target.pos, target.facing, 0));
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, target.pos, target.facing));
                        EventManager.post(new PostBlockDigging(target.pos, target.facing));

                        time.reset();
                        currentBlockDamage = 0;
                        mc.thePlayer.swingItem();
                    }
                }
            }
        });
    }

    public Set<Listener<?>> getListeners() {
        return listeners;
    }
}
