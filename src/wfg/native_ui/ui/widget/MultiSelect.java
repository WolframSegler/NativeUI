package wfg.native_ui.ui.widget;

import static wfg.native_ui.util.UIConstants.pad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.ui.ButtonAPI.UICheckboxSize;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.functional.Button;
import wfg.native_ui.ui.functional.CheckboxButton;
import wfg.native_ui.ui.functional.Button.CutStyle;
import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.ui.widget.RadioPanel.LayoutMode;
import wfg.native_ui.util.CallbackRunnable;

public class MultiSelect extends CustomPanel implements UIBuildableAPI {
    private final List<String> options = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final Set<Integer> selectedIndexes = new HashSet<>();
    private final LayoutMode mode;

    public CallbackRunnable<MultiSelect> onSelected;

    public int checkboxSize = 20;
    public String font = Fonts.DEFAULT_SMALL;
    public UICheckboxSize checkboxType = UICheckboxSize.SMALL;

    public MultiSelect(UIPanelAPI parent, int width, int height, Iterable<String> options, LayoutMode mode) {
        super(parent, width, height);

        for (String opt : options) this.options.add(opt);
        this.mode = mode;
    }

    public final void selectFirst(String option) {
        select(options.indexOf(option));
    }

    public final void deselectFirst(String option) {
        deselect(options.indexOf(option));
    }

    public final void toggleFirst(String option) {
        toggle(options.indexOf(option));
    }

    public final void select(int index) {
        if (index < 0 || buttons.isEmpty()) return;
        selectedIndexes.add(index);
        final Button btn = buttons.get(index);
        if (btn != null) btn.setChecked(true);
    }

    public final void deselect(int index) {
        if (index < 0 || buttons.isEmpty()) return;
        selectedIndexes.remove(index);
        final Button btn = buttons.get(index);
        if (btn != null) btn.setChecked(false);
    }

    public final void toggle(int index) {
        if (selectedIndexes.contains(index)) deselect(index); 
        else select(index);
    }

    public final List<Button> getButtons() {
        return buttons;
    }

    public final Set<Integer> getSelectedIndexes() {
        return Collections.unmodifiableSet(selectedIndexes);
    }

    public final List<String> getSelectedStrings() {
        final List<String> selected = new ArrayList<>(selectedIndexes.size());
        for (Integer index : selectedIndexes) selected.add(options.get(index));
        return selected;
    }

    @Override
    public void buildUI() {
        buttons.clear();
        clearChildren();

        final CallbackRunnable<Button> run = (btn) -> {
            toggle(buttons.indexOf(btn));
            if (onSelected != null) onSelected.run(this);
        };

        switch (mode) {
        default: case VERTICAL:
            for (int i = 0; i < options.size(); i++) {
                final CheckboxButton checkbox = new CheckboxButton(
                    m_panel, checkboxSize, options.get(i), font,
                    run, checkboxType, false
                );
                checkbox.customData = i;
                buttons.add(checkbox);
                add(checkbox).inTL(pad, pad + (pad + checkboxSize) * i);
            }
            break;
    
        case HORIZONTAL:
            final int count = options.size();
            final float totalGap = pad * (count - 1);
            final float available = pos.getWidth() - pad * 2 - totalGap;
            final int buttonWidth = (int) (available / count);

            for (int i = 0; i < count; i++) {
                final Button button = new Button(m_panel, buttonWidth, (int) pos.getHeight(),
                    options.get(i), font, run
                );

                button.customData = i;
                buttons.add(button);

                final float x = pad + i * (buttonWidth + pad);
                add(button).inTL(x, pad);

                if (i == 0) button.cutStyle = CutStyle.TL_BL;
                if (i == count - 1) button.cutStyle = CutStyle.TR_BR;
            }
            break;
        }

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setChecked(selectedIndexes.contains(i));
        }
    }
}