package host.serenity.serenity.mixin.core;

import host.serenity.serenity.util.iface.EntityLivingBaseExtension;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase implements EntityLivingBaseExtension {
    @Shadow
    private int jumpTicks;

    @Override
    public void setJumpTicks(int jumpTicks) {
        this.jumpTicks = jumpTicks;
    }
}
