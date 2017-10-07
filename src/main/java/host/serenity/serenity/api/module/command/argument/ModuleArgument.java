package host.serenity.serenity.api.module.command.argument;

import host.serenity.serenity.Serenity;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;
import host.serenity.serenity.api.module.Module;

public class ModuleArgument extends CommandArgument<Module> {
    public ModuleArgument(String identifier) {
        super(identifier);
    }

    @Override
    public Module getObjectFromString(String string) {
        return Serenity.getInstance().getModuleManager().getModules().stream().filter(module -> module.getName().replace(" ", "").toLowerCase().equalsIgnoreCase(string)).findFirst().orElse(null);
    }

    @Override
    public String getStringFromObject(Module token) {
        return token.getName().replace(" ", "").toLowerCase();
    }

    @Override
    public Module[] getAllowedObjects() {
        return Serenity.getInstance().getModuleManager().getModules().toArray(new Module[Serenity.getInstance().getModuleManager().getModules().size()]);
    }

    @Override
    public String getTypeDescriptor() {
        return "module";
    }
}
