package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.module.command.argument.ModuleArgument;

public class Hide extends Command {
    public Hide() {
        branches.add(new CommandBranch(ctx -> {
            Module module = ctx.getArgumentValue("module");
            if (module == null) {
                out("Module not found!");
                return;
            }

            module.setHidden(!module.isHidden());

            out("The module '%s' is %s hidden.", module.getName(), module.isHidden() ? "now" : "no longer");
        }, new ModuleArgument("module")));
    }
}
