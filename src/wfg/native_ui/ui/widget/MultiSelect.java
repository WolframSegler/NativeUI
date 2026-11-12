package wfg.native_ui.ui.widget;

import static wfg.native_ui.util.UIConstants.pad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;
import com.fs.starfarer.api.ui.Fonts;

import wfg.native_ui.example.widget.MultiSelectExample;
import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.widget.Button.CutStyle;
import wfg.native_ui.ui.widget.RadioPanel.LayoutMode;

/**
 * Multi‑selection panel that presents multiple independently selectable options.
 *
 * <p>Options are provided at construction and rendered according to the
 * chosen {@link LayoutMode}. The selected indices and their
 * corresponding strings can be retrieved with {@link #getSelectedIndexes()}
 * and {@link #getSelectedStrings()}.</p>
 *
 * <p>The panel supports a callback {@link #onSelected} which is invoked
 * whenever the user clicks an option to change its state. Individual button states are automatically
 * synchronized with the internal selection set when the panel is built
 * via {@link #buildUI()}.</p>
 *
 * <p>Layout behavior:</p>
 * <ul>
 *   <li>{@link LayoutMode#VERTICAL} – options are stacked vertically
 *       using checkbox buttons.</li>
 *   <li>{@link LayoutMode#HORIZONTAL} – options are distributed evenly
 *       across the panel width as toggle buttons with cut styles applied
 *       to the first and last buttons.</li>
 * </ul>
 * 
 * <p><strong>Example: </strong> {@link MultiSelectExample}</p>
 */
public class MultiSelect extends UIContainer implements UIBuildableAPI {
    private final List<String> mOptions = new ArrayList<>();
    private final List<Button> mButtons = new ArrayList<>();
    private final Set<Integer> mSelectedIndexes = new HashSet<>();
    private final LayoutMode mMode;

    public CallbackRunnable<Button> onSelected;

    public int checkboxSize = 20;
    public String font = Fonts.DEFAULT_SMALL;
    public UICheckboxSize checkboxType = UICheckboxSize.SMALL;

    public MultiSelect(float width, float height, Iterable<String> options, LayoutMode mode) {
        super(width, height);

        for (String opt : options) this.mOptions.add(opt);
        this.mMode = mode;
    }

    public final void selectFirst(String option) {
        select(mOptions.indexOf(option));
    }

    public final void deselectFirst(String option) {
        deselect(mOptions.indexOf(option));
    }

    public final void toggleFirst(String option) {
        toggle(mOptions.indexOf(option));
    }

    public final void select(int index) {
        if (index < 0 || mButtons.isEmpty()) return;
        mSelectedIndexes.add(index);
        final Button btn = mButtons.get(index);
        if (btn != null) btn.setChecked(true);
    }

    public final void deselect(int index) {
        if (index < 0 || mButtons.isEmpty()) return;
        mSelectedIndexes.remove(index);
        final Button btn = mButtons.get(index);
        if (btn != null) btn.setChecked(false);
    }

    public final void toggle(int index) {
        if (mSelectedIndexes.contains(index)) deselect(index); 
        else select(index);
    }

    public final List<Button> getButtons() {
        return mButtons;
    }

    public final Set<Integer> getSelectedIndexes() {
        return Collections.unmodifiableSet(mSelectedIndexes);
    }

    public final List<String> getSelectedStrings() {
        final List<String> selected = new ArrayList<>(mSelectedIndexes.size());
        for (Integer index : mSelectedIndexes) selected.add(mOptions.get(index));
        return selected;
    }

    @Override
    public void buildUI() {
        mButtons.clear();
        clearChildren();

        final CallbackRunnable<Button> run = (btn) -> {
            toggle((Integer) btn.customData);
            if (onSelected != null) onSelected.run(btn);
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
            }
            break;
    
        case HORIZONTAL:
            final int count = mOptions.size();
            final float totalGap = pad * (count - 1);
            final float available = getWidth() - pad * 2 - totalGap;
            final int buttonWidth = (int) (available / count);

            for (int i = 0; i < count; i++) {
                final Button button = new Button(buttonWidth, (int) getHeight(),
                    mOptions.get(i), font, run
                );

                button.customData = i;
                mButtons.add(button);

                final float x = pad + i * (buttonWidth + pad);
                add(button).inTL(x, 0f);

                if (i == 0) button.setCutStyle(CutStyle.TL_BL);
                if (i == count - 1) button.setCutStyle(CutStyle.TR_BR);
            }
            break;
        }

        for (int i = 0; i < mButtons.size(); i++) {
            mButtons.get(i).setChecked(mSelectedIndexes.contains(i));
        }
    }
}