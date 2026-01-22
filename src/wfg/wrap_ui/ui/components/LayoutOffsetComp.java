package wfg.wrap_ui.ui.components;

public final class LayoutOffsetComp extends BaseComponent {
    public int x, y, w, h;

    public final void setOffset(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }
}