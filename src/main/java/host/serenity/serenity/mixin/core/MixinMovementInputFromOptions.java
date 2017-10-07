package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.player.MoveInput;
import host.serenity.synapse.EventManager;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public abstract class MixinMovementInputFromOptions extends MovementInput {
    @Inject(method = "updatePlayerMoveState()V", at = @At("RETURN"))
    public void onUpdatePlayerMoveState(CallbackInfo callbackInfo) {
        try {
            EventManager.post(new MoveInput(this));
        } catch (Exception ignored) {}
    }
}
