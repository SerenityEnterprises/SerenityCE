package host.serenity.serenity.commands;

import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.event.internal.GameShutdown;
import host.serenity.synapse.EventManager;

public class Unload extends Command {
    public Unload() {
        branches.add(new CommandBranch(ctx -> {
            EventManager.post(new GameShutdown());
        }));
    }
}
