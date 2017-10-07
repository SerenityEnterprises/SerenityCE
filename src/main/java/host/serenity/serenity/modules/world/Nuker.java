package host.serenity.serenity.modules.world;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.DoubleValue;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.api.value.minecraft.BlockListValue;
import host.serenity.serenity.event.player.LeftClick;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.modules.world.util.nuker.NukerEngine;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.serenity.util.iface.PlayerControllerMPExtension;
import host.serenity.synapse.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.List;

public class Nuker extends Module {
    public Nuker() {
        super("Nuker", 0xFFC84D, ModuleCategory.WORLD);

        getModuleModes().add(new ModuleMode("Creative") {
            private DoubleValue radius = new DoubleValue("radius", 6, 0, 6);

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        if (!mc.playerController.isInCreativeMode()) {
                            setState(false);

                            Serenity.getInstance().addChatMessage("Nuker's creative mode can only be used in creative mode!");
                            return;
                        }

                        int radiusValue = (int) Math.ceil(radius.getValue());
                        for (int y = radiusValue; y >= -radiusValue; y--) {
                            for (int x = -radiusValue; x <= radiusValue; x++) {
                                for (int z = -radiusValue; z <= radiusValue; z++) {
                                    BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                                    if (mc.thePlayer.getDistanceSq(pos) >= radiusValue * radiusValue)
                                        continue;

                                    IBlockState state = mc.theWorld.getBlockState(pos);

                                    if (state != null && state.getBlock() != Blocks.air) {
                                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
                                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
                                    }
                                }
                            }
                        }
                    }
                });

                listeners.add(new Listener<RenderWorld>() {
                    @Override
                    public void call(RenderWorld event) {
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

                        GL11.glColor4d(1, 1, 1, 0.2);

                        int radiusValue = (int) Math.ceil(radius.getValue());
                        for (int y = radiusValue; y >= -radiusValue; y--) {
                            for (int x = -radiusValue; x <= radiusValue; x++) {
                                for (int z = -radiusValue; z <= radiusValue; z++) {
                                    BlockPos pos = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                                    if (mc.thePlayer.getDistanceSq(pos) >= radius.getValue() * radius.getValue())
                                        continue;

                                    IBlockState state = mc.theWorld.getBlockState(pos);
                                    if (state != null && state.getBlock() != Blocks.air) {
                                        AxisAlignedBB boundingBox = state.getBlock().getSelectedBoundingBox(mc.theWorld, pos);

                                        if (boundingBox != null)
                                            RenderUtilities.drawOutlinedBoundingBox(boundingBox);
                                    }
                                }
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
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[] { radius };
            }
        });

        getModuleModes().add(new ModuleMode("Survival") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                NukerEngine nukerEngine = new NukerEngine((pos, block) -> {
                    boolean blockChecks = BlockHelper.canSeeBlock(pos.getX(), pos.getY(), pos.getZ()) && !(block instanceof BlockAir) && !(block instanceof BlockLiquid);
                    blockChecks = blockChecks && (block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN) != -1.0F || mc.playerController.isInCreativeMode());

                    return blockChecks;
                });

                listeners.addAll(nukerEngine.getListeners());
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[0];
            }
        });

        getModuleModes().add(new ModuleMode("Selective") {
            private Block selected;

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                NukerEngine nukerEngine = new NukerEngine((pos, block) -> {
                    boolean blockChecks = BlockHelper.canSeeBlock(pos.getX(), pos.getY(), pos.getZ()) && !(block instanceof BlockAir) && !(block instanceof BlockLiquid);
                    blockChecks = blockChecks && (block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN) != -1.0F || mc.playerController.isInCreativeMode());

                    return blockChecks && block == selected;
                });

                listeners.addAll(nukerEngine.getListeners());

                listeners.add(new Listener<LeftClick>() {
                    @Override
                    public void call(LeftClick event) {
                        BlockPos currentBlock = ((PlayerControllerMPExtension) mc.playerController).getCurrentBlock();

                        selected = mc.theWorld.getBlockState(currentBlock).getBlock();
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[0];
            }
        });

        getModuleModes().add(new ModuleMode("Whitelist") {
            private BlockListValue blocks = new BlockListValue("blocks", Arrays.asList("minecraft:stone", "minecraft:cobblestone"));

            @Override
            public void addListeners(List<Listener<?>> listeners) {
                NukerEngine nukerEngine = new NukerEngine((pos, block) -> {
                    boolean blockChecks = BlockHelper.canSeeBlock(pos.getX(), pos.getY(), pos.getZ()) && !(block instanceof BlockAir) && !(block instanceof BlockLiquid);
                    blockChecks = blockChecks && (block.getBlockHardness(mc.theWorld, BlockPos.ORIGIN) != -1.0F || mc.playerController.isInCreativeMode());

                    return blockChecks && blocks.containsBlock(block);
                });

                listeners.addAll(nukerEngine.getListeners());
            }

            @Override
            public Value<?>[] getValues() {
                return new Value[] { blocks };
            }
        });

        setActiveMode("Survival");
    }
}
