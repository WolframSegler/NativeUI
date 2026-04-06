package wfg.native_ui.ui.system;

import wfg.native_ui.ui.component.BackgroundComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class BackgroundSystem extends BaseSystem {

    private static final BackgroundSystem INSTANCE = new BackgroundSystem();
    public static BackgroundSystem get() { return INSTANCE;}
    private BackgroundSystem() {}

    @Override
    public void init(CustomPanel<?> element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.BACKGROUND, new BackgroundComp());
    }

    @Override
    public void renderBelow(final CustomPanel<?> element, float alpha) {
        final var comp = element.comp();
        final BackgroundComp bg = comp.get(NativeComponents.BACKGROUND);
        if (!bg.enabled) return;

        final var pos = element.getPos();

        final int x = (int) pos.getX() + bg.offset.x;
        final int y = (int) pos.getY() + bg.offset.y;
        final int w = (int) pos.getWidth() + bg.offset.w;
        final int h = (int) pos.getHeight() + bg.offset.h;

        RenderUtils.drawQuad(x, y, w, h, bg.color, bg.alpha * alpha, false);
    }
}