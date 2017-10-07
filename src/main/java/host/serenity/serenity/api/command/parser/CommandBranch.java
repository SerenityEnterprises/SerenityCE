package host.serenity.serenity.api.command.parser;

import host.serenity.serenity.api.command.parser.argument.CommandArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class CommandBranch {
    private final String identifier;
    private final Consumer<CommandContext> commandContextConsumer;

    private List<CommandArgument<?>> arguments = new ArrayList<>();

    public CommandBranch(String identifier, Consumer<CommandContext> commandContextConsumer, CommandArgument<?>... arguments) {
        this.identifier = identifier;
        this.commandContextConsumer = commandContextConsumer;

        this.arguments.addAll(Arrays.asList(arguments));
    }

    public CommandBranch(Consumer<CommandContext> commandContextConsumer, CommandArgument<?>... arguments) {
        this(null, commandContextConsumer, arguments);
    }

    public void accept(CommandContext context) {
        commandContextConsumer.accept(context);
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<CommandArgument<?>> getArguments() {
        return arguments;
    }
}
