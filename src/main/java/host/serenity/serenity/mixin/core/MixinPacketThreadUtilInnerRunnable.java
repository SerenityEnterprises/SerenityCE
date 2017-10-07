package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.network.PostReceivePacket;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.synapse.EventManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/PacketThreadUtil$1")
public abstract class MixinPacketThreadUtilInnerRunnable {
    @Shadow(aliases = { "val$p_180031_0_" }) @Final
    private Packet a;

    @Inject(method = "run()V", at = @At("HEAD"), cancellable = true)
    public void onRun(CallbackInfo callbackInfo) {
        if (EventManager.post(new ReceivePacket(a)).isCancelled())
            callbackInfo.cancel();
    }

    @Inject(method = "run()V", at = @At("RETURN"))
    public void afterRun(CallbackInfo callbackInfo) {
        EventManager.post(new PostReceivePacket(a));
    }
}
