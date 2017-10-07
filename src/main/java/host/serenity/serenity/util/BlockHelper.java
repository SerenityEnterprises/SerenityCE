package host.serenity.serenity.util;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.isNaN;

public class BlockHelper {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static float[] getRotationsForPosition(double x, double y, double z) {
        return getRotationsForPosition(x, y, z, mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    public static float[] getRotationsForPosition(double x, double y, double z, double sourceX, double sourceY, double sourceZ) {
        double deltaX = x - sourceX;
        double deltaY = y - sourceY;
        double deltaZ = z - sourceZ;

        double yawToEntity;

        if (deltaZ < 0 && deltaX < 0) { // quadrant 3
            yawToEntity = 90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // forward
        } else if (deltaZ < 0 && deltaX > 0) { // quadrant 4
            yawToEntity = -90D + Math.toDegrees(Math.atan(deltaZ / deltaX)); // 90
            // degrees
            // back
        } else { // quadrants one or two
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }

        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ
                * deltaZ);

        double pitchToEntity = -Math.toDegrees(Math.atan(deltaY / distanceXZ));

        yawToEntity = wrapAngleTo180((float) yawToEntity);
        pitchToEntity = wrapAngleTo180((float) pitchToEntity);

        yawToEntity = isNaN(yawToEntity) ? 0 : yawToEntity;
        pitchToEntity = isNaN(pitchToEntity) ? 0 : pitchToEntity;

        return new float[] { (float) yawToEntity, (float) pitchToEntity };
    }

    public static float[] getBlockRotations(int x, int y, int z) {
        return getRotationsForPosition(x + 0.5, y + 0.5, z + 0.5);
    }

    public static float[] getFacingRotations(int x, int y, int z, EnumFacing facing) {
        return getFacingRotations(x, y, z, facing, 1);
    }

    public static float[] getFacingRotations(int x, int y, int z, EnumFacing facing, double width) {
        return getRotationsForPosition(x + 0.5 + facing.getDirectionVec().getX() * width / 2.0, y + 0.5 + facing.getDirectionVec().getY() * width / 2.0, z + 0.5 + facing.getDirectionVec().getZ() * width / 2.0);
    }

    private static float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }
        while (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    public static boolean canSeeBlock(int x, int y, int z) {
        return getFacing(new BlockPos(x, y, z)) != null;
    }
    public static Block getBlock(int x, int y, int z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public static Block getBlock(double x, double y, double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    // this is darkmagician's. credits to him.
    public static boolean isInLiquid() {
        boolean inLiquid = false;
        final int y = (int) (mc.thePlayer.getEntityBoundingBox().minY + 0.00011);
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                final Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid))
                        return false;
                    inLiquid = true;
                }
            }
        }
        return inLiquid;
    }

    public static boolean isOnIce() {
        boolean onIce = false;
        final int y = (int) mc.thePlayer.getEntityBoundingBox().offset(0.0D, -0.1D,
                0.0D).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                final Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir)) {
                    if (block instanceof BlockPackedIce
                            || block instanceof BlockIce) {
                        onIce = true;
                    }
                }
            }
        }
        return onIce;
    }

    public static boolean isOnFloor(double yOffset) {
        boolean onGround = false;
        final int y = (int) mc.thePlayer.getEntityBoundingBox().offset(0.0D, yOffset,
                0.0D).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                final Block block = getBlock(x, y, z);
                AxisAlignedBB boundingBox = block
                        .getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z),
                                Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)));

                List<AxisAlignedBB> boundingBoxList = new ArrayList<>();
                block.addCollisionBoxesToList(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z)), mc.thePlayer.getEntityBoundingBox().offset(0, yOffset, 0).contract(0.625, 0, 0.625), boundingBoxList, mc.thePlayer);

                if (!(block instanceof BlockAir)) {
                    if (block.isCollidable() && !boundingBoxList.isEmpty()) {
                        onGround = true;
                    }
                }
            }
        }
        return onGround;
    }

    public static BlockPos getFloor() {
        final AxisAlignedBB INFINITY_BB = new AxisAlignedBB(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

        BlockPos highestPos = null;

        for (int i = 0; i <= Math.ceil(mc.thePlayer.posY); i++) {
            for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                        .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    BlockPos pos = new BlockPos(x, mc.thePlayer.posY - 1 - i, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);

                    List<AxisAlignedBB> boundingBoxes = new ArrayList<>();
                    state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, INFINITY_BB, boundingBoxes, mc.thePlayer);

                    if (!boundingBoxes.isEmpty()) {
                        if (highestPos == null)
                            highestPos = pos;
                        if (pos.getY() > highestPos.getY())
                            highestPos = pos;
                    }
                }
            }
        }

        return highestPos;
    }

    public static boolean isOnLadder() {
        boolean onLadder = false;
        final int y = (int) mc.thePlayer.getEntityBoundingBox().offset(0.0D, 1.0D,
                0.0D).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                final Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLadder))
                        return false;
                    onLadder = true;
                }
            }
        }
        return onLadder || mc.thePlayer.isOnLadder();
    }
    // this method is N3xuz_DK's I believe. credits to him.
    public static boolean isOnLiquid() {
        boolean onLiquid = false;
        final int y = (int) mc.thePlayer.getEntityBoundingBox().offset(0.0D,
                -0.01D, 0.0D).minY;
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                final Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir)) {
                    if (!(block instanceof BlockLiquid))
                        return false;
                    onLiquid = true;
                }
            }
        }
        return onLiquid;
    }

    /* public static EnumFacing getFacing(BlockPos position) {
        Vec3d playerPosition = new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        RayTraceResult result = mc.theWorld.rayTraceBlocks(playerPosition, new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5), false, false, true);

        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
            return result.sideHit;
        }

        return null;
    } */

    public static EnumFacing getFacing(BlockPos pos) {
        return getFacing(pos, 1);
    }

    public static EnumFacing getFacing(BlockPos pos, double width) {
        EnumFacing[] orderedValues = new EnumFacing[] {
                EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN
        };
        for (EnumFacing facing : orderedValues) {
            MovingObjectPosition objectHit = mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                    new Vec3(pos.getX() + 0.5 + facing.getDirectionVec().getX() * width / 2,
                            pos.getY() + 0.5 + facing.getDirectionVec().getY() * width / 2,
                            pos.getZ() + 0.5 + facing.getDirectionVec().getZ() * width / 2), false, true, false);
            if (objectHit == null || (objectHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && objectHit.getBlockPos().equals(pos))) {
                return facing;
            }
        }
        return null;
    }

    public static boolean isInsideBlock() {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY); y < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper
                        .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    final Block block = Minecraft.getMinecraft().theWorld
                            .getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block == null || block instanceof BlockAir) {
                        continue;
                    }

                    AxisAlignedBB boundingBox = block
                            .getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z),
                                    Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)));
                    if (block instanceof BlockHopper) {
                        boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
                    }
                    if (boundingBox != null
                            && mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox))
                        return true;
                }
            }
        }
        return false;
    }
}
