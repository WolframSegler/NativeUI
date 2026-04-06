package wfg.native_ui.ui.system;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.ui.component.HoverGlowComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.component.UIContextComp;
import wfg.native_ui.ui.component.HoverGlowComp.GlowType;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.RenderUtils;

public final class HoverGlowSystem extends BaseSystem {

    private static final HoverGlowSystem INSTANCE = new HoverGlowSystem();
    public static HoverGlowSystem get() { return INSTANCE;}
    private HoverGlowSystem() {}

    @Override
    public void init(CustomPanel<?> element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.HOVER_GLOW, new HoverGlowComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    @Override
    public final void advance(final CustomPanel<?> element, float amount) {
        final var comp = element.comp();
        final HoverGlowComp glow = comp.get(NativeComponents.HOVER_GLOW);
        final UIContextComp context = comp.get(NativeComponents.UI_CONTEXT);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);

        if (!glow.enabled || !glow.isFaderOwner) return;

        State target = input.hoveredLastFrame ? State.IN : State.OUT;

        if (!context.isValid()) target = State.OUT;
        
        if (glow.persistent) target = State.IN;

        glow.fader.setState(target);
        glow.fader.advance(amount);
    }

    @Override
    public final void renderBelow(final CustomPanel<?> element, float alpha) {
        final var comp = element.comp();
        final HoverGlowComp glow = comp.get(NativeComponents.HOVER_GLOW);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);
        if (glow.fader.getBrightness() <= 0f) return;

        switch (glow.type) {
            case UNDERLAY:
                drawGlowLayer(alpha, input, glow, element);
                break;

            default: break;
        }
        
    }

    @Override
    public final void render(final CustomPanel<?> element, float alpha) {
        final var comp = element.comp();
        final HoverGlowComp glow = comp.get(NativeComponents.HOVER_GLOW);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);
        if (glow.fader.getBrightness() <= 0) return;

        switch (glow.type) {
        case OVERLAY:
            drawGlowLayer(alpha, input, glow, element);
            break;

        case ADDITIVE:
            final float glowAmount = glow.additiveBrightness * glow.fader.getBrightness() * alpha;
            final SpriteAPI sprite = glow.additiveSprite;
            if (sprite != null) {
                RenderUtils.drawAdditiveGlow(
                    sprite,
                    element.getPos().getX(),
                    element.getPos().getY(),
                    glow.color,
                    glowAmount
                );
            } else {
                drawGlowLayer(alpha, input, glow, element);
            }
            break;
            
        default: break;
        }
    }

    private final void drawGlowLayer(float alpha, InputSnapshotComp input, HoverGlowComp glow,
        CustomPanel<?> element
    ) {

        final float effectiveAlpha = glow.overlayBrightness * glow.fader.getBrightness() * alpha;
        final float brightness = input.hasLMBClickedBefore ? effectiveAlpha * 1.5f : effectiveAlpha;
        final float[] verts = glow.faderMaskVertices != null ? glow.faderMaskVertices.clone() : null;

        if (verts != null) {
            RenderUtils.drawPolygon(verts, glow.color, brightness);
        } else {
            final PositionAPI pos = element.getPos();
            RenderUtils.drawQuad(
                pos.getX() + glow.offset.x,
                pos.getY() + glow.offset.y,
                pos.getWidth() + glow.offset.w,
                pos.getHeight() + glow.offset.h,
                glow.color, brightness, glow.type == GlowType.ADDITIVE
            );
        }
    }
}