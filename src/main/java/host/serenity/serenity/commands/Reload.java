package host.serenity.serenity.commands;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.Command;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.event.internal.GameShutdown;
import host.serenity.synapse.EventManager;

public class Reload extends Command {
    public Reload() {
        branches.add(new CommandBranch(ctx -> {
            EventManager.post(new GameShutdown());
            new Serenity();
        }));
    }
}
