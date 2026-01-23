package wfg.native_ui.ui.systems;

import static wfg.native_ui.util.UIConstants.*;

import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.ui.components.InputSnapshot;
import wfg.native_ui.ui.components.LayoutOffsetComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.OutlineComp;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.components.OutlineComp.OutlineType;
import wfg.native_ui.ui.panels.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class OutlineSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

    private final OutlineComp outline;
    private final UIContextComp context;
    private final LayoutOffsetComp offset;

    public OutlineSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.OUTLINE, new OutlineComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        comp.setIfNotPresent(NativeComponents.LAYOUT_OFFSET, new LayoutOffsetComp());

        outline = comp.get(NativeComponents.OUTLINE);
        context = comp.get(NativeComponents.UI_CONTEXT);
        offset = comp.get(NativeComponents.LAYOUT_OFFSET);
    }

    @Override
    public final void renderBelow(float alpha, InputSnapshot input) {
        if (!outline.enabled || outline.type == OutlineType.NONE || !context.isValid()) return;

        final PositionAPI pos = panel.getPos();

        String textureID = null;
        int textureSize = 4;
        int borderThickness = 0;

        switch (outline.type) {
            case LINE: borderThickness = 1; break;
            case VERY_THIN: borderThickness = 2; break;
            case THIN: borderThickness = 3; break;
            case MEDIUM: borderThickness = 4; break;
            case THICK: borderThickness = 8; break;
            case TEX_VERY_THIN: textureID = "ui_border4"; break;
            case TEX_THIN: textureID = "ui_border3"; break;
            case TEX_MEDIUM: textureID = "ui_border1"; textureSize = 8; break;
            case TEX_THICK: textureID = "ui_border2"; textureSize = 24; break;
            default: break;
        }

        if (borderThickness != 0) {
            RenderUtils.drawFramedBorder(
                pos.getX() + offset.x,
                pos.getY() + offset.y,
                pos.getWidth() + offset.w,
                pos.getHeight() + offset.h,
                borderThickness,
                outline.color,
                alpha
            );
        }

        if (textureID != null) {
            RenderUtils.drawRoundedBorder(
                pos.getX() - pad + offset.x,
                pos.getY() - pad + offset.y,
                pos.getWidth() + pad * 2 + offset.w,
                pos.getHeight() + pad * 2 + offset.h,
                alpha, textureID, textureSize,
                outline.color
            );
        }
    }
}