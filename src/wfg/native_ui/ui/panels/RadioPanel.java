package wfg.native_ui.ui.panels;

import static wfg.native_ui.util.UIConstants.pad;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;

import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.panels.Button.CutStyle;
import wfg.native_ui.util.CallbackRunnable;
import wfg.native_ui.util.RunnableWithCode;

/**
 * A simple radio selection panel that presents multiple mutually exclusive options.
 *
 * <p>Options must be added via {@link #addOption(String)} or {@link #addOption(String, boolean)}
 * before calling {@link #buildUI()}. The selected option can be queried or changed
 * using {@link #getSelectedIndex()} and {@link #setSelectedIndex(int)}.</p>
 *
 * <p>The panel supports a callback {@link #optionSelected} which is invoked whenever
 * the user selects a different option. The callback receives the index of the newly
 * selected option.</p>
 *
 * <p>The generated UI buttons are accessible through {@link #getButtons()} if further
 * customization is required.</p>
 *
 * <p>Layout behavior:</p>
 * <ul>
 *   <li>{@link LayoutMode#VERTICAL} – options are stacked vertically using checkbox buttons.</li>
 *   <li>{@link LayoutMode#HORIZONTAL} – options are distributed evenly across the panel width.</li>
 * </ul>
 */
public class RadioPanel extends CustomPanel<RadioPanel> implements UIBuildableAPI {
    public enum LayoutMode {
        HORIZONTAL, VERTICAL
    }

    private final List<String> optionTexts = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final LayoutMode mode;
    private int selectedIndex = 0;

    public RunnableWithCode optionSelected;

    public int checkboxSize = 20;
    public String font = Fonts.DEFAULT_SMALL;
    public UICheckboxSize checkboxType = UICheckboxSize.SMALL;

    /** Used for horizontal buttons */
    public int buttonHeight = 32;

    public RadioPanel(UIPanelAPI parent, int width, int height, LayoutMode mode) {
        super(parent, width, height);

        this.mode = mode;
    }

    public final RadioPanel addOption(String text) {
        return addOption(text, false);
    }

    public final RadioPanel addOption(String text, boolean selected) {
        optionTexts.add(text);
        
        return this;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public void buildUI() {
        buttons.clear();
        clearChildren();

        final CallbackRunnable<Button> run = (btn) -> {
            buttons.forEach(b -> b.setChecked(false));
            btn.setChecked(true);
            selectedIndex = (int) btn.customData;
            if (optionSelected != null) optionSelected.run(selectedIndex);
        };

        switch (mode) {
        default: case VERTICAL:
            for (int i = 0; i < optionTexts.size(); i++) {
                final CheckboxButton checkbox = new CheckboxButton(
                    m_panel, checkboxSize, optionTexts.get(i), font,
                    run, checkboxType, false
                );
                checkbox.customData = i;
                buttons.add(checkbox);
                add(checkbox).inTL(pad, pad + (pad + checkboxSize) * i);

                if (selectedIndex == i) checkbox.setChecked(true);
            }
            break;
    
        case HORIZONTAL:
            final int count = optionTexts.size();
            final float totalGap = pad * (count - 1);
            final float available = pos.getWidth() - pad * 2 - totalGap;
            final int buttonWidth = (int) (available / count);

            for (int i = 0; i < count; i++) {
                final Button button = new Button(m_panel, buttonWidth, checkboxSize,
                    optionTexts.get(i), font, run
                );

                button.customData = i;
                buttons.add(button);

                final float x = pad + i * (buttonWidth + pad);
                add(button).inTL(x, pad);

                if (i == 0) button.cutStyle = CutStyle.TL_BL;
                if (i == count - 1) button.cutStyle = CutStyle.TR_BR;

                if (selectedIndex == i) button.setChecked(true);
            }
            break;
        }
    }
}