package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.render.BlockShouldSideBeRendered;
import host.serenity.synapse.EventManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {
    @Redirect(method = "renderModelAmbientOcclusion", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    public boolean onOcclusionShouldSideBeRendered(Block block, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        boolean result = block.shouldSideBeRendered(worldIn, pos, side);
        return EventManager.post(new BlockShouldSideBeRendered(block, side, result)).getShouldSideBeRendered();
    }

    @Redirect(method = "renderModelStandard", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    public boolean onStandardShouldSideBeRendered(Block block, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
        boolean result = block.shouldSideBeRendered(worldIn, pos, side);
        return EventManager.post(new BlockShouldSideBeRendered(block, side, result)).getShouldSideBeRendered();
    }
}
