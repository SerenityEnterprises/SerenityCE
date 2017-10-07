package host.serenity.serenity.api.binding;

public abstract class Keybinding {
    private final int key;

    public Keybinding(int key) {
        this.key = key;
    }

    public abstract void updateState(boolean state);

    public int getKey() {
        return key;
    }
}
