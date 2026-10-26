package wfg.native_ui.ui.system;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.ui.component.HoverGlowComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.HoverGlowComp.GlowType;
import wfg.native_ui.ui.core.UIEntityAPI;
import wfg.native_ui.util.RenderUtils;

public final class HoverGlowSystem extends BaseSystem {

    private static final HoverGlowSystem INSTANCE = new HoverGlowSystem();
    public static HoverGlowSystem get() { return INSTANCE;}
    private HoverGlowSystem() {}

    @Override
    public void init(UIEntityAPI element) {
        element.comp().setIfNotPresent(NativeComponents.HOVER_GLOW, new HoverGlowComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    @Override
    public final void advance(final UIEntityAPI element, float amount) {
        final var comp = element.comp();
        final HoverGlowComp glow = comp.get(NativeComponents.HOVER_GLOW);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);

        if (!glow.enabled || !glow.isFaderOwner) return;

        final State target = (input.hoveredLastFrame || glow.persistent) ? State.IN : State.OUT;

        glow.fader.setState(target);
        glow.fader.advance(amount);
    }

    @Override
    public final void renderBelow(final UIEntityAPI element, float alpha) {
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
    public final void renderAbove(final UIEntityAPI element, float alpha) {
        final var comp = element.comp();
        final HoverGlowComp glow = comp.get(NativeComponents.HOVER_GLOW);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);
        if (glow.fader.getBrightness() <= 0f) return;

        switch (glow.type) {
        case OVERLAY:
            drawGlowLayer(alpha, input, glow, element);
            break;

        case ADDITIVE:
            final float brightness = getEffectiveBrightness(glow, input) * alpha;
            final SpriteAPI sprite = glow.additiveSprite;
            if (sprite != null) {
                RenderUtils.drawAdditiveGlow(
                    sprite, element.getX(),
                    element.getY(),
                    glow.color,
                    brightness
                );
            } else {
                drawGlowLayer(alpha, input, glow, element);
            }
            break;
            
        default: break;
        }
    }

    private final void drawGlowLayer(float alpha, InputSnapshotComp input, HoverGlowComp glow,
        UIEntityAPI element
    ) {
        final float brightness = getEffectiveBrightness(glow, input) * alpha;
        final PositionAPI pos = element.pos();

        if (glow.faderMaskVertices != null) {
            RenderUtils.drawPolygon(
                glow.faderMaskVertices,
                pos.getX(),
                pos.getY(),
                glow.color, brightness
            );

        } else {
            RenderUtils.drawQuad(
                pos.getX() + glow.offset.x,
                pos.getY() + glow.offset.y,
                pos.getWidth() + glow.offset.w,
                pos.getHeight() + glow.offset.h,
                glow.color, brightness, glow.type == GlowType.ADDITIVE
            );
        }
    }

    private static final float getEffectiveBrightness(HoverGlowComp glow, InputSnapshotComp input) {
        final float scalar = switch(glow.type) {
            default -> 1f;
            case ADDITIVE -> 1f;
            case OVERLAY, UNDERLAY -> 0.45f;
        };

        if (glow.overrideBrightness > 0f) {
            return glow.overrideBrightness * scalar;
        }

        final float base = input.hasLMBClickedBefore ? glow.flashBrightness : glow.glowBrightness;

        return base * scalar * glow.fader.getBrightness();
    }
}