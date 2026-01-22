package wfg.wrap_ui.ui.systems;

import wfg.wrap_ui.ui.components.BackgroundComp;
import wfg.wrap_ui.ui.components.InputSnapshot;
import wfg.wrap_ui.ui.components.LayoutOffsetComp;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.panels.CustomPanel;
import wfg.wrap_ui.util.RenderUtils;

public final class BackgroundSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

    private final BackgroundComp bg;
    private final LayoutOffsetComp offset;

    public BackgroundSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.BACKGROUND, new BackgroundComp());
        comp.setIfNotPresent(NativeComponents.LAYOUT_OFFSET, new LayoutOffsetComp());

        bg = comp.get(NativeComponents.BACKGROUND);
        offset = comp.get(NativeComponents.LAYOUT_OFFSET);
    }

    @Override
    public void renderBelow(float alpha, InputSnapshot input) {
        if (!bg.enabled) return;

        final var pos = panel.getPos();

        final int x = (int) pos.getX() + offset.x;
        final int y = (int) pos.getY() + offset.y;
        final int w = (int) pos.getWidth() + offset.w;
        final int h = (int) pos.getHeight() + offset.h;

        RenderUtils.drawQuad(x, y, w, h, bg.color, bg.alpha * alpha, false);
    }
}