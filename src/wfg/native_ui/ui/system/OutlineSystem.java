package wfg.native_ui.ui.system;

import static wfg.native_ui.util.UIConstants.*;

import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.OutlineComp;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class OutlineSystem extends BaseSystem {

    private static final OutlineSystem INSTANCE = new OutlineSystem();
    public static OutlineSystem get() { return INSTANCE;}
    private OutlineSystem() {}

    @Override
    public void init(CustomPanel element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.OUTLINE, new OutlineComp());
    }

    @Override
    public final void renderBelow(final CustomPanel element, float alpha) {
        final OutlineComp outline = element.comp().get(NativeComponents.OUTLINE);

        if (!outline.enabled) return;

        final PositionAPI pos = element.getPos();

        String textureID = null;
        int textureSize = 4;
        int borderThickness = 0;

        switch (outline.type) {
            case LINE: borderThickness = 1; break;
            case VERY_THIN: borderThickness = 2; break;
            case THIN: borderThickness = 3; break;
            case MEDIUM: borderThickness = 4; break;
            case THICK: borderThickness = 8; break;
            case TEX_VERY_THIN: textureID = UI_BORDER_4; break;
            case TEX_THIN: textureID = UI_BORDER_3; break;
            case TEX_MEDIUM: textureID = UI_BORDER_1; textureSize = 8; break;
            case TEX_THICK: textureID = UI_BORDER_2; textureSize = 24; break;
            default: break;
        }

        if (borderThickness != 0) {
            RenderUtils.drawFramedBorder(
                pos.getX() + outline.offset.x,
                pos.getY() + outline.offset.y,
                pos.getWidth() + outline.offset.w,
                pos.getHeight() + outline.offset.h,
                borderThickness,
                outline.color,
                alpha
            );
        }

        if (textureID != null) {
            RenderUtils.drawRoundedBorder(
                pos.getX() - pad + outline.offset.x,
                pos.getY() - pad + outline.offset.y,
                pos.getWidth() + pad * 2 + outline.offset.w,
                pos.getHeight() + pad * 2 + outline.offset.h,
                alpha, textureID, textureSize,
                outline.color
            );
        }
    }
}