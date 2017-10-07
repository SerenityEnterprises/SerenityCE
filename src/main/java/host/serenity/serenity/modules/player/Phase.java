package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.core.BlockBB;
import host.serenity.serenity.event.player.InsideOpaqueBlock;
import host.serenity.serenity.event.player.PushOutOfBlocks;
import host.serenity.serenity.event.render.Culling;
import host.serenity.serenity.util.BlockHelper;
import host.serenity.synapse.Listener;

import java.util.List;

public class Phase extends Module {
    public Phase() {
        super("Phase", 0xFF7DFB, ModuleCategory.PLAYER);

        listeners.add(new Listener<InsideOpaqueBlock>() {
            @Override
            public void call(InsideOpaqueBlock event) {
                event.setInsideOpaqueBlock(false);
            }
        });

        listeners.add(new Listener<PushOutOfBlocks>() {
            @Override
            public void call(PushOutOfBlocks event) {
                event.setCancelled(true);
            }
        });

        listeners.add(new Listener<Culling>() {
            @Override
            public void call(Culling event) {
                event.setCancelled(true);
            }
        });

        registerMode(new ModuleMode("Vanilla") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<BlockBB>() {
                    @Override
                    public void call(BlockBB event) {
                        if (event.getY() >= mc.thePlayer.posY || (mc.thePlayer.isSneaking() && BlockHelper.isInsideBlock()))
                            event.setBoundingBox(null);

                        /* if (event.getBoundingBox() != null) {
                            AxisAlignedBB axisAlignedBB = event.getBoundingBox();
                            if (axisAlignedBB.maxY > mc.thePlayer.posY && axisAlignedBB.minY < mc.thePlayer.posY) {
                                event.setBoundingBox(new AxisAlignedBB(
                                        axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,
                                        axisAlignedBB.maxX, Math.floor(mc.thePlayer.posY) + mc.thePlayer.posY % 0.5, axisAlignedBB.maxZ
                                ));
                            }
                        } */
                    }
                });
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });
    }

    @Override
    protected void onEnable() {
        mc.thePlayer.stepHeight = 0;
    }

    @Override
    protected void onDisable() {
        mc.thePlayer.stepHeight = 0.5F;
    }
}
