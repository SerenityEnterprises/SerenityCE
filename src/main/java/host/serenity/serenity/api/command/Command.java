package host.serenity.serenity.api.command;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.parser.CommandBranch;
import host.serenity.serenity.api.command.parser.CommandContext;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;
import host.serenity.serenity.api.command.parser.argument.impl.StringArgument;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command {
    protected final Minecraft mc = Minecraft.getMinecraft();

    private final String commandName;
    protected final List<CommandBranch> branches = new ArrayList<>();

    public Command(String commandName) {
        this.commandName = commandName;
    }

    protected Command() {
        this.commandName = this.getClass().getSimpleName().toLowerCase();
    }

    public String getCommandName() {
        return commandName;
    }

    @SuppressWarnings("unchecked")
    public final void execute(CommandSender sender, String[] args) {
        List<CommandBranch> viableBranches = getViableBranches(args, true);

        if (viableBranches.size() > 1) {
            viableBranches = getViableBranches(args, false);
        }

        if (viableBranches.size() == 1) {
            CommandBranch branch = viableBranches.get(0);

            int numberOfArguments = (branch.getIdentifier() != null) ? branch.getArguments().size() + 1 : branch.getArguments().size();
            boolean isFinalStringArgument = (args.length > numberOfArguments &&
                    branch.getArguments().size() >= 1 &&
                    branch.getArguments().get(branch.getArguments().size() - 1) instanceof StringArgument);

            List<String> rawArgsList = new ArrayList<>(Arrays.asList(args));
            if (branch.getIdentifier() != null)
                rawArgsList.remove(0);

            CommandContext context = new CommandContext(sender, rawArgsList.toArray(new String[rawArgsList.size()]));
            if (branch.getArguments().size() == 0) {
                branch.accept(context);
            } else {
                for (int i = (branch.getIdentifier() != null ? 1 : 0); i < args.length; i++) {
                    final String argument = args[i];

                    if (i == numberOfArguments - 1 && isFinalStringArgument) {
                        StringBuilder argumentStringBuilder = new StringBuilder();
                        for (int j = i; j < args.length; j++) {
                            argumentStringBuilder.append(args[j]).append(" ");
                        }

                        argumentStringBuilder.setLength(argumentStringBuilder.length() - 1); // Remove final ' ' character.

                        StringArgument stringArgument = (StringArgument) branch.getArguments().get(branch.getArguments().size() - 1);
                        context.getArguments().put(stringArgument, argumentStringBuilder.toString());
                        break;
                    }

                    CommandArgument commandArgument = branch.getArguments().get(i + (branch.getIdentifier() == null ? 0 : -1));
                    if (commandArgument.getAllowedObjects().length == 0) {
                        context.getArguments().put(commandArgument, commandArgument.getObjectFromString(argument));
                    } else {
                        for (Object allowedObject : commandArgument.getAllowedObjects()) {
                            if (commandArgument.getStringFromObject(allowedObject).equalsIgnoreCase(argument)) {
                                context.getArguments().put(commandArgument, allowedObject);
                                break;
                            }
                        }
                    }
                }

                branch.accept(context);
            }
        } else {
            out(String.format("Syntax:\n%s", this.getSyntax()));
        }
    }

    private List<CommandBranch> getViableBranches(String[] args, boolean shouldCheckFinalStringArgument) {
        List<CommandBranch> viableBranches = new ArrayList<>();

        for (CommandBranch branch : branches) {
            if (branch.getIdentifier() == null || (args.length > 0 && args[0].equalsIgnoreCase(branch.getIdentifier()))) {
                int numberOfArguments = (branch.getIdentifier() != null) ? branch.getArguments().size() + 1 : branch.getArguments().size();

                boolean isFinalStringArgument = (args.length > numberOfArguments &&
                        branch.getArguments().size() >= 1 &&
                        branch.getArguments().get(branch.getArguments().size() - 1) instanceof StringArgument);

                isFinalStringArgument = isFinalStringArgument && shouldCheckFinalStringArgument;

                if (args.length == numberOfArguments ||
                        isFinalStringArgument) {

                    boolean isViableBranch = true;

                    for (int i = (branch.getIdentifier() != null ? 1 : 0); i < args.length; i++) {
                        final String argument = args[i];

                        if (i >= numberOfArguments - 1 && isFinalStringArgument) {
                            isViableBranch = true;
                            break;
                        } else {
                            if (branch.getArguments().size() > 0) {
                                CommandArgument commandArgument = branch.getArguments().get(i + (branch.getIdentifier() == null ? 0 : -1));

                                if (commandArgument.getAllowedObjects().length == 0) {
                                    if (commandArgument.getObjectFromString(argument) == null) {
                                        isViableBranch = false;
                                        break;
                                    }
                                } else {
                                    boolean found = false;
                                    for (Object allowedObject : commandArgument.getAllowedObjects()) {
                                        if (allowedObject == null)
                                            continue;

                                        if (commandArgument.getStringFromObject(allowedObject).equalsIgnoreCase(argument)) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        isViableBranch = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (isViableBranch)
                        viableBranches.add(branch);
                }
            }
        }

        return viableBranches;
    }

    public String getSyntax() {
        StringBuilder syntax = new StringBuilder();
        for (CommandBranch branch : branches) {
            syntax.append(".").append(this.getCommandName());

            if (branch.getIdentifier() != null)
                syntax.append(" ");

            syntax.append(getBranchSyntax(branch)).append("\n");
        }

        return syntax.toString().substring(0, syntax.length() - 1);
    }

    public static String getBranchSyntax(CommandBranch branch) {
        StringBuilder syntax = new StringBuilder();

        if (branch.getIdentifier() != null) {
            syntax.append(branch.getIdentifier());
        }

        for (CommandArgument<?> commandArgument : branch.getArguments()) {
            syntax.append(" <").append(commandArgument.getIdentifier()).append(":").append(commandArgument.getTypeDescriptor()).append(">");
        }

        return syntax.toString();
    }

    protected static void out(String info) {
        Serenity.getInstance().addChatMessage(info);
    }

    protected static void out(String info, Object... formatting) {
        out(String.format(info, formatting));
    }
}
