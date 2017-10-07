package host.serenity.serenity.plugins.gui.component.moduleconfig.value;

import host.serenity.serenity.api.gui.component.BaseComponent;
import host.serenity.serenity.api.value.minecraft.BlockListValue;
import host.serenity.serenity.util.RenderUtilities;
import host.serenity.serenity.util.math.DoubleWithVelocity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockListSelector extends BaseComponent {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final String label;
    private final BlockListValue value;
    private DoubleWithVelocity scrollValue = new DoubleWithVelocity(0);


    public BlockListSelector(String label, BlockListValue value) {
        super(96, 18 * 5 + 22);
        this.label = label;
        this.value = value;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        Block hoveredBlock = getHoveringOver(mouseX, mouseY);

        ttfRenderer.drawString(label, getX() + 2, getY() + 1);
        RenderUtilities.drawRect(getX(), getY() + 18, getX() + getWidth(), getY() + getHeight(), 0xFF232323);

        prepareScissorBox(getX(), getY() + 18, getX() + getWidth(), getY() + getHeight() - 2);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int x = 0;
        int y = 0;
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslated(0, -scrollValue.getValue(), 0);
        for (Block block : blocks) {
            try {
                if (x + 18 > getWidth()) {
                    x = 0;
                    y += 18;
                }

                /* if (isMouseWithin(getX() + x - 1, getY() + y + 17, getX() + x + 17, getY() + y + 35, mouseX, mouseY)) */
                boolean hovered = hoveredBlock == block;
                boolean selected = value.containsBlock(block);
                if (hovered || selected)
                    RenderUtilities.drawRect(getX() + 2 + x - 1, getY() + 20 + y - 1, getX() + 2 + x + 17, getY() + 20 + y + 17, hovered && selected ? 0xFF787878 : hovered ? 0xFF494949 : selected ? 0xFF363636 : 0);
                mc.getRenderItem().renderItemIntoGUI(new ItemStack(block), getX() + 2 + x, getY() + 20 + y);
                x += 18;
            } catch (Exception e) {}
        }
        GL11.glPopMatrix();

        scrollValue.update(partialTicks);

        double wheel = Math.signum(-Mouse.getDWheel()) * 2;
        if (isHovering(mouseX, mouseY)) {
            if (wheel > 0) {
                scrollValue.applyForce(wheel);
            } else if (wheel < 0) {
                scrollValue.applyForce(wheel);
            }
        }

        if (scrollValue.getValue() < 0) {
            scrollValue.setValue(0);
        }
        if (scrollValue.getValue() > y - getHeight() + 36) {
            scrollValue.setValue(y - getHeight() + 36);
        }
        RenderHelper.disableStandardItemLighting();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Block block = getHoveringOver(mouseX, mouseY);
        if (block == null) return;

        if (value.containsBlock(block)) {
            value.removeBlock(block);
        } else {
            value.addBlock(block);
        }
    }

    private boolean isMouseWithin(int minX, int minY, int maxX, int maxY, int mouseX, int mouseY) {
        return ((mouseX > minX) && (mouseX < maxX) && (mouseY > minY) && (mouseY < maxY));
    }

    private Block getHoveringOver(int mouseX, int mouseY) {
        int x = 0;
        int y = (int) -scrollValue.getValue();

        for (Block block : blocks) {
            try {
                if (x + 18 > getWidth()) {
                    x = 0;
                    y += 18;
                }
                if (y > -20 && y < getHeight() - 18) {
                    if (isMouseWithin(getX() + 2 + x - 1, getY() + 20 + y - 1, getX() + 2 + x + 17, getY() + 20 + y + 17, mouseX, mouseY)) {
                        return block;
                    }
                }
                x += 18;
            } catch (Exception e) {}
        }

        return null;
    }

    private void prepareScissorBox(float x, float y, float x2, float y2) {
        final int factor = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }

    private static List<String> illegalBlocks = Arrays.asList("minecraft:jungle_door",
            "minecraft:piston_extension",
            "minecraft:acacia_door",
            "minecraft:carrots",
            "minecraft:standing_sign",
            "minecraft:cocoa",
            "minecraft:double_stone_slab",
            "minecraft:redstone_wire",
            "minecraft:lava",
            "minecraft:unlit_redstone_torch",
            "minecraft:flower_pot",
            "minecraft:birch_door",
            "minecraft:piston_head",
            "minecraft:standing_banner",
            "minecraft:portal",
            "minecraft:iron_door",
            "minecraft:double_stone_slab2",
            "minecraft:unpowered_comparator",
            "minecraft:end_portal",
            "minecraft:wooden_door",
            "minecraft:wall_sign",
            "minecraft:water",
            "minecraft:flowing_water",
            "minecraft:brewing_stand",
            "minecraft:potatoes",
            "minecraft:melon_stem",
            "minecraft:skull",
            "minecraft:double_wooden_slab",
            "minecraft:lit_redstone_lamp",
            "minecraft:nether_wart",
            "minecraft:dark_oak_door",
            "minecraft:pumpkin_stem",
            "minecraft:wall_banner",
            "minecraft:cauldron",
            "minecraft:cake",
            "minecraft:powered_repeater",
            "minecraft:flowing_lava",
            "minecraft:spruce_door",
            "minecraft:tripwire",
            "minecraft:bed",
            "minecraft:powered_comparator",
            "minecraft:lit_redstone_ore",
            "minecraft:wheat",
            "minecraft:unpowered_repeater",
            "minecraft:reeds",
            "minecraft:daylight_detector_inverted");

    private static List<Block> blocks = new ArrayList<>(); static {
        for (Object key : Block.blockRegistry.getKeys()) {
            Object value = Block.blockRegistry.getObject(key);

            Block block = (Block) value;
            if (block == Blocks.air || block == Blocks.fire || illegalBlocks.contains(key.toString())) continue;

            blocks.add(block);
        }

        blocks.sort((b1, b2) -> Integer.compare(Block.getIdFromBlock(b1), Block.getIdFromBlock(b2)));
    }
}
