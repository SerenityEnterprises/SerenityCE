package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.CommandSender;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.argument.impl.DoubleArgument;
import me.jordin.deltoid.vector.Vec3;

/**
 * Created by jordin on 7/26/17.
 */
public class Hclip extends Command {
    public Hclip() {
        branches.add(new CommandBranch(ctx -> {
            double distance = ctx.getArgumentValue("distance");
            Vec3 delta = Vec3.fromAnglesDeg(distance, mc.thePlayer.rotationYaw, 0);

            mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(delta.x, 0, delta.z));

            if (ctx.sender == CommandSender.CHAT)
                out("Teleported %s %s blocks.", (distance < 0 ? "backwards" : "forwards"), Math.abs(distance));
        }, new DoubleArgument("distance")));
    }
}
