package host.serenity.serenity.modules.world;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.modules.world.util.nuker.NukerEngine;
import host.serenity.serenity.util.BlockHelper;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;

public class Smasher extends Module {
    public Smasher() {
        super("Smasher", 0xFCC6FF, ModuleCategory.WORLD);

        NukerEngine nukerEngine = new NukerEngine((pos, block) -> {
            boolean blockChecks = BlockHelper.canSeeBlock(pos.getX(), pos.getY(), pos.getZ())
                    && !(block instanceof BlockAir)
                    && !(block instanceof BlockLiquid);

            return blockChecks && block.getBlockHardness(mc.theWorld, pos) == 0;
        });

        listeners.addAll(nukerEngine.getListeners());
    }
}
