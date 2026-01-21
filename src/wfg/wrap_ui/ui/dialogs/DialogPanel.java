package wfg.wrap_ui.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import static wfg.wrap_ui.util.UIConstants.*;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.wrap_ui.internal.ui.dialogs.FoldingPanel;
import wfg.wrap_ui.internal.ui.dialogs.ModalDialog;
import wfg.wrap_ui.ui.panels.Button;
import wfg.wrap_ui.ui.panels.Button.CutStyle;
import wfg.wrap_ui.util.CallbackRunnable;
import wfg.wrap_ui.util.RunnableWithCode;

/**
<p>
A modal, fold-animated dialog panel with a built-in <em>holo</em> ({@link FoldingPanel}) frame and an editable
inner content panel. This class combines:
</p>
<ul>
<li>a fullscreen modal behavior and input interception (from {@link ModalDialog}),</li>
<li>a folding / noise animated chrome (the {@link DialogPanel#holo} FoldingPanel),</li>
<li>and a simple button → integer option mapping via {@link DialogPanel#optionsMap}.</li>
</ul>

<p><strong>Important implementation notes</strong></p>
<ul>
<li><strong>Ownership:</strong> {@link DialogPanel#innerPanel} is owned and positioned by {@link DialogPanel#holo}.
Do <em>not</em> assign {@link DialogPanel#innerPanel} to any other parent. Use {@link FoldingPanel#setNext(innerPanel)} instead.</li>
<li><strong>Buttons:</strong> Buttons map to integer options stored in {@link DialogPanel#optionsMap}.</li>
</ul>

<p><strong>Typical usage</strong></p>
<pre><code>
final DialogPanel dlg = new DialogPanel(
    Attachments.getScreenPanel(), // parent
    (option) -&gt; {...},
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
<li>Subclass and populate {@link DialogPanel#innerPanel} in {@link #createPanel()}, add labels, tables or other components, and register buttons in {@link DialogPanel#optionsMap}.</li>
<li>Override {@link #outsideClickAbsorbed(InputEventAPI)} to provide custom functionality.</li>

</ul>

<p><strong>Subclass example</strong></p>
<pre><code>public class MyConfirmDialog extends DialogPanel {
    public MyConfirmDialog(UIPanelAPI parent, RunnableWithCode done) {
        super(parent, 420, 220, done);
    }

    &#64;Override
    public void createPanel() {
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
public class DialogPanel extends ModalDialog implements CallbackRunnable<Button> {
    public boolean noiseOnConfirmDismiss = true;
    public final UIPanelAPI innerPanel;
    public final FoldingPanel holo;
    public final Map<Button, Integer> optionsMap = new HashMap<>();

    public DialogPanel(UIPanelAPI parent, int w, int h, RunnableWithCode runnable) {
        super(parent, w, h + BUTTON_H + pad + opad, runnable);

        holo = new FoldingPanel(m_panel, w, h + BUTTON_H + pad + opad,
            "ui_border1", 7
        );
        add(holo);
        holo.getPos().inMid();
        holo.forceFoldIn();

        innerPanel = Global.getSettings().createCustom(0f, 0f, null); 

        holo.transitionEnabled = false;
        holo.setNext(innerPanel);
    }

    public DialogPanel(UIPanelAPI parent, RunnableWithCode runnable, String txt, String... btnText) {
        this(parent, 500, 200, runnable, txt, btnText);
    }

    public DialogPanel(UIPanelAPI parent, int w, int h, RunnableWithCode runnable,
        String txt, String... btnText
    ) { this(parent, w, h, btnTxtColor, btnBgColorDark, runnable, txt, btnText); }

    public DialogPanel(UIPanelAPI parent, int w, int h, Color btnTxtColor, Color btnBgColor,
        RunnableWithCode runnable, String txt, String... btnTextArr
    ) {
        this(parent, w, h, runnable);

        final PositionAPI innerPos = innerPanel.getPosition();

        if (txt != null && !txt.equals("")) {
            final LabelAPI txtLbl = Global.getSettings().createLabel(
                txt, Fonts.INSIGNIA_LARGE
            );
            innerPanel.addComponent((UIComponentAPI)txtLbl);
            txtLbl.setColor(btnTxtColor);
            txtLbl.getPosition().setSize(
                innerPos.getWidth() - opad*2,
                innerPos.getHeight() - BUTTON_H - opad*2
            ).inTL(opad, opad);
            txtLbl.setAlignment(Alignment.TL);
        }

        if (btnTextArr != null && btnTextArr.length > 0) {
            Button prevBtn = null;
            for(int i = btnTextArr.length - 1; i >= 0; i--) {
                final String BtnTxt = btnTextArr[i];
                if (BtnTxt == null) continue;
    
                final Button btn = new Button(innerPanel, BUTTON_W, BUTTON_H, BtnTxt,
                    Fonts.ORBITRON_20AA, this
                );
                btn.setAlignment(Alignment.MID);
                btn.cutStyle = CutStyle.TL_BR;
                btn.setQuickMode(true);
                optionsMap.put(btn, i);
                innerPanel.addComponent(btn.getPanel());

                if (prevBtn == null) btn.getPos().inBR(opad, opad);
                else  btn.getPos().leftOfMid(prevBtn.getPanel(), opad);
    
                prevBtn = btn;
            }
        }
    }

    public void createPanel() {}

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
        holo.foldOut(fader.getDurationIn() * 0.5f);
        holo.flickerNoise(0f, 1f);
    }

    @Override
    public void outsideClickAbsorbed(InputEventAPI event) {
        holo.flickerNoise(0f, 0.5f);
    }

    public void run(Button btn) {
        final Integer optionValue = optionsMap.get(btn);
        if (optionValue == null) dismiss(1);
        else dismiss(optionValue);
    }

    public Button getButton(int id) {
        for (Map.Entry<Button, Integer> entry : optionsMap.entrySet()) {
            if (entry.getValue() == id) return entry.getKey();
        }
        return null;
    }
}