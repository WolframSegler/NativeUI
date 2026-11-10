package wfg.native_ui.ui.system;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.ui.component.GlowComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.GlowComp.GlowType;
import wfg.native_ui.ui.core.UIEntityAPI;
import wfg.native_ui.util.RenderUtils;

public final class GlowSystem extends BaseSystem {

    private static final GlowSystem INSTANCE = new GlowSystem();
    public static GlowSystem get() { return INSTANCE;}
    private GlowSystem() {}

    @Override
    public void init(UIEntityAPI element) {
        element.comp().setIfNotPresent(NativeComponents.GLOW, new GlowComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    @Override
    public final void advance(final UIEntityAPI element, float amount) {
        final var comp = element.comp();
        final GlowComp glow = comp.get(NativeComponents.GLOW);
        if (!glow.enabled) return;
        if (!glow.isSelfFaderOwner) return;
        
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);
        if (input.hasLMBClickedBefore && !glow.isFlashing()) glow.flash();

        glow.glowFader.setState(input.hoveredLastFrame ? State.IN : State.OUT);
        glow.glowFader.advance(amount);
        glow.flashFader.advance(amount);
        glow.highlightFader.advance(amount);
    }

    @Override
    public final void renderBelow(final UIEntityAPI element, float alpha) {
        final GlowComp glow = element.comp().get(NativeComponents.GLOW);

        switch (glow.type) {
            case UNDERLAY:
                drawGlowLayer(alpha, glow, element);
                break;
            default: break;
        }
    }

    @Override
    public final void renderAbove(final UIEntityAPI element, float alpha) {
        final GlowComp glow = element.comp().get(NativeComponents.GLOW);

        switch (glow.type) {
            case OVERLAY:
                drawGlowLayer(alpha, glow, element);
                break;

            case ADDITIVE:
                final SpriteAPI sprite = glow.additiveSprite;
                if (sprite != null) {
                    RenderUtils.drawAdditiveGlow(sprite, element.getX(), element.getY(),
                        glow.color, getEffectiveBrightness(glow) * alpha
                    );
                } else {
                    drawGlowLayer(alpha, glow, element);
                }
                break;

            default: break;
        }
    }

    private final void drawGlowLayer(float alpha, GlowComp glow,
        UIEntityAPI element
    ) {
        final float brightness = getEffectiveBrightness(glow) * alpha;
        final PositionAPI pos = element.pos();

        if (brightness <= 0f) return;

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

    private static final float getEffectiveBrightness(GlowComp glow) {
        final float scalar = switch (glow.type) {
            default -> 1f;
            case ADDITIVE -> 1f;
            case OVERLAY, UNDERLAY -> 0.45f;
        };

        if (glow.overrideBrightness > 0f) {
            return Math.min(1f, glow.overrideBrightness * scalar);
        }

        final float glowB = glow.glowFader.getBrightness() * glow.glowBrightness;
        final float highlightB = glow.highlightFader.getBrightness() * glow.highlightBrightness;
        final float flashB = glow.flashFader.getBrightness() * glow.flashBrightness;

        final float base = Math.max(glowB, highlightB);
        return Math.min(1f, base * scalar + flashB);
    }
}