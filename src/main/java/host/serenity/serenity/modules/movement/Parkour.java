package host.serenity.serenity.modules.movement;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.serenity.event.player.LeftClick;
import host.serenity.serenity.event.player.MovePlayer;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.serenity.util.math.Vector3;
import host.serenity.serenity.util.math.VectorUtilities;
import host.serenity.synapse.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class Parkour extends Module {
    private Vector3 start = null;
    private Vector3 end = null;

    private State state = State.WALKING;
    private enum State {
        RESTING,
        WALKING,
        JUMPING
    }

    private boolean wasOnGround = false;

    private static final AxisAlignedBB INFINITY_BB = new AxisAlignedBB(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    private double speed;

    public Parkour() {
        super("Parkour", 0xDEFF97, ModuleCategory.MOVEMENT);

        listeners.add(new Listener<MovePlayer>() {
            @Override
            public void call(MovePlayer event) {
                double baseSpeed = mc.thePlayer.isSneaking() ? 0.3 : mc.thePlayer.getFoodStats().getFoodLevel() > 6 ? 0.283 : 0.23;

                if (mc.thePlayer.onGround && !wasOnGround) {
                    state = State.RESTING;
                }

                if (end != null) {
                    if (mc.thePlayer.posY < end.y - 1)
                        state = State.RESTING;
                }

                if (state == State.RESTING) {
                    start = null;
                    end = null;
                    wasOnGround = mc.thePlayer.onGround;
                    return;
                }

                Vector3 playerPosition = new Vector3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

                if (state == State.WALKING) {
                    if (start == null) {
                        state = State.RESTING;
                        wasOnGround = mc.thePlayer.onGround;
                        return;
                    }

                    Vector3 delta = start.subtract(playerPosition);
                    delta = flatten(delta);
                    double dist = delta.length();

                    baseSpeed = Math.min(baseSpeed, dist);

                    delta = delta.normalize(baseSpeed);

                    event.setX(delta.x);
                    event.setZ(delta.z);

                    if (dist <= 0.01 || mc.thePlayer.isCollidedHorizontally)
                        state = State.JUMPING;
                } else if (state == State.JUMPING) {
                    if (!wasOnGround && mc.thePlayer.onGround) {
                        wasOnGround = mc.thePlayer.onGround;
                        state = State.RESTING;
                        return;
                    }

                    Vector3 rotations = VectorUtilities.faceOffset(end.subtract(playerPosition));
                    rotations = new Vector3(rotations.getRotationYaw(), 0);

                    float yaw = (float) rotations.getRotationYaw();

                    if (mc.thePlayer.onGround) {
                        speed = 0.4764960967713995;

                        event.setY(mc.thePlayer.motionY = 0.42);
                    } else {
                        if (wasOnGround) {
                            speed = speed - (0.66 * (speed - 0.28));
                        } else {
                            speed = speed - (speed / (160 - 1));
                        }
                    }

                    speed = Math.max(speed, baseSpeed);

                    double endDistance = flatten(end.subtract(playerPosition)).length();

                    double moveSpeed = Math.min(speed, endDistance);
                    if (endDistance == 0) {
                        state = State.RESTING;

                        wasOnGround = mc.thePlayer.onGround;
                        return;
                    }

                    event.setX(-(Math.sin(Math.toRadians(rotations.getRotationYaw())) * moveSpeed));
                    event.setZ(Math.cos(Math.toRadians(rotations.getRotationYaw())) * moveSpeed);
                }
                wasOnGround = mc.thePlayer.onGround;
            }
        });

        listeners.add(new Listener<LeftClick>() {
            @Override
            public void call(LeftClick event) {
                if (!mc.thePlayer.onGround)
                    return;

                Vec3 eyePos = mc.thePlayer.getPositionEyes(0);
                Vec3 lookVec = mc.thePlayer.getLook(1);
                Vec3 endPos = eyePos.addVector(lookVec.xCoord * 999, lookVec.yCoord * 999, lookVec.zCoord * 999);

                MovingObjectPosition result = mc.theWorld.rayTraceBlocks(eyePos, endPos, false, true, false);

                if (result == null || result.getBlockPos() == null)
                    return;

                wasOnGround = true;
                plotPath(result.getBlockPos());
            }
        });

        listeners.add(new Listener<RenderWorld>() {
            @Override
            public void call(RenderWorld event) {
                if (!mc.thePlayer.onGround)
                    return;

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LINE_SMOOTH);
                GL11.glDepthMask(false);
                GL11.glLineWidth(1.0F);

                GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);

                GL11.glColor4d(54 / 255D, 224 / 255D, 79 / 255D, 0.3);

                Vec3 eyePos = mc.thePlayer.getPositionEyes(0);
                Vec3 lookVec = mc.thePlayer.getLook(1);
                Vec3 endPos = eyePos.addVector(lookVec.xCoord * 999, lookVec.yCoord * 999, lookVec.zCoord * 999);

                MovingObjectPosition result = mc.theWorld.rayTraceBlocks(eyePos, endPos, false, true, false);

                if (result != null) {
                    if (result.getBlockPos() != null) {
                        BlockPos pos = result.getBlockPos();

                        RenderUtilities.drawBoundingBox(mc.theWorld.getBlockState(pos).getBlock().getSelectedBoundingBox(mc.theWorld, pos));
                    }
                }

                GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);

                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glPopMatrix();

                GL11.glColor3f(1, 1, 1);
            }
        });

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                    state = State.RESTING;
                }
            }
        });
    }

    @Override
    protected void onEnable() {
        state = State.RESTING;
    }

    private void plotPath(BlockPos pos) {
        Set<Vector3> startPoints = new HashSet<>();
        for (BlockPos floorPos : getFloor()) {
            startPoints.addAll(getCorners(floorPos, true));
        }

        Set<Vector3> endPoints = getCorners(pos, false);

        double bestDistance = Double.POSITIVE_INFINITY;

        for (Vector3 start : startPoints) {
            for (Vector3 end : endPoints) {
                double dist = flatten(end.subtract(start)).length();
                double vDist = end.y - start.y;

                if (dist > bestDistance)
                    continue;

                if (vDist > 1.2)
                    continue;

                bestDistance = dist;
                this.end = end;
                this.start = start;
            }
        }

        if (this.start == null)
            return;

        state = State.WALKING;
    }

    private BlockPos[] getFloor() {
        Set<BlockPos> positions = new HashSet<>();
        int highestY = -1;

        for (double d = 0; d <= mc.thePlayer.posY; d += 0.5) {
            for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX - 0.5); x < MathHelper
                    .floor_double(mc.thePlayer.getEntityBoundingBox().maxX + 0.5) + 1; x++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ - 0.5); z < MathHelper
                        .floor_double(mc.thePlayer.getEntityBoundingBox().maxZ + 0.5) + 1; z++) {
                    BlockPos pos = new BlockPos(x, mc.thePlayer.posY - d, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    Block block = state.getBlock();

                    List<AxisAlignedBB> boundingBoxes = new ArrayList<>();
                    state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, INFINITY_BB, boundingBoxes, mc.thePlayer);

                    if (!boundingBoxes.isEmpty() && !(block instanceof BlockLiquid) && !(block instanceof BlockAir)) {
                        if (pos.getY() == highestY) {
                            positions.add(pos);
                        }

                        if (pos.getY() > highestY) {
                            positions.clear();
                            highestY = pos.getY();
                        }
                    }
                }
            }
        }

        return positions.toArray(new BlockPos[positions.size()]);
    }

    private Set<Vector3> getCorners(BlockPos pos, boolean start) {
        IBlockState state = mc.theWorld.getBlockState(pos);
        if (state == null || state.getBlock() == null || state.getBlock() == Blocks.air)
            return new HashSet<>();

        List<AxisAlignedBB> boundingBoxes = new ArrayList<>();
        state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, INFINITY_BB, boundingBoxes, mc.thePlayer);

        Set<Vector3> positions = new HashSet<>();
        boundingBoxes.stream().map(bb -> getCorners(state.getBlock(), bb, start)).map(Arrays::asList).forEach(positions::addAll);

        return positions;
    }

    private Vector3[] getCorners(Block block, AxisAlignedBB boundingBox, boolean start) {
        if (boundingBox == null)
            return new Vector3[0];

        if (start)
            boundingBox = boundingBox.expand(0.299, 0, 0.299);

        // Return the center for slime block ending positions
        if (block == Blocks.slime_block && !start) {
            return new Vector3[] {
                    new Vector3(boundingBox.minX + (boundingBox.maxX - boundingBox.minX) / 2.0,
                            boundingBox.maxY,
                            boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) / 2.0),
            };
        }

        if (block == Blocks.ladder) {
            double xDiff = boundingBox.maxX - boundingBox.minX;
            double zDiff = boundingBox.maxZ - boundingBox.minZ;

            if (xDiff > zDiff) {

            } else if (zDiff > xDiff) {

            }
        }

        return new Vector3[] {
                new Vector3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vector3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vector3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vector3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),

                new Vector3(boundingBox.minX + (boundingBox.maxX - boundingBox.minX) / 2.0, boundingBox.maxY, boundingBox.minZ),
                new Vector3(boundingBox.minX + (boundingBox.maxX - boundingBox.minX) / 2.0, boundingBox.maxY, boundingBox.maxZ),

                new Vector3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) / 2.0),
                new Vector3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) / 2.0)
        };
    }

    private static Vector3 flatten(Vector3 v) {
        return new Vector3(v.x, 0, v.z);
    }
}