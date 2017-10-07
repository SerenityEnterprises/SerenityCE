package host.serenity.serenity.api.module.command.argument;

import host.serenity.serenity.api.command.parser.argument.ArgumentParsingException;
import host.serenity.serenity.api.command.parser.argument.CommandArgument;
import host.serenity.serenity.api.module.Module;
import host.serenity.serenity.api.value.Value;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValueArgumentFromModule extends CommandArgument<Value<?>> {
    private final Module module;

    public ValueArgumentFromModule(String identifier, Module module) {
        super(identifier);
        this.module = module;
    }

    @Override
    public Value<?> getObjectFromString(String string) throws ArgumentParsingException {
        Stream<Value<?>> valueStream = module.getValues().stream();
        if (module.getActiveMode() != null)
            valueStream = Stream.concat(valueStream, Arrays.stream(module.getActiveMode().getValues()));
        return valueStream.filter(value -> value.getName().replace(" ", "").equalsIgnoreCase(string.replace("_", ""))).findFirst().orElse(null);
    }

    @Override
    public String getStringFromObject(Value<?> object) {
        return object.getName().replace(" ", "");
    }

    @Override
    public Value<?>[] getAllowedObjects() {
        Stream<Value<?>> valueStream = module.getValues().stream();
        if (module.getActiveMode() != null)
            valueStream = Stream.concat(valueStream, Arrays.stream(module.getActiveMode().getValues()));

        return valueStream.collect(Collectors.toList()).toArray(new Value<?>[0]);
    }

    @Override
    public String getTypeDescriptor() {
        return "value";
    }
}
