package wfg.native_ui.ui;

import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
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
import wfg.native_ui.ui.panels.Button;
import wfg.native_ui.ui.systems.TooltipSystem;
import wfg.native_ui.util.CallbackRunnable;

public class ComponentFactory {
    public static final Button createCheckboxWithText(
        UIPanelAPI parent, int btnSize, String text, String font, CallbackRunnable<Button> onClick, 
        Color txtColor, int textAndBtnGap
    ) {
        final SettingsAPI settings = Global.getSettings();
        final Button checkbox = new Button(
            parent, btnSize, btnSize, null, null, onClick
        );
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
        final SettingsAPI settings = Global.getSettings();

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
        if (tooltip instanceof StandardTooltipV2Expandable tp) {
            StandardTooltipV2Expandable.updateSizeAsUIElement(tp);
            if (withScroller) {
                final var scrollPanel = (ScrollPanelAPI) RolfLectionUtil.instantiateClass(
                    TooltipSystem.scrollPanelConstr
                );
                RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setContentSizeMethod, scrollPanel,
                    tp.getWidth(), tp.getHeight()
                );
                RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setSizeMethod, scrollPanel,
                    tp.getWidth() + 5f, h
                );
                RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setMaxShadowHeightMethod, scrollPanel,
                    15f
                );
                RolfLectionUtil.invokeMethodDirectly(TooltipSystem.setUseSimpleShadowsMethod, scrollPanel,
                    true
                );
                scrollPanel.addComponent(tooltip).inTL(0f, 0f);
                tooltip.setExternalScroller(scrollPanel);
                return parent.addComponent(scrollPanel);
            } else {
                tooltip.getPosition().setSize(tp.getWidth(), Math.max(tp.getHeight(), h));
                return parent.addComponent(tooltip);
            }
        } else {
            throw new IllegalArgumentException(
                "addTooltip only supports StandardTooltipV2Expandable instances. " +
                "Provided tooltip of type " + tooltip.getClass().getName() + " is invalid."
            );
        }
    }
}