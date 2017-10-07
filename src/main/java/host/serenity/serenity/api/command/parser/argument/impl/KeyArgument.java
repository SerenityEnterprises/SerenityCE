package host.serenity.serenity.api.command.parser.argument.impl;

import host.serenity.serenity.api.command.parser.argument.ArgumentParsingException;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;
import org.lwjgl.input.Keyboard;

public class KeyArgument extends CommandArgument<Integer> {
    public KeyArgument(String identifier) {
        super(identifier);
    }

    @Override
    public Integer getObjectFromString(String string) throws ArgumentParsingException {
        return Keyboard.getKeyIndex(string.toUpperCase());
    }

    @Override
    public String getStringFromObject(Integer object) {
        return Keyboard.getKeyName(object).toUpperCase();
    }

    @Override
    public Integer[] getAllowedObjects() {
        return new Integer[0];
    }

    @Override
    public String getTypeDescriptor() {
        return "key";
    }
}