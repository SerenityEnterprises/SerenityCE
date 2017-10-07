package host.serenity.serenity.util.overlay;

public enum OverlayArea {
    TOP_LEFT(true, true),
    TOP_RIGHT(true, false),
    BOTTOM_LEFT(false, true),
    BOTTOM_RIGHT(false, false);


    private final boolean top, left;
    OverlayArea(boolean top, boolean left) {
        this.top = top;
        this.left = left;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isLeft() {
        return left;
    }
}
