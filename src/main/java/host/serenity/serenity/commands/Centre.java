package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;

public class Centre extends Command {
    public Centre() {
        branches.add(new CommandBranch(ctx -> {
            mc.thePlayer.setPosition(Math.floor(mc.thePlayer.posX) + 0.5, mc.thePlayer.posY, Math.floor(mc.thePlayer.posZ) + 0.5);
        }));

        branches.add(new CommandBranch("angles", ctx -> {
            mc.thePlayer.rotationYaw = (float) (Math.round(mc.thePlayer.rotationYaw / 90) * 90);
            mc.thePlayer.rotationPitch = (float) (Math.round(mc.thePlayer.rotationPitch / 90) * 90);
        }));
    }
}
