package host.serenity.serenity.api.module.command;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.CommandSender;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.command.argument.ValueArgumentFromModule;
import host.serenity.serenity.api.module.mode.ModuleMode;
import host.serenity.serenity.api.value.EnumValue;
import host.serenity.serenity.api.value.Value;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ModuleCommand extends Command {
    private final Module module;
    public ModuleCommand(Module module) {
        super(module.getName().toLowerCase().replace(" ", ""));
        this.module = module;

        branches.add(new CommandBranch(ctx -> {
            if (ctx.sender == CommandSender.CHAT)
                out("%s has now been %s.", module.getName(), (!module.isEnabled() ? EnumChatFormatting.GREEN + "enabled" : EnumChatFormatting.RED + "disabled") + EnumChatFormatting.RESET);

            module.setState(!module.isEnabled());
        }));

        branches.add(new CommandBranch(ctx -> {
            Value value = ctx.getArgumentValue("value");
            if (value == null) {
                out("The value '%s' does not exist in %s.", ctx.rawArguments[0], module.getName());
                return;
            }

            out("The current value of '%s' is set to: %s (Default: %s)", value.getName(), value.getValue(), value.getDefaultValue());
            if (value instanceof EnumValue) {
                String possible = "";

                for (Enum possibleValue : ((EnumValue) value).getConstants()) {
                    possible += possibleValue.name().toLowerCase() + " ";
                }
                out("Possible values of '%s' are: %s", value.getName(), possible.trim());
            }
        }, new ValueArgumentFromModule("value", module)));

        branches.add(new CommandBranch(ctx -> {
            Value value = ctx.getArgumentValue("value");
            if (value == null) {
                out("The value '%s' does not exist in %s.", ctx.rawArguments[0], module.getName());
                return;
            }
            String newValue = ctx.getArgumentValue("new_value");
            if (newValue.equalsIgnoreCase("reset") || newValue.equalsIgnoreCase("clear") || newValue.equalsIgnoreCase("default")) {
                //noinspection unchecked
                value.setValue(value.getDefaultValue());
                if (ctx.sender == CommandSender.CHAT)
                    out("The value of '%s' was set to the default (%s)", value.getName(), value.getValue());
                return;
            }

            value.setValueFromString(newValue);
            if (ctx.sender == CommandSender.CHAT) {
                out("The value of '%s' was set to %s", value.getName(), value.getValue());
            }
        }, new ValueArgumentFromModule("value", module), new StringArgument("new_value")));

        branches.add(new CommandBranch("modes", ctx -> {
            if (!module.getModuleModes().isEmpty()) {
                StringBuilder modes = new StringBuilder();
                for (ModuleMode mode : module.getModuleModes()) {
                    modes.append(mode.getName()).append("\n");
                }

                modes.setLength(modes.length() - 1); // Remove final '\n' character.

                out("Available modes for '%s':\n%s", module.getName(), modes.toString());
            } else {
                out("No modes available for '%s'.", module.getName());
            }
        }));

        if (!module.getModuleModes().isEmpty()) {
            branches.add(new CommandBranch("mode", ctx -> {
                out("The currently active mode of '%s' is: '%s'", module.getName(), module.getActiveMode().getName());
            }));

            branches.add(new CommandBranch("mode", ctx -> {
                for (ModuleMode mode : module.getModuleModes()) {
                    if (mode.getName().replace(" ", "").equalsIgnoreCase(ctx.getArgumentValue("mode", String.class).replace(" ", ""))) {
                        module.setActiveMode(mode);
                        out("The active mode of '%s' was set to: '%s'", module.getName(), mode.getName());
                        return;
                    }
                }

                out("Module mode not found.");
            }, new StringArgument("mode")));
        }
    }

    public List<CommandBranch> getBranches() {
        return branches;
    }

    public Module getModule() {
        return module;
    }
}
