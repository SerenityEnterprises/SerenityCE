package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.event.render.RenderWorld;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.synapse.Listener;
import net.minecraft.tileentity.*;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class StorageESP extends Module {
    // These are the colours in Energetic that were chosen to look like the specific TileEntity. Feel free to change any of them.
    private Color chestColor = new Color(255, 252, 99);
    private Color trappedChestColor = new Color(230, 89, 86);
    private Color enderChestColor = new Color(58, 141, 53);
    private Color dispenserColor = new Color(192, 192, 192);
    private Color furnaceColor = new Color(192, 192, 192);
    private Color hopperColor = new Color(167, 167, 167);

    private Color genericColor = new Color(30, 255, 40);

    @ModuleValue
    @ValueDescription("Renders a box around every chest.")
    private BooleanValue chest = new BooleanValue("Chest", true);

    @ModuleValue
    @ValueDescription("Renders a box around every trapped chest.")
    private BooleanValue trappedChest = new BooleanValue("Trapped Chest", true);

    @ModuleValue
    @ValueDescription("Renders a box around every ender chest.")
    private BooleanValue enderChest = new BooleanValue("Ender Chest", false);

    @ModuleValue
    @ValueDescription("Renders a box around every dispenser.")
    private BooleanValue dispenser = new BooleanValue("Dispenser", true);

    @ModuleValue
    @ValueDescription("Renders a box around every dropper.")
    private BooleanValue dropper = new BooleanValue("Dropper", true);

    @ModuleValue
    @ValueDescription("Renders a box around every hopper.")
    private BooleanValue hopper = new BooleanValue("Hopper", false);

    @ModuleValue
    @ValueDescription("Renders a box around every furnace.")
    private BooleanValue furnace = new BooleanValue("Furnace", false);

    public StorageESP() {
        super("Storage ESP", 0xFFA05C, ModuleCategory.RENDER);
        setHidden(true);

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

                mc.theWorld.loadedTileEntityList.stream().filter(o -> o instanceof TileEntity).filter(o -> shouldRender((TileEntity) o)).forEach(o -> {
                    TileEntity tileEntity = (TileEntity) o;
                    renderESP(tileEntity);
                });

                GL11.glTranslated(mc.getRenderManager().viewerPosX, mc.getRenderManager().viewerPosY, mc.getRenderManager().viewerPosZ);

                GL11.glDepthMask(true);
                GL11.glDisable(GL11.GL_LINE_SMOOTH);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glPopMatrix();
            }
        });
    }

    private void renderESP(TileEntity tileEntity) {
        // TODO: Improve
        if (tileEntity instanceof TileEntityEnderChest) {
            renderESP(tileEntity, enderChestColor);
        } else if (tileEntity instanceof TileEntityChest) {
            if (((TileEntityChest) tileEntity).getChestType() == 1) {
                renderESP(tileEntity, trappedChestColor);
            } else {
                renderESP(tileEntity, chestColor);
            }
        } else if (tileEntity instanceof TileEntityDispenser || tileEntity instanceof TileEntityDropper) {
            renderESP(tileEntity, dispenserColor);
        } else if (tileEntity instanceof TileEntityFurnace) {
            renderESP(tileEntity, furnaceColor);
        } else if (tileEntity instanceof TileEntityHopper) {
            renderESP(tileEntity, hopperColor);
        } else {
            renderESP(tileEntity, genericColor);
        }
    }
    private boolean shouldRender(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            if (((TileEntityChest) tileEntity).getChestType() == 1) {
                return this.trappedChest.getValue();
            } else {
                return this.chest.getValue();
            }
        }

        return (this.enderChest.getValue() && tileEntity instanceof TileEntityEnderChest) ||
                (this.dispenser.getValue() && tileEntity instanceof TileEntityDispenser) ||
                (this.dropper.getValue() && tileEntity instanceof TileEntityDropper) ||
                (this.hopper.getValue() && tileEntity instanceof TileEntityHopper) ||
                (this.furnace.getValue() && tileEntity instanceof TileEntityFurnace);
    }

    private void renderESP(TileEntity tileEntity, Color color) {
        double x = tileEntity.getPos().getX();
        double y = tileEntity.getPos().getY();
        double z = tileEntity.getPos().getZ();
        AxisAlignedBB boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

        double distance = mc.thePlayer.getDistance(x + 0.5, y + 0.5, z + 0.5);
        double alpha = Math.max(Math.min(0.25, distance / 128), 0.105);

        if (tileEntity instanceof TileEntityChest) {
            TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
            if (tileEntityChest.adjacentChestXPos != null) {
                boundingBox = boundingBox.expand(0.5, 0, 0).offset(0.5, 0, 0);
            } else if (tileEntityChest.adjacentChestZPos != null) {
                boundingBox = boundingBox.expand(0, 0, 0.5).offset(0, 0, 0.5);
            }
            boundingBox = boundingBox.contract(0.05, 0.05, 0.05).offset(0, -0.05, 0);
            if (tileEntityChest.adjacentChestZNeg == null && tileEntityChest.adjacentChestXNeg == null) {
                this.renderBoundingBoxInstant(color, alpha, boundingBox);
            }
        } else {
            this.renderBoundingBoxInstant(color, alpha, boundingBox);
        }
    }

    private void renderBoundingBoxInstant(Color color, double alpha, AxisAlignedBB boundingBox) {
        GL11.glColor4d(color.getRed() / 255D, color.getGreen() / 255D, color.getBlue() / 255D, alpha);
        RenderUtilities.drawBoundingBox(boundingBox);
    }
}
