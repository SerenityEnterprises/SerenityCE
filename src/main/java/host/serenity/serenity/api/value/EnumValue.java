package host.serenity.serenity.api.value;

import org.jetbrains.annotations.NotNull;

public final class EnumValue<T extends Enum<T>> extends Value<T> {
    private final T[] constants;

    public EnumValue(String name, T value) {
        super(name, value);
        this.constants = extractConstantsFromEnumValue(value);
    }

    private T[] extractConstantsFromEnumValue(T value) {
        return value.getDeclaringClass().getEnumConstants();
    }

    public String getFixedValue() {
        return getValue().toString();
    }

    public T[] getConstants() {
        return constants;
    }

    public void increment() {
        T currentValue = getValue();

        for (T constant : constants) {
            if (constant != currentValue) {
                continue;
            }

            T newValue;

            int ordinal = constant.ordinal();
            if (ordinal == constants.length - 1) {
                newValue = constants[0];
            } else {
                newValue = constants[ordinal + 1];
            }

            setValue(newValue);
            return;
        }
    }

    public void decrement() {
        T currentValue = getValue();

        for (T constant : constants) {
            if (constant != currentValue) {
                continue;
            }

            T newValue;

            int ordinal = constant.ordinal();
            if (ordinal == 0) {
                newValue = constants[constants.length - 1];
            } else {
                newValue = constants[ordinal - 1];
            }

            setValue(newValue);
            return;
        }
    }

    @Override
    public void setValueFromString(@NotNull String string) {
        for (T constant : constants) {
            if (constant.name().equalsIgnoreCase(string)) {
                setValue(constant);
            }
        }
    }
}
