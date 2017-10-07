package host.serenity.serenity.commands;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;

public class Friend extends Command {
    public Friend() {
        branches.add(new CommandBranch("add", ctx -> {
            Serenity.getInstance().getFriendManager().addFriend(ctx.getArgumentValue("friend"));
            out("The player '%s' was added to friends.", ctx.getArgumentValue("friend", String.class));
        }, new StringArgument("friend")));

        branches.add(new CommandBranch("add", ctx -> {
            Serenity.getInstance().getFriendManager().addFriend(ctx.getArgumentValue("friend"), ctx.getArgumentValue("alias"));
            out("The player '%s' was added to friends and nameprotected.", ctx.getArgumentValue("friend", String.class));
        }, new StringArgument("friend"), new StringArgument("alias")));

        branches.add(new CommandBranch("del", ctx -> {
            Serenity.getInstance().getFriendManager().delFriend(ctx.getArgumentValue("friend"));
            out("The player '%s' was deleted from friends.", ctx.getArgumentValue("friend", String.class));
        }, new StringArgument("friend")));
    }
}
