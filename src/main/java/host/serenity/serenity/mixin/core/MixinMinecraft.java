package host.serenity.serenity.mixin.core;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.event.core.RunTick;
import host.serenity.serenity.event.internal.KeyEvent;
import host.serenity.serenity.event.player.LeftClick;
import host.serenity.serenity.util.ShutdownListenerThread;
import host.serenity.serenity.util.iface.MinecraftExtension;
import host.serenity.synapse.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements MinecraftExtension {
    @Shadow @Final
    private Timer timer;

    @Inject(method = "startGame()V", at = @At("RETURN"))
    public void postInit(CallbackInfo callbackInfo) {
        new Serenity();

        Runtime.getRuntime().addShutdownHook(new ShutdownListenerThread()); // We cannot use anonymous classes in Mixins :(
    }

    @Inject(method = "runTick()V", at = @At("HEAD"))
    public void onRunTick(CallbackInfo callbackInfo) {
        EventManager.post(new RunTick());
    }

    @Inject(method = "runTick", at = @At(
            value = "INVOKE",
            remap = false,
            target = "Lorg/lwjgl/input/Keyboard;getEventKey()I",
            ordinal = 0,
            shift = At.Shift.BEFORE))
    public void runTickKeyboard(CallbackInfo callback) throws IOException {
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (key != Keyboard.KEY_NONE)
            EventManager.post(new KeyEvent(key, state));
    }

    @Inject(method = "clickMouse", at = @At("RETURN"))
    public void onClickMouse(CallbackInfo callbackInfo) {
        EventManager.post(new LeftClick());
    }

    @Shadow
    private int rightClickDelayTimer;

    @Override
    public int getRightClickDelayTimer() {
        return rightClickDelayTimer;
    }

    @Override
    public void setRightClickDelayTimer(int rightClickDelayTimer) {
        this.rightClickDelayTimer = rightClickDelayTimer;
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Shadow @Final @Mutable
    private Session session;

    @Override
    public void setSession(Session session) {
        this.session = session;
    }
}
