package wfg.native_ui.example.widget;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.graphics.SpriteAPI;

import wfg.native_ui.example.container.DockPanelExample;
import wfg.native_ui.ui.component.GlowComp.GlowType;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.visual.AbstractSpriteElement.SpriteElement;
import wfg.native_ui.ui.widget.DockButton;

/**
 * Example dock button with an icon instead of a standard background.
 */
public final class DockButtonExample extends DockButton<DockPanelExample> {
    private static final SpriteAPI ICON = settings.getSprite("icons", "example_icon");

    public DockButtonExample(int width, int height, UIBuildableAPI content) {
        super(width, height, null, null, () -> new DockPanelExample(content));

        setShortcut(Keyboard.KEY_2);
        setAppendShortcutToText(false);
        setShowTooltipWhileInactive(true);
        bgAlpha = 0f; // set bg to invisible
        bgDisabledAlpha = 0f; // set bg to invisible

        tooltip.builder = (tp, expanded) -> {
            tp.addPara("Example button tooltip text", 0f, highlight, Keyboard.getKeyName(interaction.shortcut));
        };

        final SpriteElement icon = new SpriteElement(width, height, ICON, null, null);
        add(icon).inBL(0f, 0f);
        glow.type = GlowType.ADDITIVE;
        glow.additiveSprite = icon.getSprite(); // set button sprite to picture sprite for additive glow
    }
}