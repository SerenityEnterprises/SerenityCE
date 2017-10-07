package host.serenity.serenity.modules.render;

import host.serenity.serenity.api.help.ValueDescription;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.BooleanValue;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.api.value.minecraft.BlockListValue;
import host.serenity.serenity.event.player.InsideOpaqueBlock;
import host.serenity.serenity.event.render.BlockAmbientLight;
import host.serenity.serenity.event.render.BlockLayer;
import host.serenity.serenity.event.render.BlockShouldSideBeRendered;
import host.serenity.serenity.event.render.Culling;
import host.serenity.synapse.Listener;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class Wallhack extends Module {
    @ModuleValue
    @ValueDescription("The blocks which wallhack will display.")
    private BlockListValue blocks = new BlockListValue("Blocks", Arrays.asList("minecraft:gold_ore",
            "minecraft:iron_ore",
            "minecraft:coal_ore",
            "minecraft:lapis_ore",
            "minecraft:diamond_ore",
            "minecraft:redstone_ore",
            "minecraft:lit_redstone_ore",
            "minecraft:tnt",
            "minecraft:emerald_ore",
            "minecraft:furnace",
            "minecraft:lit_furnace",
            "minecraft:diamond_block",
            "minecraft:iron_block",
            "minecraft:gold_block",
            "minecraft:quartz_ore",
            "minecraft:beacon",
            "minecraft:mob_spawner"));

    @ModuleValue
    public BooleanValue opacity = new BooleanValue("Opacity", true);

    public Wallhack() {
        super("Wallhack", 0xAFAFAF, ModuleCategory.RENDER);
        registerToggleKeybinding(Keyboard.KEY_X);

        listeners.add(new Listener<Culling>() {
            @Override
            public void call(Culling event) {
                event.setCancelled(true);
            }
        });

        listeners.add(new Listener<InsideOpaqueBlock>() {
            @Override
            public void call(InsideOpaqueBlock event) {
                event.setInsideOpaqueBlock(false);
            }
        });

        listeners.add(new Listener<BlockLayer>(opacity::getValue) {
            @Override
            public void call(BlockLayer event) {
                if (!blocks.containsBlock(event.getBlock()))
                    event.setLayer(EnumWorldBlockLayer.TRANSLUCENT);
            }
        });

        listeners.add(new Listener<BlockAmbientLight>() {
            @Override
            public void call(BlockAmbientLight event) {
                event.setAmbientLight(1);
            }
        });

        listeners.add(new Listener<BlockShouldSideBeRendered>() {
            @Override
            public void call(BlockShouldSideBeRendered event) {
                if (!opacity.getValue()) {
                    event.setShouldSideBeRendered(false);
                }

                if (blocks.containsBlock(event.getBlock())) {
                    event.setShouldSideBeRendered(true);
                }
            }
        });
    }

    @Override
    protected void onEnable() {
        mc.renderChunksMany = false;
        reloadWorld();
    }

    @Override
    protected void onDisable() {
        mc.renderChunksMany = false;
        reloadWorld();
    }

    private void reloadWorld() {
        int range = 250;
        int x = (int) mc.thePlayer.posX;
        int y = (int) mc.thePlayer.posY;
        int z = (int) mc.thePlayer.posZ;
        mc.theWorld.markBlockRangeForRenderUpdate(x - range, y - range, z - range, x + range, y + range, z + range);
    }
}
