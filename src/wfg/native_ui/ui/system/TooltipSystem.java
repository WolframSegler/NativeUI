package wfg.native_ui.ui.system;

import static wfg.native_ui.util.Globals.settings;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.util.FaderUtil.State;

import wfg.native_ui.internal.ui.core.UITooltip;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.core.UIEntityAPI;

public final class TooltipSystem extends BaseSystem {

    private static final TooltipSystem INSTANCE = new TooltipSystem();
    public static TooltipSystem get() { return INSTANCE;}
    private TooltipSystem() {}

    @Override
    public void init(UIEntityAPI element) {
        element.comp().setIfNotPresent(NativeComponents.TOOLTIP, new TooltipComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    private static final float SCROLL_COOLDOWN = 0.15f;
    private static final int VISIBILITY_GRACE_FRAMES = 3;
    private static final float VISIBILITY_OPACITY_REQUIRED = 0.7f;

    @Override
    public void renderBelow(UIEntityAPI element, float alpha) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        if (alpha >= VISIBILITY_OPACITY_REQUIRED) spec.iLastVisibleFrameCounter = 0;
    }

    @Override
    public void advance(UIEntityAPI element, float delta) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);
        spec.iLastVisibleFrameCounter++;
        spec.iOrphanFrames = 0;

        if (shouldForceHide(spec, element)) {
            focusLost(element);
            return;
        }

        if (spec.iTooltip != null && spec.iTooltip.isFinished()) {
            focusLost(element);
            return;
        }

        final boolean hovering = input.hoveredLastFrame && !input.hasLMBClickedBefore;
        final boolean suppressed = input.LMBDownLastFrame || input.RMBDownLastFrame
            || Mouse.isButtonDown(2) || Mouse.getDWheel() != 0;

        if (!hovering) spec.iDismissedThisHover = false;

        final boolean shouldShow = hovering
            && !suppressed
            && spec.builder != null
            && !spec.iDismissedThisHover;

        if (shouldShow) {
            spec.iHoverTime += delta;
            if (spec.iHoverTime > settings.getTooltipDelay() && spec.iTooltip == null) {
                spec.iTooltip = UITooltip.create(spec);
                spec.iTooltip.attach();
            }
            if (spec.iTooltip != null) {
                spec.positioner.position(spec.iTooltip, spec.iTooltip.isExpanded());
            }
        } else {
            spec.iHoverTime = suppressed ? -SCROLL_COOLDOWN : 0f;
            if (spec.iTooltip != null) spec.iTooltip.dismiss();
        }
    }

    @Override
    public void onRemove(UIEntityAPI element) {
        focusLost(element);
    }

    public void focusLost(UIEntityAPI element) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        if (spec.iTooltip != null) spec.iTooltip.detach();
        spec.iTooltip = null;
        spec.iHoverTime = 0f;
    }

    private static final boolean shouldForceHide(TooltipComp spec, UIEntityAPI element) {
        return spec.iLastVisibleFrameCounter > VISIBILITY_GRACE_FRAMES ||
            element.getOpacity() <= 0f ||
            element.getFader().getState() == State.OUT ||
            !spec.enabled;
    }
}