package wfg.native_ui.example.widget;

import static wfg.native_ui.util.UIConstants.highlight;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.ui.Fonts;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.component.GlowComp.GlowType;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.visual.AbstractSpriteElement.SpriteElement;
import wfg.native_ui.ui.widget.Button;
import wfg.native_ui.ui.widget.Button.CutStyle;
import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.NativeUiUtils.AnchorType;

public final class ButtonExample extends UIContainer implements UIBuildableAPI {
    public ButtonExample(float width, float height) {
        super(width, height);

        buildUI();
    }

    @Override
    public void buildUI() {

        // Color toggle button
        final Button colorBtn = new Button(60, 20, "color", Fonts.DEFAULT_SMALL, (btn) -> {
            btn.setChecked(!btn.isChecked()); // when an onClicked listener is used, the button does not automatically toggle the checked state.
            btn.bgColor = btn.isChecked() ? Color.GREEN : Color.RED;
        });
        add(colorBtn).inTL(0f, 0f);


        // Counting button
        final Button countBtn = new Button(60, 20, "0", Fonts.DEFAULT_SMALL, (btn) -> {
            btn.customData = ((Integer) btn.customData).intValue() + 1;
            btn.setText(btn.customData.toString());
        });
        countBtn.customData = 0;
        countBtn.setQuickMode(true); // we don't want to toggle the button. Technically not needed, since a custom onClicked listener is used.
        countBtn.setCutStyle(CutStyle.TL_BR);
        add(countBtn).inTL(0f, 30f);
        

        // subclassed button
        final BtnWithImage imgBtn = new BtnWithImage(60, 20, Global.getSector().getEconomy().getMarketsCopy().get(5));
        add(imgBtn).inTL(0f, 60f);
    }

    public static class BtnWithImage extends Button {
        public BtnWithImage(float width, float height, MarketAPI market) {
            super(width, height, null, null, null);

            onClicked = (btn) -> {
                Global.getSector().adjustPlayerReputation(RepActions.TRADE_EFFECT, market.getFactionId());
            };
            setShortcut(Keyboard.KEY_V);
            setAppendShortcutToText(false);
            bgAlpha = 0f;
            bgDisabledAlpha = 0f;

            tooltip.builder = (tp, expanded) -> {
                tp.addPara("Trade with " + market.getFaction().getDisplayName(), 0f,
                    highlight, Keyboard.getKeyName(interaction.shortcut)
                );
            };
            tooltip.positioner = (tp, expanded) -> {
                NativeUiUtils.anchorPanel(tp, this, AnchorType.RightTop, 15);
            };

            final String iconId = market.getFaction().getLogo();
            final SpriteElement icon = new SpriteElement(width, height, iconId, null, null);
            add(icon).inBL(0f, 0f);
            glow.type = GlowType.ADDITIVE;
            glow.additiveSprite = icon.getSprite();
        }
    }
}