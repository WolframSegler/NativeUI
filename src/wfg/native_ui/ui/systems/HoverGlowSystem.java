package wfg.native_ui.ui.systems;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.ui.components.HoverGlowComp;
import wfg.native_ui.ui.components.InputSnapshot;
import wfg.native_ui.ui.components.LayoutOffsetComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.components.HoverGlowComp.GlowType;
import wfg.native_ui.ui.panels.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class HoverGlowSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

    private final HoverGlowComp glow;
    private final UIContextComp context;
    private final LayoutOffsetComp offset;

    public HoverGlowSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.HOVER_GLOW, new HoverGlowComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        comp.setIfNotPresent(NativeComponents.LAYOUT_OFFSET, new LayoutOffsetComp());

        glow = comp.get(NativeComponents.HOVER_GLOW);
        context = comp.get(NativeComponents.UI_CONTEXT);
        offset = comp.get(NativeComponents.LAYOUT_OFFSET);
    }

    @Override
    public final void advance(float amount, InputSnapshot input) {
        if (!glow.enabled || !glow.isFaderOwner) return;

        State target = input.hoveredLastFrame ? State.IN : State.OUT;

        if (!context.isValid()) target = State.OUT;
        
        if (glow.persistent) target = State.IN;

        glow.fader.setState(target);
        glow.fader.advance(amount);
    }

    @Override
    public final void renderBelow(float alphaMult, InputSnapshot input) {
        if (glow.fader.getBrightness() <= 0f) return;

        switch (glow.type) {
            case UNDERLAY:
                drawGlowLayer(alphaMult, input);
                break;

            default: break;
        }
        
    }

    @Override
    public final void render(float alpha, InputSnapshot input) {
        if (glow.fader.getBrightness() <= 0) return;

        switch (glow.type) {
        case OVERLAY:
            drawGlowLayer(alpha, input);
            break;

        case ADDITIVE:
            final float glowAmount = glow.additiveBrightness * glow.fader.getBrightness() * alpha;
            final SpriteAPI sprite = glow.additiveSprite;
            if (sprite != null) {
                RenderUtils.drawAdditiveGlow(
                    sprite,
                    panel.getPos().getX(),
                    panel.getPos().getY(),
                    glow.color,
                    glowAmount
                );
            } else {
                drawGlowLayer(alpha, input);
            }
            break;
            
        default: break;
        }
    }

    private final void drawGlowLayer(float alpha, InputSnapshot input) {
        final float glowAmount = glow.overlayBrightness * glow.fader.getBrightness() * alpha;
        final float[] verts = glow.faderMaskVertices;

        if (verts != null) {
            for (int i = 0; i < verts.length; i += 2) {
                verts[i] = verts[i] + offset.x;
                verts[i + 1] = verts[i + 1] + offset.y;
            }
        }   

        if (verts != null) {
            RenderUtils.drawPolygon(verts, glow.color, glowAmount);
        } else {
            final PositionAPI pos = panel.getPos();
            RenderUtils.drawQuad(
                pos.getX() + offset.x,
                pos.getY() + offset.y,
                pos.getWidth() + offset.w,
                pos.getHeight() + offset.h,
                glow.color, glowAmount, glow.type == GlowType.ADDITIVE
            );
        }

        if (!input.hasLMBClickedBefore) return;
        
        if (verts != null) {
            RenderUtils.drawPolygon(verts, glow.color, glowAmount / 2f);
        } else {
            final PositionAPI pos = panel.getPos();
            RenderUtils.drawQuad(
                pos.getX() + offset.x,
                pos.getY() + offset.y,
                pos.getWidth() + offset.w,
                pos.getHeight() + offset.h,
                glow.color,
                glowAmount / 2f, glow.type == GlowType.ADDITIVE
            );
        }
    }
}