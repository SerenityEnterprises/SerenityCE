package host.serenity.serenity.modules.player;

import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.Value;
import host.serenity.serenity.event.network.SendPacket;
import host.serenity.serenity.event.player.PlayerWalkingUpdate;
import host.serenity.serenity.event.player.PostPlayerWalkingUpdate;
import host.serenity.synapse.Listener;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.lwjgl.input.Keyboard;

import java.util.List;

/**
 * Created by Jordin on 3/25/2017.
 * Jordin is still best hacker.
 */
public class Sneak extends Module {
    private boolean sneaking;

    private Listener<SendPacket> preventStopSneaking;

    public Sneak() {
        super("Sneak", 0x30AA30, ModuleCategory.PLAYER);

        preventStopSneaking = new Listener<SendPacket>() {
            @Override
            public void call(SendPacket event) {
                if (event.getPacket() instanceof C0BPacketEntityAction) {
                    C0BPacketEntityAction.Action action = ((C0BPacketEntityAction) event.getPacket()).getAction();
                    if ((action == C0BPacketEntityAction.Action.START_SNEAKING) || (action == C0BPacketEntityAction.Action.STOP_SNEAKING)) {
                        event.setCancelled(true);
                    }
                }
            }
        };

        getModuleModes().add(new ModuleMode("Packet") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
                        }
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                        sneaking = false;
                    }
                });

                listeners.add(new Listener<PostPlayerWalkingUpdate>() {
                    @Override
                    public void call(PostPlayerWalkingUpdate event) {
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                        sneaking = true;
                    }
                });

                listeners.add(preventStopSneaking);
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        getModuleModes().add(new ModuleMode("Shift") {
            @Override
            public void addListeners(List<Listener<?>> listeners) {
                listeners.add(new Listener<PlayerWalkingUpdate>() {
                    @Override
                    public void call(PlayerWalkingUpdate event) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        sneaking = true;
                    }
                });

                listeners.add(preventStopSneaking);
            }

            @Override
            public Value<?>[] getValues() {
                return new Value<?>[0];
            }
        });

        setActiveMode("Packet");
    }

    @Override
    protected void onDisable() {
        if (!Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            if (this.sneaking) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                this.sneaking = false;
            }
        }

        this.sneaking = false;
    }
}
