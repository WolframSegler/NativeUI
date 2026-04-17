package wfg.native_ui.ui.system;

import java.awt.Color;

import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public class DebugBgSystem extends BaseSystem {
    private static final DebugBgSystem INSTANCE = new DebugBgSystem();
    public static DebugBgSystem get() { return INSTANCE;}
    private DebugBgSystem() {}

    public void init(CustomPanel element) {}

    @Override
    public void renderBelow(final CustomPanel element, float alpha) {
        final var pos = element.getPos();

        final int x = (int) pos.getX();
        final int y = (int) pos.getY();
        final int w = (int) pos.getWidth();
        final int h = (int) pos.getHeight();

        RenderUtils.drawQuad(x, y, w, h, Color.GREEN, 0.15f * alpha, false);
    }
}