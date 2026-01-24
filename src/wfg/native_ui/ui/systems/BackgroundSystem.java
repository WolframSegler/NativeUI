package wfg.native_ui.ui.systems;

import wfg.native_ui.ui.components.BackgroundComp;
import wfg.native_ui.ui.components.LayoutOffsetComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.panels.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class BackgroundSystem extends BaseSystem {

    private static final BackgroundSystem INSTANCE = new BackgroundSystem();
    public static BackgroundSystem get() { return INSTANCE;}
    private BackgroundSystem() {}

    @Override
    public void init(CustomPanel<?> element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.BACKGROUND, new BackgroundComp());
        comp.setIfNotPresent(NativeComponents.LAYOUT_OFFSET, new LayoutOffsetComp());
    }

    @Override
    public void renderBelow(final CustomPanel<?> element, float alpha) {
        final var comp = element.comp();
        final BackgroundComp bg = comp.get(NativeComponents.BACKGROUND);
        final LayoutOffsetComp offset = comp.get(NativeComponents.LAYOUT_OFFSET);
        if (!bg.enabled) return;

        final var pos = element.getPos();

        final int x = (int) pos.getX() + offset.x;
        final int y = (int) pos.getY() + offset.y;
        final int w = (int) pos.getWidth() + offset.w;
        final int h = (int) pos.getHeight() + offset.h;

        RenderUtils.drawQuad(x, y, w, h, bg.color, bg.alpha * alpha, false);
    }
}