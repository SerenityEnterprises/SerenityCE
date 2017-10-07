package host.serenity.serenity.api.command;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.help.GenericDescription;
import host.serenity.serenity.event.player.SendChat;
import host.serenity.serenity.util.StringUtilities;
import host.serenity.synapse.EventManager;
import host.serenity.synapse.Listener;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@GenericDescription("Commands can be executed by typing a dot ('.'), followed by the command name in chat. " +
        "Separate arguments by spaces, " +
        "if you need to pass an argument with spaces in, wrap it in \"double quotes\". " +
        "If you type two dots, a single dot will be typed into chat. You can even put a message after the two dots.")
public class CommandManager {
    private List<Command> commands = new ArrayList<>();

    public CommandManager() {
        EventManager.register(new Listener<SendChat>() {
            @Override
            public void call(SendChat event) {
                if (event.getMessage().startsWith(".")) {
                    event.setCancelled(true);
                    if (event.getMessage().startsWith("..")) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C01PacketChatMessage(event.getMessage().substring(1)));
                        return;
                    }

                    if (event.getMessage().length() > 1)
                        executeCommand(CommandSender.CHAT, event.getMessage().substring(1));
                }
            }
        });
    }
    public void executeCommand(CommandSender sender, String commandString) {
        String[] split = StringUtilities.splitExceptingQuotes(commandString, true);
        String cmd = split[0];

        String[] args;
        if (split.length == 1) {
            args = new String[0];
        } else {
            List<String> argsList = new ArrayList<>(Arrays.asList(split));
            argsList.remove(0);
            args = argsList.toArray(new String[argsList.size()]);
        }


        try {
            for (Command command : commands) {
                if (command.getCommandName().equalsIgnoreCase(cmd)) {
                    command.execute(sender, args);
                    return;
                }
            }
        } catch (Exception e) {
            try {
                Serenity.getInstance().addChatMessage(String.format("An exception of type '%s' was thrown while trying to execute your command.", e.getClass().getSimpleName()));
            } catch (Exception e2) {}

            e.printStackTrace();
            return;
        }

        Serenity.getInstance().addChatMessage(String.format("The command '%s' was not found.", cmd));

    }
    public List<Command> getCommands() {
        return commands;
    }
}
