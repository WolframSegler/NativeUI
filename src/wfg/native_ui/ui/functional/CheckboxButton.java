package wfg.native_ui.ui.functional;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.component.HoverGlowComp.GlowType;
import wfg.native_ui.util.CallbackRunnable;

/**
 * A UI Component consisting of a toggleable checkbox and an optional text label.
 *
 * <p>The visual size of the checkbox is determined by {@code checkboxSize}, while the
 * {@link UICheckboxSize} enum selects which vanilla sprite set is used as the base asset.
 * The sprite is stretched to the desired size when rendered.</p>
 *
 * <p>The checkbox can be clicked to toggle its state. A callback can be attached to react
 * to state changes. The label, if present, is rendered next to the checkbox and is
 * automatically repositioned if the checkbox size changes.</p>
 */
public class CheckboxButton extends Button {
    private static final SettingsAPI settings = Global.getSettings();
    private static final SpriteAPI S_TOGGLE_OFF = settings.getSprite("ui", "toggle12_off");
    private static final SpriteAPI S_TOGGLE_ON = settings.getSprite("ui", "toggle12_on");
    private static final SpriteAPI M_TOGGLE_OFF = settings.getSprite("ui", "toggle20_off");
    private static final SpriteAPI M_TOGGLE_ON = settings.getSprite("ui", "toggle20_on");
    private static final SpriteAPI L_TOGGLE_OFF = settings.getSprite("ui", "toggle40_off");
    private static final SpriteAPI L_TOGGLE_ON = settings.getSprite("ui", "toggle40_on");
    private static final SpriteAPI S_TOGGLE_ON_GLOW = settings.getSprite("ui", "toggle12_on2");
    private static final SpriteAPI M_TOGGLE_ON_GLOW = settings.getSprite("ui", "toggle20_on2");
    private static final SpriteAPI L_TOGGLE_ON_GLOW = settings.getSprite("ui", "toggle40_on2");
    
    private static final float hpad = 5f;

    private final SpriteAPI TOGGLE_OFF;
    private final SpriteAPI TOGGLE_ON;

    public float checkboxSize = 0f;

    /**
     * @param labelText the text label displayed next to the checkbox. May be null if no label is desired.
     * @param checkboxSizeEnum the vanilla checkbox asset variant to use.
     * @param btnSize the rendered size of the checkbox in pixels. The underlying sprite will be stretched to this size.
     */
    public CheckboxButton(UIPanelAPI parent, int btnSize, String text, String font,
        CallbackRunnable<Button> onClick, UICheckboxSize type, boolean useGlowTex
    ) {
        super(parent, btnSize, btnSize, text, font, onClick);
        bgAlpha = 0f;
        bgDisabledAlpha = 0f;
        glow.overlayBrightness = 0f;
        glow.type = GlowType.ADDITIVE;

        switch (type) {
        default: case TINY:
            TOGGLE_OFF = S_TOGGLE_OFF;
            TOGGLE_ON = useGlowTex ? S_TOGGLE_ON_GLOW : S_TOGGLE_ON;
            break;

        case SMALL:
            TOGGLE_OFF = M_TOGGLE_OFF;
            TOGGLE_ON = useGlowTex ? M_TOGGLE_ON_GLOW : M_TOGGLE_ON;
            break;

        case LARGE:
            TOGGLE_OFF = L_TOGGLE_OFF;
            TOGGLE_ON = useGlowTex ? L_TOGGLE_ON_GLOW : L_TOGGLE_ON;
            break;
        }

        checkboxSize = btnSize;

        customLblMods();
    }

    @Override
    public void render(float alpha) {
        final SpriteAPI toggleTex = isChecked() ? TOGGLE_ON : TOGGLE_OFF;
        glow.additiveSprite = toggleTex;

        toggleTex.setSize(checkboxSize, checkboxSize);
        toggleTex.render(pos.getX(), pos.getY());
        
        super.render(alpha);
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);

        for (InputEventAPI event : events) {
            if (event.isMouseMoveEvent()) {
                if (label.getPosition().containsEvent(event)) {
                    label.flash(0f, 0.3f);
                }
            }
        }
    }

    @Override
    public void recreateLabel() {
        super.recreateLabel();

        customLblMods();
    }

    @Override
    public boolean isPersistentGlow() {
        return false;
    }

    private final void customLblMods() {
        final float lblSize = label.computeTextWidth(label.getText());
        setAlignment(Alignment.LMID);
        label.getPosition().setSize(lblSize, checkboxSize);
        label.getPosition().inBL(checkboxSize + hpad, 0f);
        pos.setSize(checkboxSize + hpad + lblSize, checkboxSize);
    }
}