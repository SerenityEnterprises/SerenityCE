package host.serenity.serenity.api.command.parser.argument;

public abstract class CommandArgument<T> {
    private final String identifier;

    public CommandArgument(String identifier) {
        this.identifier = identifier;
    }

    public abstract T getObjectFromString(String string) throws ArgumentParsingException;
    public abstract String getStringFromObject(T object);
    public abstract T[] getAllowedObjects();
    public abstract String getTypeDescriptor();

    public T[] getObjectsForTabComplete() {
        return getAllowedObjects();
    }

    public String getIdentifier() {
        return identifier;
    }
}
