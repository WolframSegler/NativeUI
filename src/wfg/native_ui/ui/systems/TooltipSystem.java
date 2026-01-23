package wfg.native_ui.ui.systems;

import static wfg.native_ui.util.UIConstants.pad;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.ComponentFactory;
import wfg.native_ui.ui.components.InputSnapshot;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.TooltipComp;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.panels.CustomPanel;

public final class TooltipSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

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

    private final TooltipComp spec;
    private final UIContextComp context;

    public TooltipSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.TOOLTIP, new TooltipComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());

        spec = comp.get(NativeComponents.TOOLTIP);
        context = comp.get(NativeComponents.UI_CONTEXT);
    }

    @Override
    public final void advance(float delta, InputSnapshot input) {
        if (!spec.enabled || !context.isValid()) {
            spec.hoverTime_internal = 0f;
            hideTooltip();
            return;
        }

        if (input.hoveredLastFrame && !input.hasLMBClickedBefore && spec.builder != null) {
            spec.hoverTime_internal += delta;
            if (spec.hoverTime_internal >= spec.delay) {
                showTooltip();
            }
        } else {
            spec.hoverTime_internal = 0f;
            hideTooltip();
        }
    }

    private final void showTooltip() {
        if (spec.tp_internal != null) return;

        final var tp = createTp(spec);
        tp.createImpl(false);
        spec.tp_internal = tp;

        ComponentFactory.addTooltip(tp, 0f, spec.useScroller, spec.parent);
        spec.positioner.position(tp, false);
        spec.parent.bringComponentToTop(tp);
    }

    private final void hideTooltip() {
        if (spec.tp_internal != null) {
            spec.parent.removeComponent(spec.tp_internal);
            spec.tp_internal = null;
        }
    }

    private static final StandardTooltipV2Expandable createTp(TooltipComp spec) {
        final StandardTooltipV2Expandable tp = new StandardTooltipV2Expandable(
            spec.width - 10f - (spec.useScroller ? 5f : 0f), spec.expandable
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