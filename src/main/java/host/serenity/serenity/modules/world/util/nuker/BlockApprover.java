package host.serenity.serenity.modules.world.util.nuker;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public interface BlockApprover {
    boolean approve(BlockPos pos, Block block);
}
