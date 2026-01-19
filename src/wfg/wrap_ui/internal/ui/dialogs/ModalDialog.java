package wfg.wrap_ui.internal.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.codex2.CodexDialog;

import rolflectionlib.util.RolfLectionUtil;
import wfg.wrap_ui.internal.ui.plugins.ModalDialogPlugin;
import wfg.wrap_ui.internal.ui.plugins.ModalInterceptorPlugin;
import wfg.wrap_ui.ui.Attachments;
import wfg.wrap_ui.ui.panels.CustomPanel;
import wfg.wrap_ui.util.RunnableWithCode;

public class ModalDialog extends CustomPanel<ModalDialogPlugin, ModalDialog> {
    public RunnableWithCode delegate;
    public int optionOnKeyboardCancel = 1;
    public int optionOnKeyboardConfirm = 0;
    public int dismissOption = -1;
    public float backgroundDimAmount = 0.66f;
    public boolean RMBAnywhereCancels = false;
    public boolean LMBOutsideCancels = false;
    public boolean RMBOutsideCancels = true;
    public boolean interceptAllContainedEvents = true;
    public boolean fadeInAndOut = true;
    public boolean keyboardShortcutsToAcceptEnabled = true;

    protected boolean useCustomCenter = false;
    protected boolean suspendEventInterception = false;
    protected float centerX, centerY;

    protected UIPanelAPI inputInterceptor;
    protected Set<Integer> optionSet = new HashSet<>();
    protected FaderUtil fader = new FaderUtil(0f, 0.5f, 0.2f);

    public ModalDialog() {
        this(Attachments.getScreenPanel(), 500, 200, null);
    }

    public ModalDialog(int width, int height) {
        this(Attachments.getScreenPanel(), width, width, null);
    }

    public ModalDialog(UIPanelAPI parent, int width, int height, RunnableWithCode dialogDismissed) {
        super(parent, width, height, new ModalDialogPlugin());
        getPlugin().init(this);

        delegate = dialogDismissed;
        inputInterceptor = Global.getSettings().createCustom(
            Global.getSettings().getScreenWidth(), Global.getSettings().getScreenHeight(),
            new ModalInterceptorPlugin(this)
        );
    }

    public void createPanel() {}

    public UIPanelAPI getInterceptor() {
        return inputInterceptor;
    }

    public void outsideClickAbsorbed(InputEventAPI event) {}

    public void setCenter(float cx, float cy) {
        centerX = cx;
        centerY = cy;
        useCustomCenter = true;
    }

    public float getDialogCenterY() {
        return centerY;
    }

    public void show(float durIn, float durOut) {
        fader.setDuration(durIn, durOut);
        m_parent.removeComponent(inputInterceptor);
        m_parent.addComponent(inputInterceptor);
        m_parent.addComponent(m_panel);
        final PositionAPI pos = m_parent.getPosition();
        inputInterceptor.getPosition().setSize(pos.getWidth(), pos.getHeight()).inMid();
        if (useCustomCenter) {
            getPos().inBL(
                    centerX - pos.getX() - pos.getWidth() / 2f,
                    centerY - pos.getY() - pos.getHeight() / 2f);
        } else getPos().inMid();

        if (fadeInAndOut) fader.fadeIn();
        else fader.forceIn();

        dismissOption = -1;
    }

    public void resetOption() { dismissOption = -1;}

    public void dismiss(int option) {
        dismissOption = option;
        if (fadeInAndOut) {
            if (optionSet.contains(option)) fader.forceOut();
            else fader.fadeOut();
        }

        if (delegate != null) delegate.run(option);
    }

    public void makeOptionInstant(int option) {
        optionSet.add(option);
    }

    public final float getFaderBrightness() {
        return fader.getBrightness();
    }

    protected boolean isFullyShown() {
        return fader.getBrightness() == 1f;
    }

    protected boolean isBeingDismissed() {
        return dismissOption >= 0;
    }

    public void setSuspendEventInterception(boolean bool) {
        if (bool && !suspendEventInterception) {
            m_parent.removeComponent(inputInterceptor);
        } else if (!bool && suspendEventInterception) {
            m_parent.removeComponent(inputInterceptor);
            m_parent.addComponent(inputInterceptor);
            final PositionAPI pos = m_parent.getPosition();
            inputInterceptor.getPosition().setSize(pos.getWidth(), pos.getHeight()).inMid();
            RolfLectionUtil.invokeMethod(
                    "bringToTop", m_parent, this);
        }

        suspendEventInterception = bool;
    }

    public void processInputImpl(List<InputEventAPI> events) {
        if (suspendEventInterception || isBeingDismissed()) return;

        events.removeIf(InputEventAPI::isConsumed);

        final PositionAPI pos = getPos();

        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            final boolean inside = pos.containsEvent(e);

            if (e.isLMBDownEvent() && LMBOutsideCancels && !inside) {
                dismiss(optionOnKeyboardCancel);
                e.consume();
                return;
            }

            if (optionOnKeyboardCancel != -1 &&
                ((e.isRMBDownEvent() && RMBOutsideCancels && (!inside || RMBAnywhereCancels))
                    || (e.isKeyDownEvent() && e.getEventValue() == Keyboard.KEY_ESCAPE
                ))
            ) {
                dismiss(optionOnKeyboardCancel);
                e.consume();
                return;
            }

            if (e.isKeyDownEvent()
                && keyboardShortcutsToAcceptEnabled
                && optionOnKeyboardConfirm != -1
                && (e.getEventValue() == Keyboard.KEY_RETURN
                    || e.getEventValue() == Keyboard.KEY_SPACE
            )) {
                dismiss(optionOnKeyboardConfirm);
                e.consume();
                return;
            }

            if (e.isKeyboardEvent() && e.getEventValue() == Keyboard.KEY_F2) {
                CodexDialog.show();
                e.consume();
                continue;
            }

            if (e.isKeyboardEvent() || (inside && interceptAllContainedEvents)) {
                e.consume();
            }
        }
    }

    public void advanceImpl(float delta) {
        fader.advance(delta);

        if (suspendEventInterception) return;

        if (isBeingDismissed() && fader.getBrightness() == 0f && m_parent != null) {
            m_parent.removeComponent(inputInterceptor);
            m_parent.removeComponent(m_panel);
        }
    }
}