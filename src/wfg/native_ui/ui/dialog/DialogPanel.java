package wfg.native_ui.ui.dialog;

import java.util.Map;

import org.lwjgl.input.Keyboard;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.internal.ui.dialog.FoldingPanel;
import wfg.native_ui.internal.ui.dialog.ModalDialog;
import wfg.native_ui.ui.core.UIBuildableAPI;
import wfg.native_ui.ui.functional.Button;
import wfg.native_ui.ui.functional.Button.CutStyle;
import wfg.native_ui.util.ArrayMap;
import wfg.native_ui.util.CallbackRunnable;
import wfg.native_ui.util.RunnableWithCode;

/**
<p>
A modal, fold-animated dialog panel with a built-in <em>holo</em> ({@link FoldingPanel}) frame.

<p><strong>Important implementation notes</strong></p>
<ul>
<li><strong>Ownership:</strong> {@link DialogPanel#m_panel} is owned and positioned by {@link DialogPanel#holo}.
Do <em>not</em> assign {@link DialogPanel#m_panel} to any other parent. Use {@link FoldingPanel#setNext(m_panel)} instead.</li>
<li><strong>Buttons:</strong> Buttons map to integer options stored in {@link DialogPanel#optionsMap}.</li>
</ul>

<p><strong>Typical usage</strong></p>
<pre><code>
final DialogPanel dlg = new DialogPanel(
    (code) -&gt; {...},
    "Confirm action", // default text
    "Confirm", "Cancel" // button texts (0 = Confirm, 1 = Cancel)
);

// optionally set keyboard shortcut for the "confirm" button (index 0)
dlg.setConfirmShortcut();

// show with fade in / out durations (seconds)
dlg.show(0.5f, 0.5f);
</code></pre>

<p><strong>Subclassing / customization</strong></p>
<ul>
<li>Subclass and populate using {@link #buildUI()}, add labels, tables or other components, and register buttons in {@link DialogPanel#optionsMap}.</li>
<li>Override {@link #outsideClickAbsorbed(InputEventAPI)} to provide custom functionality.</li>

</ul>

<p><strong>Subclass example</strong></p>
<pre><code>public class MyConfirmDialog extends DialogPanel {
    public MyConfirmDialog(RunnableWithCode done) {
        super(420, 220, done);
    }

    &#64;Override
    public void buildUI() {
        final LabelAPI text2 = Global.getSettings().createLabel(
            "Extra info", Fonts.INSIGNIA_LARGE
        );
        add(text2).inTL(opad, 50);
    }

    &#64;Override
    public void outsideClickAbsorbed(InputEventAPI e) {
        getHolo().flickerNoise(0f, 0.5f);
    }
}
</code></pre>
*/
public class DialogPanel extends ModalDialog implements UIBuildableAPI, CallbackRunnable<Button> {
    public boolean noiseOnConfirmDismiss = true;
    public final FoldingPanel holo;
    public final Map<Button, Integer> optionsMap = new ArrayMap<>();

    public DialogPanel(int w, int h, RunnableWithCode onDismissed) {
        super(w, h + BUTTON_H + pad + opad, onDismissed);

        holo = new FoldingPanel(w, h + BUTTON_H + pad + opad,
            UI_BORDER_1, 7
        );

        holo.getPos().inMid();
        holo.forceFoldIn();

        holo.transitionEnabled = false;
        holo.setNext(m_panel);
    }

    public DialogPanel(RunnableWithCode onDismissed, String txt, String... btnText) {
        this(500, 200, onDismissed, txt, btnText);
    }

    public DialogPanel(int w, int h, RunnableWithCode onDismissed,
        String txt, String... btnText
    ) { this(w, h, text_color, btnBgColorDark, onDismissed, txt, btnText); }

    public DialogPanel(int w, int h, Color btnTxtColor, Color btnBgColor,
        RunnableWithCode onDismissed, String txt, String... btnTextArr
    ) {
        this(w, h, onDismissed);

        if (txt != null && !txt.equals("")) {
            final LabelAPI txtLbl = settings.createLabel(
                txt, Fonts.INSIGNIA_LARGE
            );
            add(txtLbl);
            txtLbl.setColor(btnTxtColor);
            txtLbl.getPosition().setSize(
                pos.getWidth(), pos.getHeight() - BUTTON_H
            ).inTL(0f, 0f);
            txtLbl.setAlignment(Alignment.TL);
        }

        if (btnTextArr != null && btnTextArr.length > 0) {
            Button prevBtn = null;
            for(int i = btnTextArr.length - 1; i >= 0; i--) {
                final String BtnTxt = btnTextArr[i];
                if (BtnTxt == null) continue;

                final CallbackRunnable<Button> run = (btn) -> {
                    final Integer optionValue = optionsMap.get(btn);
                    if (optionValue == null) dismiss(1);
                    else dismiss(optionValue);
                };
    
                final Button btn = new Button(m_panel, BUTTON_W, BUTTON_H, BtnTxt,
                    Fonts.ORBITRON_20AA, run
                );
                btn.setAlignment(Alignment.MID);
                btn.cutStyle = CutStyle.TL_BR;
                btn.setQuickMode(true);
                optionsMap.put(btn, i);
                add(btn);

                if (prevBtn == null) btn.getPos().inBR(pad, pad);
                else btn.getPos().leftOfMid(prevBtn.getPanel(), pad*2);
    
                prevBtn = btn;
            }
        }
    }

    /** Override */
    public void buildUI() {}

    public PositionAPI setSize(float w, float h) {
        holo.setSize(w, h);
        return pos.setSize(w, h);
    }

    public PositionAPI sizeToInner(float w, float h) {
        return setSize(w + holo.borderThickness * 3, h + holo.borderThickness * 3);
    }

    public void setConfirmShortcut() {
        optionsMap.keySet().forEach(b -> {
            if (optionsMap.get(b) == 0) { b.setShortcut(Keyboard.KEY_G);}
        });
    }

    public void run(Button btn)  {
        final Integer optionValue = optionsMap.get(btn);
        if (optionValue == null) dismiss(1);
        else dismiss(optionValue);
    }

    @Override
    public void dismiss(int option) {
        holo.foldIn(fader.getDurationOut() * 0.5f);
        if (noiseOnConfirmDismiss || option != 0) {
            holo.flickerNoise(0f, 1f);
        }

        super.dismiss(option);
    }

    public void show(float durIn, float durOut) {
        super.show(durIn, durOut);

        holo.getParent().bringComponentToTop(holo.getPanel());
        holo.foldOut(fader.getDurationIn() * 0.5f);
        holo.flickerNoise(0f, 1f);
    }

    @Override
    public void outsideClickAbsorbed(InputEventAPI event) {
        holo.flickerNoise(0f, 0.5f);
    }

    public Button getButton(int id) {
        for (Map.Entry<Button, Integer> entry : optionsMap.entrySet()) {
            if (entry.getValue() == id) return entry.getKey();
        }
        return null;
    }
}