package wfg.native_ui.ui.system;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.pad;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import rolflectionlib.util.RolfLectionUtil;
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

    // TODO clean up this workaround by directly getting the scroll panel class.
    public static final Object scrollPanelConstr;
    public static final Object setContentSizeMethod;
    public static final Object setSizeMethod;
    public static final Object setMaxShadowHeightMethod;
    public static final Object setUseSimpleShadowsMethod;

    static {
        final CustomPanelAPI customPanel = settings.createCustom(pad, pad, null);
        final TooltipMakerAPI tp = customPanel.createUIElement(1f, 1f, true);
        customPanel.addUIElement(tp);
        final Class<?> scrollClass = tp.getExternalScroller().getClass();

        scrollPanelConstr = RolfLectionUtil.getConstructor(scrollClass,
            RolfLectionUtil.getConstructorParamTypesSingleConstructor(scrollClass)
        );
        setContentSizeMethod = RolfLectionUtil.getMethodDeclared("setContentSize", 
            scrollClass, 2
        );
        setSizeMethod = RolfLectionUtil.getMethodFromSuperClass("setSize", scrollClass);
        setMaxShadowHeightMethod = RolfLectionUtil.getMethodDeclared("setMaxShadowHeight", 
            scrollClass, 1
        );
        setUseSimpleShadowsMethod = RolfLectionUtil.getMethodDeclared("setUseSimpleShadows", 
            scrollClass, 1
        );
    }

    private static final float TOOLTIP_DELAY = 0.3f; // TODO replace with SettingsAPI.getTooltipDelay()
    private static final float SCROLL_COOLDOWN = 0.15f;
    private static final int VISIBILITY_GRACE_FRAMES = 3;

    @Override
    public void renderBelow(UIEntityAPI element, float alpha) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        if (alpha > 0f) spec.internal_lastVisibleFrameCounter = 0;
    }

    @Override
    public void advance(UIEntityAPI element, float delta) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);
        spec.internal_lastVisibleFrameCounter++;

        if (shouldForceHide(spec, element)) {
            focusLost(element);
            return;
        }

        if (spec.internal_tp != null && spec.internal_tp.isFinished()) {
            focusLost(element);
            return;
        }

        final boolean hovering = input.hoveredLastFrame && !input.hasLMBClickedBefore;
        final boolean suppressed = input.LMBDownLastFrame || input.RMBDownLastFrame
            || Mouse.isButtonDown(2) || Mouse.getDWheel() != 0;

        if (!hovering) spec.internal_dismissedThisHover = false;

        final boolean shouldShow = hovering
            && !suppressed
            && spec.builder != null
            && !spec.internal_dismissedThisHover;

        if (shouldShow) {
            spec.internal_hoverTime += delta;
            if (spec.internal_hoverTime > TOOLTIP_DELAY && spec.internal_tp == null) {
                spec.internal_tp = UITooltip.create(spec);
                spec.internal_tp.attach();
            }
            if (spec.internal_tp != null) {
                spec.positioner.position(spec.internal_tp, spec.internal_tp.isExpanded());
            }
        } else {
            spec.internal_hoverTime = suppressed ? -SCROLL_COOLDOWN : 0f;
            if (spec.internal_tp != null) spec.internal_tp.dismiss();
        }
    }

    @Override
    public void onRemove(UIEntityAPI element) {
        focusLost(element);
    }

    public void focusLost(UIEntityAPI element) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        if (spec.internal_tp != null) spec.internal_tp.detach();
        spec.internal_tp = null;
        spec.internal_hoverTime = 0f;
    }

    private static final boolean shouldForceHide(TooltipComp spec, UIEntityAPI element) {
        return spec.internal_lastVisibleFrameCounter > VISIBILITY_GRACE_FRAMES ||
            element.getOpacity() <= 0f ||
            element.getFader().isFadedOut() ||
            !spec.enabled;
    }
}