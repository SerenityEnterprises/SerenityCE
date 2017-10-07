package host.serenity.serenity.api.binding.impl;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.binding.Keybinding;
import host.serenity.serenity.api.command.CommandSender;

public class CommandKeybinding extends Keybinding {
    private final String command;

    public CommandKeybinding(int key, String command) {
        super(key);

        this.command = command;
    }

    @Override
    public void updateState(boolean state) {
        if (this.command.length() > 1 && this.command.startsWith("+")) {
            String strippedCommand = this.command.substring(1);
            Serenity.getInstance().getCommandManager().executeCommand(CommandSender.KEYBIND, (state ? "+" : "-") + strippedCommand);
        } else {
            if (state) {
                Serenity.getInstance().getCommandManager().executeCommand(CommandSender.KEYBIND, this.command);
            }
        }
    }

    public String getCommand() {
        return command;
    }
}
