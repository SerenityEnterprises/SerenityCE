package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.core.BlockBB;
import host.serenity.serenity.event.render.BlockAmbientLight;
import host.serenity.serenity.event.render.BlockShouldSideBeRendered;
import host.serenity.synapse.EventManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Block.class)
public abstract class MixinBlock {
    @Shadow
    public abstract AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state);

    @Shadow
    public abstract boolean isBlockNormalCube();

    @Overwrite
    public float getAmbientOcclusionLightValue() {
        return EventManager.post(new BlockAmbientLight((Block) (Object) this, this.isBlockNormalCube() ? 0.2F : 1.0F)).getAmbientLight();
    }

    @Inject(method = "shouldSideBeRendered", at = @At("RETURN"), cancellable = true)
    public void onShouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(EventManager.post(new BlockShouldSideBeRendered((Block) (Object) this, side, callbackInfo.getReturnValue())).getShouldSideBeRendered());
    }

    @Overwrite
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity) {
        AxisAlignedBB boundingBox = this.getCollisionBoundingBox(worldIn, pos, state);

        if (collidingEntity instanceof EntityPlayerSP)
            boundingBox = EventManager.post(new BlockBB((Block) (Object) this, state, pos.getX(), pos.getY(), pos.getZ(), boundingBox)).getBoundingBox();

        if (boundingBox != null && mask.intersectsWith(boundingBox)) {
            list.add(boundingBox);
        }
    }
}
