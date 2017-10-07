package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.render.BlockLayer;
import host.serenity.synapse.EventManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumWorldBlockLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderChunk.class)
public abstract class MixinRenderChunk {
    @Redirect(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getBlockLayer()Lnet/minecraft/util/EnumWorldBlockLayer;"))
    public EnumWorldBlockLayer onGetBlockLayer(Block block) {
        return EventManager.post(new BlockLayer(block, block.getBlockLayer())).getLayer();
    }
}
