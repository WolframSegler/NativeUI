package wfg.native_ui.ui.widget;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.*;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TextFieldAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.ActionListenerDelegate;

import wfg.native_ui.ui.panel.CustomPanel;
import wfg.native_ui.util.NativeUiUtils;

public class TextField extends CustomPanel implements TextFieldAPI {
    private static final String TYPER_BUZZER_SOUND_ID = "ui_typer_buzz";
    private static final String TYPER_TYPE_SOUND_ID = "ui_typer_type";

    private final ActionListenerDelegate actionListener;

    private LabelAPI textLabel;
    private LabelAPI cursorLabel;
    private float leftPad = 2f;
    private Color bgColor = NativeUiUtils.setAlpha(btnBgColorDark, 150);
    private Color borderColor = bgColor;
    private TextFieldAPI nextTextField;
    private LabelAPI blankTextLabel;
    private LabelAPI descLabel;
    private o textListener = null;
    private int maxCharacters = -1;
    private String lastTextBeforeFocus = null;
    
    public boolean goToNextOnEnter = false;
    public boolean callTabPressedOnEnter = false;
    public boolean limitByStringWidth = true;
    public boolean undoOnEscape = true;
    public boolean handleCtrlV = true;
    public boolean minimalMode = false;

    public TextField(float w, float h, String text, String font, ActionListenerDelegate listener) {
        this(w, h, text, font, false, listener);
    }

    public TextField(float w, float h, String text, String font, boolean useSmallInsignia, ActionListenerDelegate listener) {
        super(null, (int) w, (int) h);

        actionListener = listener;
        textLabel = settings.createLabel(text, useSmallInsignia ? Fonts.INSIGNIA_LARGE : font);
        textLabel.setAlignment(Alignment.LMID);
        textLabel.autoSizeToWidth(w);
        add(textLabel).inLMid(leftPad * 2f);
        cursorLabel = settings.createLabel("_", font);
        cursorLabel.autoSizeToWidth(w);
        cursorLabel.setAlignment(Alignment.MID);
        // TODO
        // cursorLabel.getFader().setDuration(0.05f, 0.25f);
        // cursorLabel.getFader().forceOut();
        setColor(btnTxtColor);
        add(cursorLabel).rightOfBottom((UIComponentAPI) textLabel, 0f);
    }

    public void setHint(String hint) {
        blankTextLabel = settings.createLabel(hint, Fonts.DEFAULT_SMALL);
        blankTextLabel.setAlignment(Alignment.MID);
        blankTextLabel.setColor(gray);
        blankTextLabel.autoSizeToWidth(pos.getWidth());
        add(blankTextLabel).inMid();
    }

    public void setDesc(String desc) {
        descLabel = settings.createLabel(desc, Fonts.VICTOR_10);
        descLabel.setColor(btnTxtColor);
        descLabel.autoSizeToWidth(pos.getWidth());
        add(descLabel).inTR(6f, -6f);
        setPad(4f);
    }

    public boolean isMinimalMode() {
        return minimalMode;
    }

    public void setMinimalMode(boolean bl) {
        minimalMode = bl;
    }

    public void setPad(final float pad) {
        leftPad = pad;
        remove(textLabel);
        add(textLabel).inLMid(pad * 2f);
    }

    public LabelAPI getTextLabel() {
        return textLabel;
    }

    public LabelAPI getTextLabelAPI() {
        return textLabel;
    }

    public o getTextListener() {
        return textListener;
    }

    public void setTextListener(o o2) {
        textListener = o2;
    }

    public void setMidAlignment() {
        remove(textLabel);
        add(textLabel).inMid();
    }

    public void setColor(Color color) {
        textLabel.setColor(color);
        cursorLabel.setColor(color);
    }

    public void setBgColor(Color color) {
        bgColor = color;
    }

    public TextFieldAPI getNext() {
        return nextTextField;
    }

    public void setNext(TextFieldAPI b2) {
        nextTextField = b2;
    }

    public final void setSize(float w, float h) {
        pos.setSize(w, h);
    }

    public final String getText() {
        return textLabel.getText();
    }

    public void setText(String text) {
        final String prevText = getText();
        textLabel.setText(text);
        textLabel.autoSizeToWidth(pos.getWidth());
        posRecompute();

        final String newText = getText();
        if (textListener != null && !prevText.equals(newText)) {
            textListener.textChanged(this, prevText);
        }
    }

    public boolean isValidChar(char c2) {
        if (c2 == '\u0000') {
            return false;
        }
        if (c2 == '%') {
            return false;
        }
        if (c2 == '$') {
            return false;
        }
        // oOOO[] oOOOArray = textLabel.getRenderer().return().\u00d600000();
        // boolean bl = oOOOArray.length > c2 && oOOOArray[c2] != null;
        // return bl;
        return false; // TODO remove
    }

    public final boolean appendCharIfPossible(char c2) {
        return appendCharIfPossible(c2, true);
    }

    public final boolean appendCharIfPossible(char c2, boolean bl) {
        String string = String.valueOf(textLabel.getText()) + c2;
        boolean bl3 = isValidChar(c2);
        float f2 = textLabel.computeTextWidth(string);
        float f3 = textLabel.computeTextWidth("_");
        boolean bl2 = string.length() > maxCharacters && maxCharacters >= 0;
        if (bl3 && (f2 <= pos.getWidth() - leftPad * 2f - f3 - leftPad || !limitByStringWidth) && !bl2) {
            setText(string);
            if (bl) {
                Global.getSoundPlayer().playUISound(TYPER_TYPE_SOUND_ID, 1f, 1f);
            }
            if (textListener != null) {
                textListener.charTyped(this, c2);
            }
            return true;
        }
        if (bl) {
            Global.getSoundPlayer().playUISound(TYPER_BUZZER_SOUND_ID, 1f, 1f);
        }
        if (textListener != null) {
            textListener.charTypeFailed(this, c2);
        }
        return false;
    }

    public final int getMaxChars() {
        return maxCharacters;
    }

    public final void setMaxChars(int count) {
        maxCharacters = count;
    }

    private final void deleteLastChar() {
        final String string = textLabel.getText();
        if (string.length() <= 0) {
            Global.getSoundPlayer().playUISound(TYPER_BUZZER_SOUND_ID, 1f, 1f);
            return;
        }
        Global.getSoundPlayer().playUISound(TYPER_TYPE_SOUND_ID, 1f, 1f);
        setText(string.substring(0, string.length() - 1));
    }

    public final void deleteAll() {
        deleteAll(true);
    }

    public final void deleteAll(boolean playSound) {
        final boolean hasText = textLabel.getText().length() > 0;
        if (playSound) Global.getSoundPlayer().playUISound(hasText ? TYPER_TYPE_SOUND_ID : TYPER_BUZZER_SOUND_ID, 1f, 1f);
        if (hasText) setText("");
    }

    public void deleteLastWord() {
        int n2;
        String string = textLabel.getText();
        if (string.length() == 0) {
            Global.getSoundPlayer().playUISound(TYPER_BUZZER_SOUND_ID, 1f, 1f);
        }
        if ((n2 = string.lastIndexOf(" ")) == string.length() - 1 && n2 > 0) {
            n2 = string.substring(0, n2).lastIndexOf(" ");
        }
        if (n2 == -1) {
            deleteAll();
        } else {
            Global.getSoundPlayer().playUISound(TYPER_TYPE_SOUND_ID, 1f, 1f);
            setText(string.substring(0, n2 + 1));
        }
    }

    public void grabFocus() {
        grabFocus(true);
    }

    public void grabFocus(boolean bl) {
        if (bl) {
            Global.getSoundPlayer().playUISound(TYPER_BUZZER_SOUND_ID, 1f, 1f);
        }
        lastTextBeforeFocus = getText();
        // O0Oo.\u00d500000(this);
    }

    public void releaseFocus(Object object) {
        if (object != null && actionListener != null) {
            actionListener.actionPerformed(object, this);
        }
        // O0Oo.super(this);
    }

    @Override
    public void advance(float delta) {
        super.advance(delta);

        if (blankTextLabel != null) {
            blankTextLabel.setOpacity(getText().isEmpty() ? 1f : 0f);
        }
        // boolean bl = O0Oo.\u00d300000() == this;
        // if ((cursorLabel.isBlinking() || cursorLabel.getFader().getBrightness() > 0f && !cursorLabel.getFader().isFadingOut()) && !bl) {
        //     cursorLabel.stopBlinking();
        //     cursorLabel.getFader().fadeOut();
        // } else if ((!cursorLabel.isBlinking() || cursorLabel.getFader().getBrightness() < 1f && !cursorLabel.getFader().isFadingIn()) && bl) {
        //     cursorLabel.blink(2f, Float.MAX_VALUE);
        //     cursorLabel.getFlasher().forceIn();
        //     cursorLabel.getFader().fadeIn();
        // }
    }

    public boolean hasFocus() {
        // return O0Oo.\u00d300000() == this;
        return false; // TODO handle
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        super.processInput(events);

        // boolean bl = O0Oo.\u00d300000() == this;
        boolean bl = false; // TODO handle

        // if (!isEnabled()) {
        //     if (bl) {
        //         releaseFocus(null);
        //     }
        //     return;
        // }

        for (InputEventAPI event : events) {
            if (event.isConsumed() || event.isMouseMoveEvent()) continue;
            if (!bl && event.isLMBDownEvent() && pos.containsEvent(event)) {
                grabFocus();
                // events.\u00d200000();
                event.consume();
                continue;
            }
            if (bl && event.isMouseDownEvent()) {
                releaseFocus(event);
                break;
            }
            if (!bl || !event.isKeyboardEvent() || !event.isKeyDownEvent() && !event.isRepeat()) continue;
            if (event.getEventValue() == Keyboard.KEY_RETURN || event.getEventValue() == Keyboard.KEY_NUMPADENTER) {
                if (callTabPressedOnEnter && textListener != null) {
                    textListener.tabPressed(this, event);
                }
                if (goToNextOnEnter && nextTextField != null) {
                    releaseFocus(event);
                    nextTextField.grabFocus();
                    // events.\u00d200000();
                    event.consume();
                    break;
                }
                if (!callTabPressedOnEnter) {
                    Global.getSoundPlayer().playUISound(TYPER_BUZZER_SOUND_ID, 1f, 1f);
                }
                releaseFocus(event);
                event.consume();
                break;
            }
            if (event.getEventValue() == 15) {
                if (textListener != null) {
                    textListener.tabPressed(this, event);
                }
                if (nextTextField == null) continue;
                releaseFocus(event);
                nextTextField.grabFocus();
                // events.\u00d200000();
                event.consume();
                break;
            }
            if (event.getEventValue() == 1) {
                Global.getSoundPlayer().playUISound(TYPER_TYPE_SOUND_ID, 1f, 1f);
                if (undoOnEscape) {
                    setText(lastTextBeforeFocus);
                }
                releaseFocus(null);
                if (textListener != null) {
                    textListener.escapePressed(this, event);
                }
                event.consume();
                break;
            }
            if (event.getEventValue() == Keyboard.KEY_V && event.isCtrlDown() && handleCtrlV) {
                final String string = getClipboardText();
                int n2 = 0;
                while (n2 < string.length()) {
                    appendCharIfPossible(string.charAt(n2));
                    ++n2;
                }
                event.consume();
                continue;
            }
            if (event.getEventValue() == 14) {
                boolean bl3 = getText().isEmpty();
                if (event.isShiftDown()) {
                    deleteAll();
                } else if (event.isCtrlDown()) {
                    deleteLastWord();
                } else {
                    deleteLastChar();
                }
                if (textListener != null) {
                    textListener.backspacePressed(this, !bl3);
                }
                event.consume();
                continue;
            }
            char c2 = event.getEventChar();
            if (c2 == '\u0000') continue;
            appendCharIfPossible(c2);
            event.consume();
        }
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color color) {
        borderColor = color;
    }

    public void hideCursor() {
        cursorLabel.setOpacity(0f);
    }

    public void showCursor() {
        cursorLabel.setOpacity(1f);
    }

    @Override
    public void render(float f2) {
        // float f3 = pos.getX();
        // float f4 = pos.getY();
        // float f5 = pos.getWidth();
        // float f6 = pos.getHeight();
        // if (minimalMode) {
        //     public.Object(f3, f4, f5, 2f, com.fs.graphics.util.B.new((Color)borderColor, (int)((int)(127f + 128f * cursorLabel.getFader().getBrightness()))), f2);
        //     Color color = com.fs.graphics.util.B.new((Color)com.fs.graphics.util.B.class((Color)bgColor, (float)0.4f), (int)200);
        //     public.o00000(f3 + 1f, f4 + 1f, f5 - 2f, f6 - 2f, color, Misc.zeroColor, Misc.zeroColor, color, f2);
        // } else {
        //     if (descLabel != null) {
        //         Color color = com.fs.graphics.util.B.new((Color)borderColor, (int)((int)(127f + 128f * cursorLabel.getFader().getBrightness())));
        //         public.o00000(f3, f4, f5, f6, 1f, false, true, true, true, color, f2);
        //         float f7 = descLabel.pos.getX();
        //         float f8 = descLabel.pos.getX() + descLabel.pos.getWidth();
        //         public.Object(f3 + 1f, f4 + f6 - 1f, f7 - f3 - 1f, 1f, color, f2);
        //         public.Object(f8 + 1f, f4 + f6 - 1f, f3 + f5 - f8 - 2f, 1f, color, f2);
        //     } else {
        //         public.o00000(pos, com.fs.graphics.util.B.new((Color)borderColor, (int)((int)(127f + 128f * cursorLabel.getFader().getBrightness()))), f2);
        //     }
        //     public.Object(f3 + 1f, f4 + 1f, f5 - 2f, f6 - 2f, com.fs.graphics.util.B.new((Color)com.fs.graphics.util.B.class((Color)bgColor, (float)0.4f), (int)200), f2);
        // }
        // boolean bl = O0Oo.\u00d300000() == this;
        // super.render(f2);
        // if (!isEnabled()) {
        //    public.Object(f3 + 1f, f4 + 1f, f5 - 2f, f6 - 2f, new Color(100, 100, 100, 255), f2 * 0.4f);
        // }
    }

    /** Copied from StarfarerLauncherUI */
    public static String getClipboardText() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable transferable = clipboard.getContents(null);
        final boolean validClipboard = transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor);

        String string = "";
        if (validClipboard) {
            try {
                string = (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
        return string;
    }

    public final void setLimitByStringWidth(boolean bool) { limitByStringWidth = bool; }
    public final void setUndoOnEscape(boolean bool) { undoOnEscape = bool; }
    public final void setHandleCtrlV(boolean bool) { handleCtrlV = bool; }
    public final void setOpacity(float alpha) { m_panel.setOpacity(alpha); }
    public final void setVerticalCursor(boolean bool) {}
    public final boolean isLimitByStringWidth() { return limitByStringWidth; }
    public final boolean isHandleCtrlV() { return handleCtrlV; }
    public final boolean isUndoOnEscape() { return undoOnEscape; }
    public final boolean isVerticalCursor() { return false; }
    public final float getOpacity() { return m_panel.getOpacity(); }
    public final PositionAPI getPosition() { return pos; }

    public static interface o {
        public void backspacePressed(TextFieldAPI var1, boolean var2);

        public void charTyped(TextFieldAPI var1, char var2);

        public void charTypeFailed(TextFieldAPI var1, char var2);

        public void tabPressed(TextFieldAPI var1, Object var2);

        default public void escapePressed(TextFieldAPI b2, Object object) {
        }

        default public void textChanged(TextFieldAPI b2, String string) {
        }
    }
}