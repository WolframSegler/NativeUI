package wfg.native_ui.internal.ui;

public enum Side {
    LEFT, RIGHT, TOP, BOTTOM;

    public final boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public final boolean isVertical() {
        return this == TOP || this == BOTTOM;
    }
}