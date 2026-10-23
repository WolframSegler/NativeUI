package wfg.native_ui.ui.widget;

import org.lwjgl.input.Keyboard;
import java.awt.Color;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.util.FaderUtil;

import wfg.native_ui.example.widget.ButtonExample;
import wfg.native_ui.ui.component.HoverGlowComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.TooltipComp;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.core.UIElementFlags.HasHoverGlow;
import wfg.native_ui.ui.core.UIElementFlags.HasTooltip;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.functional.ShortcutHandler;
import wfg.native_ui.ui.interaction.UIClickable;
import wfg.native_ui.util.RenderUtils;

/**
 * UI button component with visual enhancements: label, tooltip, hover glow, and custom background.
 *
 * <ul>
 *   <li>Displays an optional text label; the label is rebuilt when text/font/shortcut changes (see {@link #recreateLabel()}).</li>
 *   <li>Supports a persistent hover/glow state (driven by {@link #checked}, {@link #quickMode} and {@link #disabled}).</li>
 *   <li>Use {@link #setEnabled()} to enable/disable the button. Disabled buttons use {@link #bgDisabledColor}/{@link #bgDisabledAlpha}.</li>
 *   <li>{@link #setShowTooltipWhileInactive()} controls whether the tooltip is shown when the button is disabled.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link ButtonExample}</p>
 */
public class Button extends UIClickable<Button> implements UIBuildableAPI, ButtonAPI,
    HasHoverGlow, HasTooltip
{
    private static final float GLOW_BRIGHTNESS = 0.2f;
    private static final float FLASH_BRIGHTNESS = 0.3f;

    public final TooltipComp tooltip = comp().get(NativeComponents.TOOLTIP);
    protected final HoverGlowComp glow = comp().get(NativeComponents.HOVER_GLOW);

    public float bgAlpha = 0.9f;
    public float bgDisabledAlpha = 0.8f;
    public float highlightBrightness = 0.85f; // override for general brightness
    public Color bgSelectedColor = dark;
    public Color bgColor = dark;
    public Color bgDisabledColor = new Color(17, 52, 62);
    public int overrideCutSize = 0;
    
    protected CutStyle cutStyle = CutStyle.NONE;
    protected LabelAPI label = null;
    protected String labelText;
    protected String labelFont;
    protected Alignment alg = Alignment.MID;
    protected FaderUtil highlightFader = new FaderUtil(0f, 0.05f, 0.25f);
    
    private boolean appendShortcutToText = false;
    private boolean showTooltipWhileInactive = false;
    
    /**
     * @param onClick if null, clicking toggles the checked state; otherwise, the Runnable handles it.
     */
    public Button(float width, float height, String text, String font,
        CallbackRunnable<Button> onClick
    ) {
        super(width, height, onClick);

        labelText = text == null ? "" : text;
        labelFont = font == null ? Fonts.ORBITRON_12 : font;

        glow.fader = new FaderUtil(0f, 0.05f, 0.25f, false, true);
        glow.color = base;
        glow.glowBrightness = GLOW_BRIGHTNESS;
        glow.flashBrightness = FLASH_BRIGHTNESS;
        glow.faderMaskVertices = getFaderMaskVertices();

        final ShortcutHandler<Button> uiClickableCallback = interaction.onShortcutPressed;
        interaction.onShortcutPressed = (source, event) -> {
            if (getOpacity() > 0f) {
                flash(false);
                label.flash(0.2f, 0.2f);
            }
            uiClickableCallback.run(source, event);
        };

        buildUI();
    }

    public void recreateLabel() {
        final LabelAPI newlbl = settings.createLabel(labelText, labelFont);

        final String shortcutTxt = (!appendShortcutToText || interaction.shortcut == 0) ? "" :
            " [" + Keyboard.getKeyName(interaction.shortcut) + "]";

        newlbl.setText(labelText + shortcutTxt);
        newlbl.getPosition().setSize(getWidth(), getHeight());
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

    public void buildUI() {
        if (label != null) remove(label);

        final String shortcutTxt = (!appendShortcutToText || interaction.shortcut == 0) ? "" :
            " [" + Keyboard.getKeyName(interaction.shortcut) + "]";

        label = settings.createLabel(labelText + shortcutTxt, labelFont);
        label.getPosition().setSize(getWidth(), getHeight());
        label.setColor(btnTxtColor);
        label.setAlignment(alg);
        if (appendShortcutToText) {
            label.setHighlightColor(highlight);
            label.setHighlight(Keyboard.getKeyName(interaction.shortcut));
        }

        add(label).inBL(0f, 0f);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        tooltip.enabled = isTooltipEnabled();
        glow.persistent = isPersistentGlow();
    }

    @Override
    public void setChecked(boolean bool) {
        super.setChecked(bool);
        glow.persistent = isPersistentGlow();
    }

    @Override
    public void setQuickMode(boolean mode) {
        super.setQuickMode(mode);
        glow.persistent = isPersistentGlow();
    }

    public void setShowTooltipWhileInactive(boolean bool) {
        showTooltipWhileInactive = bool;
        tooltip.enabled = isTooltipEnabled();
    }

    /**
     * @param keyCode the key code corresponding to {@link org.lwjgl.input.Keyboard} constants
     */
    @Override
    public void setShortcut(int keyCode) {
        super.setShortcut(keyCode);
        recreateLabel();
    }

    public void setShortcutAndAppendToText(int keyCode) {
        super.setShortcut(keyCode);
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

    public Color getLabelColor() {
        return label.getColor();
    }

    public void setLabelColor(Color color) {
        label.setColor(color);
    }

    public void setAlignment(Alignment alg) {
        this.alg = alg;
        label.setAlignment(alg);
    }

    @Override
    public PositionAPI setSize(float w, float h) {
        setSizeWithoutRecreate(w, h);
        recreateLabel();

        return mPos;
    }

    protected final void setSizeWithoutRecreate(float w, float h) {
        super.setSize(w, h);
        glow.faderMaskVertices = getFaderMaskVertices();
    }

    @Override
    public void renderBelowImpl(float alpha) {
        final float w = getWidth();
        final float h = getHeight();
        final float cutSize = computeCut((int) w, (int) h);

        final float[] cuts = cutStyle.toVector4();
        for (int i = 0; i < 4; i++) cuts[i] *= cutSize;
        final float[] verts = RenderUtils.buildCornersVertices(getX(), getY(), w, h, cuts);

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

    public void setCutStyle(CutStyle style) {
        cutStyle = style;
        glow.faderMaskVertices = getFaderMaskVertices();
    }

    public void setCutStyle(com.fs.starfarer.api.ui.CutStyle vanillaStyle) {
        cutStyle = CutStyle.fromVanilla(vanillaStyle);
        if (cutStyle == null) cutStyle = CutStyle.NONE;
        glow.faderMaskVertices = getFaderMaskVertices();
    }

    public void setClickable(boolean bool) {
        clickable = bool;
    }

    public Object getCustomData() {
        return customData;
    }

    public void setCustomData(Object data) {
        customData = data;
    }

    public void setMouseOverSound(String sound) {
        mouseOverSound = sound;
    }

    public boolean isRightClicksOkWhenDisabled() {
        return rightClicksOkWhenDisabled;
    }

    @Override
    public void setRightClicksOkWhenDisabled(boolean bool) {
        rightClicksOkWhenDisabled = bool;
    }

    public void setShortcut(int lwjgl_key, boolean putLast) {
        setShortcut(lwjgl_key);
    }

    public boolean isPerformActionWhenDisabled() {
        return performActionWhenDisabled;
    }

    public void setButtonDisabledPressedSound(String sound) {
        disabledSound = sound;
    }

    public void setButtonPressedSound(String sound) {
        pressedSound = sound;
    }

    public void setPerformActionWhenDisabled(boolean bool) {
        performActionWhenDisabled = bool;
    }

    public float getGlowBrightness() {
        return glow.glowBrightness;
    }

    public float getFlashBrightness() {
        return glow.flashBrightness;
    }

    public float getHighlightBrightness() {
        return highlightBrightness;
    }

    public void setGlowBrightness(float brightness) {
        glow.glowBrightness = brightness;
    }

    public void setFlashBrightness(float brightness) {
        glow.flashBrightness = brightness;
    }

    public void setHighlightBrightness(float brightness) {
        highlightBrightness = brightness;
    }

    public void highlight() {
        this.highlightFader.fadeIn();
    }

    public boolean isHighlighted() {
        return this.highlightFader.isFadedIn() || this.highlightFader.isFadingIn();
    }

    public void unhighlight() {
        this.highlightFader.fadeOut();
    }
    
    public void setHighlightBounceDown(boolean bool) {
        highlightFader.setBounceDown(bool);
    }

    @Override
    public void flash() {
        flash(true);
    }

    @Override
    public void flash(boolean withSound) {
        flash(withSound, 0.05f, 0.25f);
    }

    @Override
    public void flash(boolean withSound, float in, float out) {
        glow.fader.fadeIn();

        if (withSound) playPressSound();
    }

    @Deprecated
    public void setSkipPlayingPressedSoundOnce(boolean bool) {}
    @Deprecated
    public boolean isSkipPlayingPressedSoundOnce() { return false; }

    @Override
    public void renderImpl(float alpha) {
        final float highlight = highlightFader.getBrightness() * highlightBrightness;
        glow.overrideBrightness = (highlight > 0f) ? highlight : -1f;

        super.renderImpl(alpha);
    }

    protected float[] getFaderMaskVertices() {
        final float cutSize = computeCut((int) getWidth(), (int) getHeight());

        final float[] cuts = cutStyle.toVector4();
        for (int i = 0; i < 4; i++) cuts[i] *= cutSize;

        return RenderUtils.buildCornersVertices(
            getX(), getY(), getWidth(), getHeight(), cuts
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

        public static final CutStyle fromVanilla(com.fs.starfarer.api.ui.CutStyle vanilla) {
            if (vanilla == null) return null;
            switch (vanilla) {
                case TL_BR: return TL_BR;
                case BL_TR: return TR_BL;
                case BOTTOM: return BL_BR;
                case TOP: return TL_TR;
                case ALL: return ALL;
                case C2_MENU: return TR_BL;
                case NONE: default: return NONE;
            }
        }
    }
}