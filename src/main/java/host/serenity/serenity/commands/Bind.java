package host.serenity.serenity.commands;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.binding.Keybinding;
import host.serenity.serenity.api.binding.impl.CommandKeybinding;
import host.serenity.serenity.api.binding.impl.ModuleKeybinding;
import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.KeyArgument;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.command.ModuleCommand;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class Bind extends Command {
    public Bind() {
        branches.add(new CommandBranch("add", ctx -> {
            String commandToBind = ctx.getArgumentValue("command");
            int keyToBind = ctx.getArgumentValue("key");

            for (Command command : Serenity.getInstance().getCommandManager().getCommands()) {
                if (command instanceof ModuleCommand) {
                    if (command.getCommandName().equalsIgnoreCase(commandToBind)) {
                        Module module = ((ModuleCommand) command).getModule();
                        Serenity.getInstance().getKeybindManager().register(new ModuleKeybinding(module, keyToBind, ModuleKeybinding.Type.TOGGLE));
                        out("Added a keybinding for the key '%s' to the module '%s'", Keyboard.getKeyName(keyToBind), module.getName());

                        // TODO: Filter duplicates.
                        return;
                    }
                }
            }

            Serenity.getInstance().getKeybindManager().register(new CommandKeybinding(keyToBind, commandToBind));
            out("Added a keybinding for the key '%s' to the command '%s'", Keyboard.getKeyName(keyToBind), commandToBind);
        }, new KeyArgument("key"), new StringArgument("command")));

        branches.add(new CommandBranch("del", ctx -> {
            for (Keybinding keybinding : new ArrayList<>(Serenity.getInstance().getKeybindManager().getBindings())) {
                if (keybinding.getKey() == ctx.getArgumentValue("key", Integer.class)) {
                    if (keybinding instanceof ModuleKeybinding) {
                        ModuleKeybinding moduleKeybinding = (ModuleKeybinding) keybinding;

                        for (Command command : Serenity.getInstance().getCommandManager().getCommands()) {
                            if (command instanceof ModuleCommand) {
                                if (command.getCommandName().equalsIgnoreCase(ctx.getArgumentValue("command"))) {
                                    if (((ModuleCommand) command).getModule() == moduleKeybinding.getModule()) {
                                        Serenity.getInstance().getKeybindManager().getBindings().remove(keybinding);
                                        out("Removed a keybinding from the key '%s' for the module '%s'.", Keyboard.getKeyName(ctx.getArgumentValue("key")), moduleKeybinding.getModule().getName());
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    if (keybinding instanceof CommandKeybinding) {
                        CommandKeybinding commandKeybinding = (CommandKeybinding) keybinding;

                        if (commandKeybinding.getCommand().equalsIgnoreCase(ctx.getArgumentValue("command"))) {
                            Serenity.getInstance().getKeybindManager().getBindings().remove(keybinding);
                            out("Removed a keybinding from the key '%s' for the command '%s'.", Keyboard.getKeyName(ctx.getArgumentValue("key")), commandKeybinding.getCommand());
                            return;
                        }
                    }
                }
            }

            out("No keybinding was found matching those criteria.");
        }, new KeyArgument("key"), new StringArgument("command")));

        branches.add(new CommandBranch("list", ctx -> {
            StringBuilder builder = new StringBuilder("Commands for key '").append(Keyboard.getKeyName(ctx.getArgumentValue("key"))).append("':");
            int count = 0;
            for (Keybinding keybinding : Serenity.getInstance().getKeybindManager().getBindings()) {
                if (keybinding.getKey() == ctx.getArgumentValue("key", Integer.class)) {
                    count++;
                    if (keybinding instanceof ModuleKeybinding) {
                        ModuleKeybinding moduleKeybinding = (ModuleKeybinding) keybinding;
                        builder.append("\n - Module: '").append(moduleKeybinding.getModule().getName()).append("'");
                    }
                    if (keybinding instanceof CommandKeybinding) {
                        CommandKeybinding commandKeybinding = (CommandKeybinding) keybinding;
                        builder.append("\n - Command: '").append(commandKeybinding.getCommand()).append("'");
                    }
                }
            }

            out(count > 0 ? builder.toString() : String.format("No keybindings for key '%s'.", Keyboard.getKeyName(ctx.getArgumentValue("key"))));
        }, new KeyArgument("key")));

        branches.add(new CommandBranch("clear", ctx -> {
            Serenity.getInstance().getKeybindManager().getBindings().clear();
            out("All keybindings have been cleared.");
        }));
    }
}
