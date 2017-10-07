package host.serenity.serenity.api.value;

import org.jetbrains.annotations.NotNull;

public class EnumValue<T extends Enum> extends Value<T> {
    public EnumValue(String name, T value) {
        super(name, value);
    }

    public String getFixedValue() {
        return this.getValue().toString();
    }

    @SuppressWarnings("unchecked")
    public T[] getConstants() {
        return (T[]) getValue().getClass().getEnumConstants();
    }


    public void increment() {
        Enum[] array;
        for (int length = (array = getValue().getClass().getEnumConstants()).length, i = 0; i < length; i++) {
            if (array[i].toString().equalsIgnoreCase(getFixedValue())) {
                i++;
                if (i > array.length - 1) {
                    i = 0;
                }
                setValueFromString(array[i].toString());
            }
        }
    }

    public void decrement() {
        Enum[] array;
        for (int length = (array = getValue().getClass().getEnumConstants()).length, i = 0; i < length; i++) {
            if (array[i].toString().equalsIgnoreCase(getFixedValue())) {
                i--;
                if (i < 0) {
                    i =  array.length - 1;
                }
                setValueFromString(array[i].toString());
            }
        }
    }

    @Override
    public void setValueFromString(@NotNull String string) {
        Enum[] array;
        for (int length = (array = getValue().getClass().getEnumConstants()).length, i = 0; i < length; i++) {
            if (array[i].toString().equalsIgnoreCase(string)) {
                this.setValue((T) array[i]);
            }
        }
    }
}
