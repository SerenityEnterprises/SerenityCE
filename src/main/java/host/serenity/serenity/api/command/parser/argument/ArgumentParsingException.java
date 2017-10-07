package host.serenity.serenity.api.command.parser.argument;

public class ArgumentParsingException extends RuntimeException {
    public ArgumentParsingException(String message) {
        super(message);
    }

    public ArgumentParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
