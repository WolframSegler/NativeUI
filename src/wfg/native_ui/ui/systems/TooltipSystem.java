package wfg.native_ui.ui.systems;

import static wfg.native_ui.util.UIConstants.pad;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.components.InputSnapshotComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.TooltipComp;
import wfg.native_ui.ui.components.UIComponentContainer;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.core.UITooltip;
import wfg.native_ui.ui.panels.CustomPanel;

public final class TooltipSystem extends BaseSystem {

    private static final TooltipSystem INSTANCE = new TooltipSystem();
    public static TooltipSystem get() { return INSTANCE;}
    private TooltipSystem() {}

    @Override
    public void init(CustomPanel<?> element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.TOOLTIP, new TooltipComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    public static final CustomPanelAPI customPanel = Global.getSettings().createCustom(pad, pad, null);
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
    public final void advance(final CustomPanel<?> element, float delta) {
        final var comp = element.comp();
        final TooltipComp spec = comp.get(NativeComponents.TOOLTIP);
        final UIContextComp context = comp.get(NativeComponents.UI_CONTEXT);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);

        if (!spec.enabled || !context.isValid()) {
            spec.internal_hoverTime = 0f;
            hideTooltip(spec);
            return;
        }

        if (spec.builder != null && input.hoveredLastFrame && !input.hasLMBClickedBefore) {
            spec.internal_hoverTime += delta;
            if (spec.internal_hoverTime >= spec.delay) {
                showTooltip(spec);
            }
        } else {
            spec.internal_hoverTime = 0f;
            hideTooltip(spec);
        }
    }

    private final void showTooltip(TooltipComp spec) {
        if (spec.internal_tp != null) return;

        final var tp = createTp(spec);
        tp.createImpl(false);
        spec.internal_tp = tp;

        tp.attach();
        spec.positioner.position(tp, false);
    }

    private final void hideTooltip(TooltipComp spec) {
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