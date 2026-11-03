package wfg.native_ui.ui.widget;

import static wfg.native_ui.util.UIConstants.pad;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;

import wfg.native_ui.example.widget.RadioPanelExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.functional.RunnableWithCode;
import wfg.native_ui.ui.widget.Button.CutStyle;

/**
 * Radio selection panel that presents multiple mutually exclusive options.
 *
 * <p>Options must be added via {@link #addOption(String)} or {@link #addOption(String, boolean)}
 * before calling {@link #buildUI}. The selected option can be queried or changed
 * using {@link #getSelectedIndex} and {@link #setSelectedIndex}.</p>
 *
 * <p>The panel supports a callback {@link #optionSelected} which is invoked whenever
 * the user selects a different option. The callback receives the index of the newly
 * selected option.</p>
 *
 * <p>The generated UI buttons are accessible through {@link #getButtons} if further
 * customization is required.</p>
 *
 * <p>Layout behavior:</p>
 * <ul>
 *   <li>{@link LayoutMode#VERTICAL} – options are stacked vertically using checkbox buttons.</li>
 *   <li>{@link LayoutMode#HORIZONTAL} – options are distributed evenly across the panel width.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link RadioPanelExample}</p>
 */
public class RadioPanel extends UIContainer implements UIBuildableAPI {
    public enum LayoutMode {
        HORIZONTAL, VERTICAL
    }

    private final List<String> mOptions = new ArrayList<>();
    private final List<Button> mButtons = new ArrayList<>();
    private final LayoutMode mMode;
    private int mSelectedIndex = 0;

    public RunnableWithCode optionSelected;

    public int checkboxSize = 20;
    public String font = Fonts.DEFAULT_SMALL;
    public UICheckboxSize checkboxType = UICheckboxSize.SMALL;

    public RadioPanel(float width, float height, LayoutMode mode) {
        super(width, height);

        this.mMode = mode;
    }

    public final RadioPanel addOption(String text) {
        return addOption(text, false);
    }

    public final RadioPanel addOption(String text, boolean selected) {
        mOptions.add(text);

        if (selected) setSelectedIndex(mOptions.size() - 1);
        
        return this;
    }

    public final void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public final int getSelectedIndex() {
        return mSelectedIndex;
    }

    public final List<Button> getButtons() {
        return mButtons;
    }

    public void buildUI() {
        mButtons.clear();
        clearChildren();

        final CallbackRunnable<Button> run = (btn) -> {
            mButtons.forEach(b -> b.setChecked(false));
            btn.setChecked(true);
            mSelectedIndex = (int) btn.customData;
            if (optionSelected != null) optionSelected.run(mSelectedIndex);
        };

        switch (mMode) {
        default: case VERTICAL:
            for (int i = 0; i < mOptions.size(); i++) {
                final CheckboxButton checkbox = new CheckboxButton(
                    checkboxSize, mOptions.get(i), font,
                    run, checkboxType, false
                );
                checkbox.customData = i;
                mButtons.add(checkbox);
                add(checkbox).inTL(pad, pad + (pad + checkboxSize) * i);

                if (mSelectedIndex == i) checkbox.setChecked(true);
            }
            break;
    
        case HORIZONTAL:
            final int count = mOptions.size();
            final float totalGap = pad * (count - 1);
            final float available = getWidth() - pad * 2 - totalGap;
            final int buttonWidth = (int) (available / count);

            for (int i = 0; i < count; i++) {
                final Button button = new Button(buttonWidth, getHeight(),
                    mOptions.get(i), font, run
                );

                button.customData = i;
                mButtons.add(button);

                final float x = pad + i * (buttonWidth + pad);
                add(button).inTL(x, 0f);

                if (i == 0) button.setCutStyle(CutStyle.TL_BL);
                if (i == count - 1) button.setCutStyle(CutStyle.TR_BR);

                if (mSelectedIndex == i) button.setChecked(true);
            }
            break;
        }
    }
}