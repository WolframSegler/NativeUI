package wfg.native_ui.ui.panels;

import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.ui.components.HoverGlowComp;
import wfg.native_ui.ui.components.InteractionComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.TooltipComp;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.core.UIElementFlags.HasHoverGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasInteraction;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.core.UIElementFlags.HasUIContext;
import wfg.native_ui.util.CallbackRunnable;
import wfg.native_ui.util.RenderUtils;

/**
 * <p><strong>Component access policy:</strong></p>
 * <ul>
 *   <li><b>Public components</b> expose supported customization points and may be read or modified
 *       directly by external code.</li>
 *   <li><b>Protected components</b> are internal implementation details and must only be accessed
 *       by this class or subclasses.</li>
 *   <li>If a panel provides a setter for a value that affects component state, that setter
 *       <b>must be used</b> instead of mutating the component directly.</li>
 * </ul>
 *
 * <p>
 * This distinction makes supported extension points explicit while allowing systems to
 * freely read component data.
 * </p>
 */
public class Button extends CustomPanel<Button> implements 
    HasHoverGlow, HasInteraction, HasTooltip, HasUIContext
{
    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    public final UIContextComp context = comp().get(NativeComponents.UI_CONTEXT);
    protected final HoverGlowComp glow = comp().get(NativeComponents.HOVER_GLOW);
    protected final InteractionComp<Button> interaction = comp().get(NativeComponents.INTERACTION);

    public float bgAlpha = 0.9f;
    public float bgDisabledAlpha = 0.8f;
    public boolean clickable = true;
    public boolean rightClicksOkWhenDisabled = false;
    public boolean performActionWhenDisabled = false;
    public boolean disabledWhileInvisible = true;
    public boolean soundEnabled = true;
    public Color bgSelectedColor = dark;
    public Color bgColor = dark;
    public Color bgDisabledColor = new Color(17, 52, 62);
    public Object customData = null;
    public String mouseOverSound = "ui_button_mouseover";
    public CallbackRunnable<Button> onClicked;
    public CutStyle cutStyle = CutStyle.NONE;
    public int overrideCutSize = 0;

    protected LabelAPI label = null;
    protected String labelText;
    protected String labelFont;
    protected Alignment alg = Alignment.MID;
    
    private boolean appendShortcutToText = false;
    private boolean disabled = false;
    private boolean checked = false;
    private boolean quickMode = false;
    private boolean showTooltipWhileInactive = false;
    
    /**
     * @param onClick if null, clicking toggles the checked state; otherwise, the Runnable handles it.
     */
    public Button(UIPanelAPI parent, int width, int height, String text, String font,
        CallbackRunnable<Button> onClick
    ) {
        super(parent, width, height);

        labelText = text == null ? "" : text;
        labelFont = font == null ? Fonts.ORBITRON_12 : font;
        this.onClicked = onClick;

        context.ignore = true;

        glow.fader = new FaderUtil(0, 0, 0.2f, false, true);
        glow.color = base;
        glow.overlayBrightness = 0.2f;
        glow.faderMaskVertices = getFaderMaskVertices();

        interaction.onClicked = (source, isLeftClick) -> {
            if ((!isLeftClick && !rightClicksOkWhenDisabled) || !clickable) return;

            interaction.onShortcutPressed.run(source);
        };
        interaction.onHoverStarted = (source) -> {
            Global.getSoundPlayer().playUISound(mouseOverSound, 1, 1);
        };
        interaction.onShortcutPressed = (source) -> {
            if (getPanel().getOpacity() <= 0f && !disabledWhileInvisible) return;
            glow.fader.forceIn();

            if (soundEnabled) {
                if (disabled && !performActionWhenDisabled) {
                    Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1, 1);
                    return;
                } else {
                    Global.getSoundPlayer().playUISound("ui_button_pressed", 1, 1);
                }
            }

            if (onClicked != null) {
                onClicked.run(this);
            } else if (!quickMode) checked = !checked;

            label.flash(0.2f, 1f);
        };

        createPanel();
    }

    public void recreateLabel() {
        final LabelAPI newlbl = Global.getSettings().createLabel(labelText, labelFont);

        final String finalText = !appendShortcutToText ? labelText :
            labelText + " [" + Keyboard.getKeyName(interaction.shortcut) + "]";
        newlbl.setText(finalText);
        newlbl.getPosition().setSize(pos.getWidth(), pos.getHeight());
        newlbl.setColor(label.getColor());
        newlbl.setAlignment(alg);
        if (appendShortcutToText) {
            newlbl.setHighlightColor(highlight);
            newlbl.setHighlight(Keyboard.getKeyName(interaction.shortcut));
        }

        remove(label);
        label = newlbl;
        add(label).inBL(0f, 0f);
    }

    public void createPanel() {
        if (label != null) remove(label);

        final String finalText = !appendShortcutToText ? labelText :
            labelText + " [" + Keyboard.getKeyName(interaction.shortcut) + "]";
        label = Global.getSettings().createLabel(finalText, labelFont);
        label.getPosition().setSize(pos.getWidth(), pos.getHeight());
        label.setColor(btnTxtColor);
        label.setAlignment(alg);
        if (appendShortcutToText) {
            label.setHighlightColor(highlight);
            label.setHighlight(Keyboard.getKeyName(interaction.shortcut));
        }

        add(label).inBL(0f, 0f);
    }

    public boolean getEnabled() { return !disabled; }
    public void setEnabled(boolean enabled) {
        disabled = !enabled;
        tooltip.enabled = isTooltipEnabled();
        glow.persistent = isPersistentGlow();
    }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean bool) {
        checked = bool;
        glow.persistent = isPersistentGlow();
    }

    public boolean isQuickMode() { return quickMode; }
    public void setQuickMode(boolean mode) {
        quickMode = mode;
        glow.persistent = isPersistentGlow();
    }

    public void setShowTooltipWhileInactive(boolean bool) {
        showTooltipWhileInactive = bool;
        tooltip.enabled = isTooltipEnabled();
    }

    public void click(boolean ignoreState) {
        if (ignoreState) interaction.onShortcutPressed.run(this);
        else interaction.onClicked.handle(this, true);
    }

    /**
     * @param keyCode the key code corresponding to {@link org.lwjgl.input.Keyboard} constants
     */
    public void setShortcut(int keyCode) {
        interaction.shortcut = keyCode;
        appendShortcutToText = true;
        recreateLabel();
    }

    public String getText() { return labelText; }
    public void setText(String text) {
        labelText = text;
        recreateLabel();
    }

    public void setFont(String font) {
        labelFont = font;
        recreateLabel();
    }

    public void setAppendShortcutToText(boolean a) {
        appendShortcutToText = a;
        recreateLabel();
    }

    public void setHighlightBounceDown(boolean bool) {
        glow.fader.setBounceDown(bool);
    }

    public void setHighlightBrightness(float brightness) {
        glow.overlayBrightness = brightness;
    }

    public Color getLabelColor() {
        return label.getColor();
    }

    public void setLabelColor(Color color) {
        label.setColor(color);
    }

    public void setAlignment(Alignment alg) {
        this.alg = alg;
        recreateLabel();
    }

    @Override
    public void positionChanged(PositionAPI position) {
        super.positionChanged(position);
        
        glow.faderMaskVertices = getFaderMaskVertices();
        recreateLabel();
    }

    @Override
    public void renderBelow(float alpha) {
        super.renderBelow(alpha);
        
        final float x = pos.getX();
        final float y = pos.getY();
        final float w = pos.getWidth();
        final float h = pos.getHeight();
        final float cutSize = computeCut((int) w, (int) h);

        final float[] cuts = cutStyle.toVector4();
        for (int i = 0; i < 4; i++) cuts[i] *= cutSize;
        final float[] verts = RenderUtils.buildCornersVertices(x, y, w, h, cuts);

        RenderUtils.drawPolygon(verts, getBgColor(), alpha * getBgAlpha());
    }

    protected float computeCut(int w, int h) {
        if (overrideCutSize > 0) return overrideCutSize;
        return Math.min(w, h) * 0.2f;
    }

    protected boolean isTooltipEnabled() {
        return (showTooltipWhileInactive || !disabled);
    }

    protected boolean isPersistentGlow() {
        return checked && !disabled && !quickMode;
    }

    protected Color getBgColor() {
        if (disabled) return bgDisabledColor;
        if (!quickMode && checked) return bgSelectedColor;
        return bgColor;
    }

    protected float getBgAlpha() {
        return disabled ? bgDisabledAlpha : bgAlpha;
    }

    protected float[] getFaderMaskVertices() {
        final float cutSize = computeCut((int) pos.getWidth(), (int) pos.getHeight());

        final float[] cuts = cutStyle.toVector4();
        for (int i = 0; i < 4; i++) cuts[i] *= cutSize;

        return RenderUtils.buildCornersVertices(
            pos.getX(),
            pos.getY(),
            pos.getWidth(),
            pos.getHeight(),
            cuts
        );
    }

    public enum CutStyle {
        NONE, TL, TR, BL, BR,
        TL_TR, TL_BL, TL_BR,
        TR_BL, TR_BR, BL_BR,
        TL_TR_BL, TL_TR_BR, TL_BL_BR,
        TR_BL_BR, ALL;

        /**  
         * 4-element array of the corners: [BL, BR, TR, TL]  
         * 1f = cut, 0f = no cut
         */
        public float[] toVector4() {
            switch (this) {
                case TL:       return new float[]{0f, 0f, 0f, 1f};
                case TR:       return new float[]{0f, 0f, 1f, 0f};
                case BL:       return new float[]{1f, 0f, 0f, 0f};
                case BR:       return new float[]{0f, 1f, 0f, 0f};
                case TL_TR:    return new float[]{0f, 0f, 1f, 1f};
                case TL_BL:    return new float[]{1f, 0f, 0f, 1f};
                case TL_BR:    return new float[]{0f, 1f, 0f, 1f};
                case TR_BL:    return new float[]{1f, 0f, 1f, 0f};
                case TR_BR:    return new float[]{0f, 1f, 1f, 0f};
                case BL_BR:    return new float[]{1f, 1f, 0f, 0f};
                case TL_TR_BL: return new float[]{1f, 0f, 1f, 1f};
                case TL_TR_BR: return new float[]{0f, 1f, 1f, 1f};
                case TL_BL_BR: return new float[]{1f, 1f, 0f, 1f};
                case TR_BL_BR: return new float[]{1f, 1f, 1f, 0f};
                case ALL:      return new float[]{1f, 1f, 1f, 1f};
                default:       return new float[]{0f, 0f, 0f, 0f};
            }
        }
    }
}