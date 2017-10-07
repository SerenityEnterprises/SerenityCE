package host.serenity.serenity.api.command.parser.argument.impl;

import host.serenity.serenity.api.command.parser.argument.CommandArgument;

public class StringArgument extends CommandArgument<String> {
    public StringArgument(String identifier) {
        super(identifier);
    }

    @Override
    public String getObjectFromString(String string) {
        return string;
    }

    @Override
    public String getStringFromObject(String object) {
        return object;
    }

    @Override
    public String[] getAllowedObjects() {
        return new String[0];
    }

    @Override
    public String getTypeDescriptor() {
        return "str";
    }
}
