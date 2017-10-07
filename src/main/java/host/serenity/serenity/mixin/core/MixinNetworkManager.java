package host.serenity.serenity.mixin.core;

import host.serenity.serenity.event.network.PushPacketToNetwork;
import host.serenity.synapse.EventManager;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class MixinNetworkManager {
    @Inject(method = "dispatchPacket", at = @At("HEAD"), cancellable = true)
    private void onDispatchPacket(final Packet inPacket, final GenericFutureListener[] futureListeners, CallbackInfo callbackInfo) {
        if (EventManager.post(new PushPacketToNetwork(inPacket)).isCancelled())
            callbackInfo.cancel();
    }
}
