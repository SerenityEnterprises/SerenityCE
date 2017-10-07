package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;

public class Echo extends Command {
    public Echo() {
        branches.add(new CommandBranch(ctx -> {
            out(ctx.getArgumentValue("message"));
        }, new StringArgument("message")));
    }
}
