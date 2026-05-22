package wfg.native_ui.ui.system;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.pad;

import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.internal.ui.core.UITooltip;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.panel.CustomPanel;

public final class TooltipSystem extends BaseSystem {

    private static final TooltipSystem INSTANCE = new TooltipSystem();
    public static TooltipSystem get() { return INSTANCE;}
    private TooltipSystem() {}

    @Override
    public void init(CustomPanel element) {
        element.comp().setIfNotPresent(NativeComponents.TOOLTIP, new TooltipComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    public static final CustomPanelAPI customPanel = settings.createCustom(pad, pad, null);
    public static final Object scrollPanelConstr;
    public static final Object setContentSizeMethod;
    public static final Object setSizeMethod;
    public static final Object setMaxShadowHeightMethod;
    public static final Object setUseSimpleShadowsMethod;

    static {
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

    // TODO fix bug causing tooltip to be visible even when its owner is invisible
    @Override
    public final void advance(final CustomPanel element, float delta) {
        final var comp = element.comp();
        final TooltipComp spec = comp.get(NativeComponents.TOOLTIP);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);

        if (!spec.enabled) {
            spec.internal_hoverTime = 0f;
            hideTooltip(spec);
            return;
        }

        if (spec.builder != null && input.hoveredLastFrame && !input.hasLMBClickedBefore) {
            spec.internal_hoverTime += delta;
            if (spec.internal_hoverTime >= spec.delay) {
                showTooltip(spec);
                spec.positioner.position(spec.internal_tp, false);
            }
        } else {
            spec.internal_hoverTime = 0f;
            hideTooltip(spec);
        }
    }

    public final void showTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) return;

        final var tp = createTp(spec);
        tp.createImpl(false);
        spec.internal_tp = tp;

        tp.attach();
    }

    public final void hideTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) {
            spec.internal_tp.detach();
            spec.internal_tp = null;
        }
    }

    private static final UITooltip createTp(TooltipComp spec) {
        final UITooltip tp = new UITooltip(
            spec.width - 10f, spec.expandable, spec.useScroller
        ) {
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