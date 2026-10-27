package wfg.native_ui.ui.dialog;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.example.dialog.DialogPanelExample;
import wfg.native_ui.internal.ui.dialog.FoldingPanel;
import wfg.native_ui.internal.ui.dialog.ModalDialog;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.functional.CallbackRunnable;
import wfg.native_ui.ui.functional.RunnableWithCode;
import wfg.native_ui.ui.widget.Button;
import wfg.native_ui.ui.widget.Button.CutStyle;

/**
<p>
A modal, fold-animated dialog panel with a built-in <em>holo</em> ({@link FoldingPanel}) frame.

<p><strong>Important implementation notes</strong></p>
<ul>
<li><strong>Ownership:</strong> {@link DialogPanel} is owned and positioned by {@link DialogPanel#holo}.
Do <em>not</em> assign {@link DialogPanel} to any other parent. Use {@link FoldingPanel#setNext} instead.</li>
<li><strong>Buttons:</strong> Buttons map to integer options stored in {@link DialogPanel#buttons}.</li>
<li>Dismissing the dialog with confirmation keys exit with code <code>0</code>, while dismissing with cancel keys exit with code <code>1</code>.
See {@link ModalDialog#optionOnKeyboardConfirm} and {@link ModalDialog#optionOnKeyboardCancel} for changing the defaults.</li>
</ul>

<p><strong>Example: </strong> {@link DialogPanelExample}</p>
*/
public class DialogPanel extends ModalDialog implements UIBuildableAPI, CallbackRunnable<Button> {
    public boolean noiseOnConfirmDismiss = true;
    public final FoldingPanel holo;
    protected final ArrayList<Button> buttons = new ArrayList<>(4);

    public DialogPanel(float w, float h, RunnableWithCode onDismissed) {
        super(w, h + BUTTON_H + pad + opad, onDismissed);

        holo = new FoldingPanel(w, h + BUTTON_H + pad + opad,
            UI_BORDER_1, 7f
        );

        holo.pos().inMid();
        holo.forceFoldIn();

        holo.transitionEnabled = false;
        holo.setNext(this);
    }

    public DialogPanel(RunnableWithCode onDismissed, String txt, String... btnText) {
        this(500f, 200f, onDismissed, txt, btnText);
    }

    public DialogPanel(float w, float h, RunnableWithCode onDismissed,
        String txt, String... btnText
    ) { this(w, h, text_color, btnBgColorDark, onDismissed, txt, btnText); }

    public DialogPanel(float w, float h, Color btnTxtColor, Color btnBgColor,
        RunnableWithCode onDismissed, String txt, String... btnTextArr
    ) {
        this(w, h, onDismissed);

        if (txt != null && !txt.equals("")) {
            final LabelAPI txtLbl = settings.createLabel(
                txt, Fonts.INSIGNIA_LARGE
            );
            add(txtLbl);
            txtLbl.setColor(btnTxtColor);
            txtLbl.setAlignment(Alignment.TL);
            txtLbl.getPosition().setSize(getWidth(), getHeight() - BUTTON_H).inTL(0f, 0f);
        }

        if (btnTextArr != null && btnTextArr.length > 0) {
            for(int i = 0; i < btnTextArr.length; i++) {
                final String BtnTxt = btnTextArr[i];
                if (BtnTxt == null) continue;
    
                final Button btn = new Button(BUTTON_W, BUTTON_H, BtnTxt,
                    Fonts.ORBITRON_20AA, this
                );
                btn.setAlignment(Alignment.MID);
                btn.setCutStyle(CutStyle.TL_BR);
                btn.setQuickMode(true);
                btn.customData = Integer.valueOf(i);
                buttons.add(btn);
                add(btn);
            }

            for (int i = buttons.size() - 1; i >= 0; i--) {
                final Button btn = buttons.get(i);

                if (i == buttons.size() - 1) btn.pos().inBR(pad, pad);
                else btn.pos().leftOfMid(buttons.get(i + 1), pad*2);
            }
        }
    }

    /** Override */
    public void buildUI() {}

    public PositionAPI setSize(float w, float h) {
        holo.setSize(w, h);
        return mPos.setSize(w, h);
    }

    public PositionAPI sizeToInner(float w, float h) {
        return setSize(w + holo.borderThickness * 3f, h + holo.borderThickness * 3f);
    }

    public void setConfirmShortcut() {
        buttons.get(0).setShortcutAndAppendToText(Keyboard.KEY_G);
    }

    public void run(Button btn) {
        dismiss((Integer) btn.customData);
    }

    @Override
    public void dismiss(int option) {
        holo.foldIn(mFader.getDurationOut() * 0.5f);
        if (noiseOnConfirmDismiss || option != 0) {
            holo.flickerNoise(0f, 1f);
        }

        super.dismiss(option);
    }

    public void show(float durIn, float durOut) {
        super.show(durIn, durOut);
        holo.bringToFront();

        holo.foldOut(mFader.getDurationIn() * 0.5f);
        holo.flickerNoise(0f, 1f);
    }

    @Override
    public void outsideClickAbsorbed(InputEventAPI event) {
        holo.flickerNoise(0f, 0.5f);
    }

    public final Button getButton(int id) {
        return buttons.get(id);
    }
}