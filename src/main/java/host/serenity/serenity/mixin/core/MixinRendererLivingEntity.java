package host.serenity.serenity.mixin.core;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.event.render.PostRenderEntity;
import host.serenity.serenity.event.render.PreRenderEntity;
import host.serenity.serenity.modules.render.NameTags;
import host.serenity.synapse.EventManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {
    @Inject(method = "passSpecialRender", at = @At("HEAD"), cancellable = true)
    public void onPassSpecialRender(EntityLivingBase entitylivingbaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        if (entitylivingbaseIn instanceof EntityPlayer) {
            try {
                if (Serenity.getInstance().getModuleManager().getModule(NameTags.class).isEnabled())
                    callbackInfo.cancel();
            } catch (Exception ignored) {}
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"))
    public void preDoRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        EventManager.post(new PreRenderEntity(entity, partialTicks));
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    public void postDoRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        EventManager.post(new PostRenderEntity(entity, partialTicks));
    }
}
