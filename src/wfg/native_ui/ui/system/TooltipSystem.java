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

    @Override
    public void advance(UIEntityAPI element, float delta) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);

        if (element.getOpacity() <= 0f || element.getFader().isFadedOut() || !spec.enabled) {
            forceHideTooltip(spec);
            spec.internal_hoverTime = 0f;
            return;
        }

        if (spec.internal_tp != null) {
            spec.internal_tp.updateFader(delta);
            if (spec.internal_tp.isDetached()) {
                spec.internal_tp = null;
            }
        }

        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);

        final boolean hovering = input.hoveredLastFrame && !input.hasLMBClickedBefore;
        final boolean scrolled = Mouse.getDWheel() != 0;
        final boolean anyMouseDown = input.LMBDownLastFrame || input.RMBDownLastFrame || Mouse.isButtonDown(2) || scrolled;

        if (hovering && !anyMouseDown && spec.builder != null) {
            spec.internal_hoverTime += delta;
            if (spec.internal_hoverTime > TOOLTIP_DELAY) {
                showTooltip(spec);
            }
            if (spec.internal_tp != null) {
                spec.positioner.position(spec.internal_tp, spec.internal_tp.isExpanded());
            }
        } else {
            spec.internal_hoverTime = scrolled ? -SCROLL_COOLDOWN : 0f;
            hideTooltip(spec);
        }
    }

    // TODO implement after update
    public void focusLost(UIEntityAPI element) {
        final TooltipComp spec = element.comp().get(NativeComponents.TOOLTIP);
        spec.internal_hoverTime = 0f;
        hideTooltip(spec);
    }

    @Override
    public void onRemove(UIEntityAPI element) {
        forceHideTooltip(element.comp().get(NativeComponents.TOOLTIP));
    }

    public void showTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) return;

        final UITooltip tp = createTp(spec);
        tp.createImpl(false);
        spec.internal_tp = tp;

        tp.beforeShown();

        tp.attachAndFadeIn();
        spec.positioner.position(tp, false);

        tp.notifyShown();
    }

    public void hideTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) {
            spec.internal_tp.fadeOutAndHide();
        }
    }

    public void forceHideTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) {
            spec.internal_tp.detach();
            spec.internal_tp = null;
        }
        spec.internal_hoverTime = 0f;
    }

    private static final UITooltip createTp(TooltipComp spec) {
        final UITooltip tp = new UITooltip(spec.width - 10f, spec.expandable) {
            
            @Override
            public void createImpl(boolean expanded) {
                if (expanded) {
                    expandString = spec.expandTxt == null ? "%s more info" : spec.expandTxt;
                } else {
                    unexpandString = spec.unexpandTxt == null ? "%s hide" : spec.unexpandTxt;
                }
                spec.builder.buildTp(this, expanded);
            }
        };
        tp.setShowBorder(true);
        tp.setShowBackground(true);
        tp.setSelfRemove(true);
        tp.setBgAlpha(spec.bgAlpha);

        if (spec.codexID != null) tp.setCodexEntryId(spec.codexID);

        return tp;
    }
}