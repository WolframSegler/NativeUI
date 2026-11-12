package wfg.native_ui.example.interaction;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.campaign.FactionSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.codex.CodexDataV2;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;

import wfg.native_ui.example.table.GridTableExample.WidgetSelectionState;
import wfg.native_ui.internal.util.BorderRenderer;
import wfg.native_ui.ui.component.GlowComp;
import wfg.native_ui.ui.component.GlowComp.GlowType;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.container.BaseContainer;
import wfg.native_ui.ui.core.UIElementFlags.HasGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.interaction.UIClickable;
import wfg.native_ui.ui.table.WidgetAPI;
import wfg.native_ui.ui.visual.IconValuePair;
import wfg.native_ui.ui.visual.InteractiveSprite;
import wfg.native_ui.util.ArrayMap;
import wfg.native_ui.util.NativeUiUtils;
import wfg.native_ui.util.NumFormat;

/**
 * This example creates a clickable widget with a glow and a tooltip. The widget uses the style colors of the provided faction.
 */
public final class UIClickableExample extends UIClickable<UIClickableExample> implements
    WidgetAPI<UIClickableExample>, HasTooltip, HasGlow
{
    public static final int WIDTH = 320;
    public static final int HEIGHT = 60;
    public static final Color AMBER_BG = new Color(84, 61, 32);
    public static final Color WIDGET_BG = new Color(28, 35, 48, 240);

    private final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    private final GlowComp glow = comp().get(NativeComponents.GLOW);
    private final BorderRenderer border = new BorderRenderer(UI_BORDER_4, true, WIDTH, HEIGHT);

    private final PlannedOrder order;
    private final ShipHullSpecAPI spec;

    public int index = 0;

    public WidgetSelectionState selectionState = WidgetSelectionState.NONE;

    public UIClickableExample(PlannedOrder order, int index, FactionSpecAPI faction) {
        super(WIDTH, HEIGHT, null);
        this.order = order;
        this.index = index;

        glow.type = GlowType.UNDERLAY;
        glow.glowBrightness = 0.6f;
        glow.flashBrightness = 0.9f;
        glow.color = faction.getBrightUIColor();

        border.centerColor = WIDGET_BG;

        spec = settings.getHullSpec(order.hullId);

        tooltip.codexID = CodexDataV2.getShipEntryId(order.hullId);
        tooltip.builder = (tp, expanded) -> {
            final String capitalName = getFactionCapital(faction.getId());

            tp.addTitle("Ship Order", base);

            tp.addPara("The %1$s production order will enter the queue once the required resources are allocated. A total cost of %2$s will be charged to the faction's capital, %3$s, and construction is expected to take %4$s before the ship is ready for service.",
                pad, new Color[]{base, highlight, faction.getBaseUIColor(), highlight},
                spec.getHullNameWithDashClass(),
                NumFormat.formatCreditAbs(order.credits), capitalName,
                getTimeWithDay(order.days)
            );

            final int gridWidth = 390;
            final int valueWidth = 40;
            int rowCount = 0;

            tp.addPara("Required Resources", base, opad);
            tp.beginGridFlipped(gridWidth, 2, valueWidth, hpad);
            for (var e : order.commodities.singleEntrySet()) {
                final CommoditySpecAPI com = settings.getCommoditySpec(e.getKey());
                final String amountStr = NumFormat.engNotate(e.getValue());

                tp.addToGrid(0, rowCount++, com.getName(), amountStr);
            }
            tp.addGrid(0);

            tp.addPara("%1$s + %2$s to remove instantly.", opad, highlight, "Shift", "Click");
        };

        buildUI();
    }

    @Override
    public void buildUI() {
        clearChildren();

        final SpriteAPI sprite = settings.getSprite(spec.getSpriteName());

        final int maxSize = HEIGHT - opad;
        final float spriteW = sprite.getWidth();
        final float spriteH = sprite.getHeight();
        final float scale = Math.min(maxSize / spriteW, maxSize / spriteH);
        final int scaledW = (int) (spriteW * scale);
        final int scaledH = (int) (spriteH * scale);

        final InteractiveSprite shipSprite = new InteractiveSprite(scaledW, scaledH, sprite, null, null);
        shipSprite.tooltip.enabled = false;
        shipSprite.audio.enabled = false;
        shipSprite.glow.setOwner(glow);
        shipSprite.glow.type = GlowType.ADDITIVE;
        shipSprite.glow.glowBrightness = 0.8f;
        shipSprite.glow.flashBrightness = 1.2f;
        add(shipSprite).inLMid(hpad + (maxSize - scaledW) / 2);

        final String shipStr = spec.getHullNameWithDashClass();
        final String costStr = NumFormat.formatCredit(order.credits);
        final String timeStr = getTimeWithDay(order.days);
        final String gapStr = " • ";
        final LabelAPI topSection = settings.createLabel(
            shipStr + gapStr + costStr + gapStr + timeStr, Fonts.DEFAULT_SMALL
        );
        topSection.setHighlightColors(highlight, base);
        topSection.setHighlight(costStr, timeStr);
        topSection.setAlignment(Alignment.LMID);
        topSection.autoSizeToWidth(WIDTH - HEIGHT);
        add(topSection).inTL(HEIGHT, hpad);

        final int iconS = 28;
        final int pairW = iconS + 60; 
        int currW = HEIGHT + opad;
        for (var e : order.commodities.singleEntrySet()) {
            final CommoditySpecAPI spec = settings.getCommoditySpec(e.getKey());
            final IconValuePair pair = new IconValuePair(pairW, iconS, spec.getIconName(), e.getValue(),
                true, null
            );

            add(pair).inBL(currW, hpad);
            currW += pairW + opad;

            if (currW > WIDTH) {
                remove(pair);
                break;
            }
        }

        if (selectionState != WidgetSelectionState.NONE) {
            final BaseContainer bgPanel = new BaseContainer(getWidth(), getHeight());
            add(bgPanel);
            bgPanel.bg.alpha = 0.8f;
            bgPanel.bg.color = AMBER_BG;
            bgPanel.bg.offset.setOffset(4, 4, -8, -8);

            if (selectionState == WidgetSelectionState.REMOVE) {
                final LabelAPI removeLabel = settings.createLabel("Click to Remove", Fonts.DEFAULT_SMALL);
                removeLabel.setColor(base);
                removeLabel.setHighlightColor(
                    NativeUiUtils.adjustBrightness(removeLabel.getColor(), 1.33f)
                );
                bgPanel.add(removeLabel).inMid();
    
            } else if (selectionState == WidgetSelectionState.SWAP) {
                final LabelAPI swapLabel = settings.createLabel("Click to Swap", Fonts.DEFAULT_SMALL);
                swapLabel.setColor(base);
                swapLabel.setHighlightColor(
                    NativeUiUtils.adjustBrightness(swapLabel.getColor(), 1.33f)
                );
                bgPanel.add(swapLabel).inMid();
            }
        }
    }

    @Override
    public void renderBelowImpl(float alpha) {
        border.render(getX(), getY(), alpha);
    }

    public InteractionComp<UIClickableExample> getInteraction() {
        return interaction;
    }

    private static final String getTimeWithDay(int val) {
        return Integer.toString(val) + " " + (val == 1 ? "day" : "days");
    }

    private static final String getFactionCapital(String factionId) {
        return "Kazeron"; // the faction capital name
    }

    /** Example data structure for hull orders */
    public static class PlannedOrder {
        public String hullId;
        public ArrayMap<String, Float> commodities;
        public long credits;
        public int days;
    }
}