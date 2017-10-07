package host.serenity.serenity.modules.miscellaneous;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.ModuleCategory;
import host.serenity.serenity.api.value.ModuleValue;
import host.serenity.serenity.api.value.StringValue;
import host.serenity.serenity.event.network.ReceivePacket;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import host.serenity.synapse.util.Cancellable;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.StringUtils;

public class AutoAccept extends Module {
    @ModuleValue
    public StringValue command = new StringValue("command", "/tpaccept $name");

    public AutoAccept() {
        super("Auto Accept", 0xB3FFAC, ModuleCategory.MISCELLANEOUS);

        listeners.add(new Listener<ReceivePacket>() {
            @Override
            public void call(ReceivePacket event) {
                if (event.getPacket() instanceof S02PacketChat) {
                    S02PacketChat packet = (S02PacketChat) event.getPacket();

                    Serenity.getInstance().getFriendManager().getFriends().keySet().stream()
                            .filter(friend -> StringUtils.stripControlCodes(packet.getChatComponent().getUnformattedText()).contains(friend + " has requested to teleport to you.") || StringUtils.stripControlCodes(packet.getChatComponent().getUnformattedText()).equalsIgnoreCase(friend + " has requested that you teleport to them."))
                            .filter(friend -> isValidTeleportMessage(StringUtils.stripControlCodes(packet.getChatComponent().getFormattedText()), false))
                            .forEach(friend -> {
                                if (!EventManager.post(new TeleportAcceptEvent(friend).isCancelled()))
                                    mc.thePlayer.sendChatMessage(command.getValue().replace("$name", friend));
                            });
                }
            }
        });
    }

    private boolean isValidTeleportMessage(String message, boolean reverse) {
        return !message.contains("-> me") && message.contains("has requested to teleport to you.") || (message.contains("has requested that you teleport to them.") && reverse);
    }

    public static class TeleportAcceptEvent extends Cancellable {
        private final String player;

        public TeleportAcceptEvent(String player) {
            this.player = player;
        }
    }
}
