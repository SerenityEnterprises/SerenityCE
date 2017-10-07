package host.serenity.serenity.api.command.parser.argument.impl;

import host.serenity.serenity.api.command.parser.argument.ArgumentParsingException;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;

public class DoubleArgument extends CommandArgument<Double> {
    public DoubleArgument(String identifier) {
        super(identifier);
    }

    @Override
    public Double getObjectFromString(String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            throw new ArgumentParsingException(String.format("Invalid value for '%s' (type: '%s')", this.getIdentifier(), this.getTypeDescriptor()));
        }
    }

    @Override
    public String getStringFromObject(Double object) {
        return String.valueOf(object.doubleValue());
    }

    @Override
    public Double[] getAllowedObjects() {
        return new Double[0];
    }

    @Override
    public String getTypeDescriptor() {
        return "num";
    }
}
