package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.player.BlockDigging;
import host.serenity.serenity.event.player.PlayerReach;
import host.serenity.serenity.event.player.PostBlockDigging;
import host.serenity.serenity.util.iface.PlayerControllerMPExtension;
import host.serenity.synapse.EventManager;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP implements PlayerControllerMPExtension {
    @Shadow
    private WorldSettings.GameType currentGameType;

    @Inject(method = "getBlockReachDistance()F", at = @At("RETURN"), cancellable = true)
    public void onGetBlockReachDistance(CallbackInfoReturnable<Float> callbackInfo) {
        callbackInfo.setReturnValue(EventManager.post(new PlayerReach(currentGameType.isCreative() ? 5.0F : 4.5F)).getReach());
    }

    @Shadow
    private int blockHitDelay;

    @Shadow
    private float curBlockDamageMP;

    @Shadow
    private BlockPos currentBlock;

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"))
    public void onOnPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> callbackInfo) {
        blockHitDelay = EventManager.post(new BlockDigging(posBlock, directionFacing, blockHitDelay)).getHitDelay();
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("RETURN"))
    public void postOnPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> callbackInfo) {
        EventManager.post(new PostBlockDigging(posBlock, directionFacing));
    }

    @Override
    public int getBlockHitDelay() {
        return blockHitDelay;
    }

    @Override
    public void setBlockHitDelay(int blockHitDelay) {
        this.blockHitDelay = blockHitDelay;
    }

    @Override
    public float getCurBlockDamageMP() {
        return curBlockDamageMP;
    }

    @Override
    public void setCurBlockDamageMP(float curBlockDamageMP) {
        this.curBlockDamageMP = curBlockDamageMP;
    }

    @Override
    public BlockPos getCurrentBlock() {
        return currentBlock;
    }
}
