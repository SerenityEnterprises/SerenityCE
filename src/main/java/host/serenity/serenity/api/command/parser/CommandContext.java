package host.serenity.serenity.api.command.parser;

import host.serenity.serenity.api.command.CommandSender;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;

import java.util.HashMap;
import java.util.Map;

public class CommandContext {
    private final Map<CommandArgument<?>, Object> arguments = new HashMap<>();
    public final String[] rawArguments;

    public final CommandSender sender;

    public CommandContext(CommandSender sender, String[] rawArguments) {
        this.sender = sender;
        this.rawArguments = rawArguments;
    }

    public Map<CommandArgument<?>, Object> getArguments() {
        return arguments;
    }

    public boolean hasArgument(CommandArgument<?> argument) {
        return arguments.containsKey(argument);
    }

    public boolean hasArgument(String identifier) {
        for (Map.Entry<CommandArgument<?>, ?> entry : arguments.entrySet()) {
            if (entry.getKey().getIdentifier().equals(identifier)) {
                return true;
            }
        }

        return false;
    }

    public <T> T getArgumentValue(String identifier, Class<T> type) {
        if (this.hasArgument(identifier)) {
            for (Map.Entry<CommandArgument<?>, ?> entry : arguments.entrySet()) {
                if (entry.getKey().getIdentifier().equals(identifier)) {
                    return type.cast(entry.getValue());
                }
            }
        }
        return null;
    }

    public <T> T getArgumentValue(String identifier) {
        if (this.hasArgument(identifier)) {
            try {
                for (Map.Entry<CommandArgument<?>, ?> entry : arguments.entrySet()) {
                    if (entry.getKey().getIdentifier().equals(identifier)) {
                        return (T) entry.getValue();
                    }
                }
            } catch (Exception e) {}
        }
        return null;
    }

    public <T> T getArgumentValue(CommandArgument<T> argument) {
        return this.hasArgument(argument) ? ((T) arguments.get(argument)) : null;
    }
}
