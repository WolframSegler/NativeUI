package wfg.native_ui.ui;

import static wfg.native_ui.util.UIConstants.*;
import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.ui.impl.StandardTooltipV2Expandable;

import rolflectionlib.util.RolfLectionUtil;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.system.TooltipSystem;
import wfg.native_ui.ui.widget.Button;
import wfg.native_ui.ui.widget.CheckboxButton;

public final class ComponentFactory {
    private ComponentFactory() {}

    /**
     * Use {@link CheckboxButton} if you want the checkbox sprite.
     */
    public static final Button createCheckboxWithText(
        int btnSize, String text, String font, CallbackRunnable<Button> onClick, 
        Color txtColor, int textAndBtnGap
    ) {
        final Button checkbox = new Button(btnSize, btnSize, null, null, onClick);
        checkbox.bgAlpha = 1f;
        checkbox.bgDisabledAlpha = 1f;
        checkbox.bgSelectedColor = new Color(60, 230, 250);
        checkbox.setQuickMode(false);

        final LabelAPI label = settings.createLabel(
            text, font == null ? Fonts.DEFAULT_SMALL : font
        );
        final int lblH = (int) label.computeTextHeight(text);
        label.setColor(txtColor == null ? base : txtColor);

        final int maxH = Math.max(lblH, btnSize);

        checkbox.add(label).inBL(btnSize + textAndBtnGap, (maxH - lblH) / 2f);

        return checkbox;
    }

    /**
     * @param panel will exclusively be used by the caption&value pair
     */
    public static final void addCaptionValueBlock(UIPanelAPI panel, String captionTxt,
        String valueTxt, Color captionColor
    ) { addCaptionValueBlock(panel, captionTxt, valueTxt, captionColor, highlight); }

    /**
     * @param panel will exclusively be used by the caption&value pair
     */
    public static final void addCaptionValueBlock(UIPanelAPI panel, String captionTxt,
        String valueTxt, Color captionColor, Color valueColor
    ) {
        addCaptionValueBlock(panel, captionTxt, valueTxt, Fonts.ORBITRON_12, Fonts.INSIGNIA_VERY_LARGE,
            captionColor, valueColor, true, 0f
        );
    }

    /**
     * @param panel will exclusively be used by the caption&value pair
     */
    public static final void addCaptionValueBlock(UIPanelAPI panel, String captionTxt,
        String valueTxt, Color captionColor, float maxW
    ) { addCaptionValueBlock(panel, captionTxt, valueTxt, captionColor, highlight, maxW); }

    /**
     * @param panel will exclusively be used by the caption&value pair
     */
    public static final void addCaptionValueBlock(UIPanelAPI panel, String captionTxt,
        String valueTxt, Color captionColor, Color valueColor, float maxW
    ) {
        addCaptionValueBlock(panel, captionTxt, valueTxt, Fonts.ORBITRON_12, Fonts.INSIGNIA_VERY_LARGE,
            captionColor, valueColor, true, maxW
        );
    }

    /**
     * @param panel will exclusively be used by the caption&value pair
     */
    public static final void addCaptionValueBlock(UIPanelAPI panel, String captionTxt, String valueTxt,
        String captionFont, String valueFont, Color captionColor, Color valueColor,
        boolean highlightOnHover, float maxW
    ) {
        final LabelAPI captionLbl = settings.createLabel(captionTxt, captionFont);
        captionLbl.setColor(captionColor);
        captionLbl.setHighlightOnMouseover(highlightOnHover);

        final LabelAPI valueLbl = settings.createLabel(valueTxt, valueFont);
        valueLbl.setColor(valueColor);
        valueLbl.setHighlightOnMouseover(highlightOnHover);

        if (maxW > 0f) {
            layoutCaptionValueLabels(maxW, panel, captionLbl, valueLbl);
        } else {
            layoutCaptionValueLabels(panel, captionLbl, valueLbl);
        }
    }

    public static final void layoutCaptionValueLabels(
        UIPanelAPI panel, LabelAPI captionLbl, LabelAPI valueLbl
    ) {
        final float panelW = Math.max(
            captionLbl.computeTextWidth(captionLbl.getText()),
            valueLbl.computeTextWidth(valueLbl.getText())
        );
        layoutCaptionValueLabels(panelW, panel, captionLbl, valueLbl);
    }

    public static final void layoutCaptionValueLabels(float maxW,
        UIPanelAPI panel, LabelAPI captionLbl, LabelAPI valueLbl
    ) {
        captionLbl.autoSizeToWidth(maxW);
        valueLbl.autoSizeToWidth(maxW);

        captionLbl.setAlignment(Alignment.MID);
        valueLbl.setAlignment(Alignment.MID);        

        final float captionH = captionLbl.getPosition().getHeight();
        final float valueH = valueLbl.getPosition().getHeight();

        panel.addComponent((UIComponentAPI) captionLbl).inTL(0f, 0f).setSize(maxW, captionH);
        panel.addComponent((UIComponentAPI) valueLbl).inTL(0f, captionH + pad).setSize(maxW, valueH);

        panel.getPosition().setSize(maxW, captionH + pad + valueH);
    }

    public static final StandardTooltipV2Expandable createTooltip(float width, boolean withScroller) {
        final StandardTooltipV2Expandable tp = new StandardTooltipV2Expandable(
            width - 10f - (withScroller ? 5f : 0f), false
        ) {
            @Override
            public void createImpl(boolean bl) {}
        };
        tp.setShowBorder(false);
        tp.setShowBackground(false);
        tp.setSelfRemove(true);

        return tp;
    }

    public static final PositionAPI addTooltip(TooltipMakerAPI tooltip, float h, boolean withScroller) {
        return addTooltip(tooltip, h, withScroller, Attachments.getScreenPanel());
    }

    public static final PositionAPI addTooltip(TooltipMakerAPI tooltip, float h, boolean withScroller,
        UIPanelAPI parent
    ) {
        tooltip.setForceProcessInput(true);
        final StandardTooltipV2Expandable tp = (StandardTooltipV2Expandable) tooltip;
        StandardTooltipV2Expandable.updateSizeAsUIElement(tp);

        final ScrollPanelAPI scrollPanel = withScroller ? wrapWithScrollPanel(tp, tp.getWidth(), h) : null;
        final UIComponentAPI comp = withScroller ? scrollPanel : tp;

        tp.setExternalScroller(scrollPanel);
        if (!withScroller) comp.getPosition().setSize(tp.getWidth(), Math.max(tp.getHeight(), h));

        return parent.addComponent(comp);
    }

    // TODO Replace reflection calls with API calls once new update drops
    public static final ScrollPanelAPI wrapWithScrollPanel(UIComponentAPI content, float viewWidth, float viewHeight) {
        final var scrollPanel = (ScrollPanelAPI) RolfLectionUtil.instantiateClass(
            TooltipSystem.scrollPanelConstr
        );
        RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setContentSizeMethod, scrollPanel,
            content.getPosition().getWidth(), content.getPosition().getHeight()
        );
        RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setSizeMethod, scrollPanel,
            viewWidth + 5f, viewHeight
        );
        RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setMaxShadowHeightMethod, scrollPanel,
            15f
        );
        RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setUseSimpleShadowsMethod, scrollPanel,
            true
        );
        if (content instanceof UIElementAPI element) {
            element.wrap(scrollPanel).inTL(0f, 0f);
        } else {
            scrollPanel.addComponent(content).inTL(0f, 0f);
        }
        return scrollPanel;
    }
}