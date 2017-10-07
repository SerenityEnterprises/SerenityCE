package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.CommandSender;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.DoubleArgument;

public class Vclip extends Command {
    public Vclip() {
        branches.add(new CommandBranch(ctx -> {
            double blocks = ctx.getArgumentValue("blocks");
            mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0, blocks, 0));

            if (ctx.sender == CommandSender.CHAT)
                out("Teleported %s %s blocks.", (blocks < 0 ? "down" : "up"), Math.abs(blocks));
        }, new DoubleArgument("blocks")));
    }
}
