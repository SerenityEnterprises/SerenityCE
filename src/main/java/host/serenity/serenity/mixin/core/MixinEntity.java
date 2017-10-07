package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.internal.HitboxSize;
import host.serenity.serenity.event.player.ShouldSafeWalk;
import host.serenity.synapse.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(method = "getCollisionBorderSize", at = @At("RETURN"), cancellable = true)
    public void onGetCollisionBorderSize(CallbackInfoReturnable<Float> callbackInfo) {
        callbackInfo.setReturnValue(EventManager.post(new HitboxSize(callbackInfo.getReturnValue())).getSize());
    }

    @Redirect(method = "moveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean onIsSneaking(Entity entity) {
        if (entity == Minecraft.getMinecraft().thePlayer)
            return EventManager.post(new ShouldSafeWalk(entity.isSneaking())).getShouldSafeWalk();

        return entity.isSneaking();
    }
}
