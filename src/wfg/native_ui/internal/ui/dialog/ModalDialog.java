package wfg.native_ui.internal.ui.dialog;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.codex2.CodexDialog;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.ui.Attachments;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.functional.RunnableWithCode;
import wfg.native_ui.util.RenderUtils;

public class ModalDialog extends UIContainer {
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

    protected final UIElementAPI mInputInterceptor;
    protected final Set<Integer> mOptionSet = new HashSet<>();
    protected final FaderUtil mFader = new FaderUtil(0f, 0.5f, 0.2f);
    protected final UIPanelAPI mInterceptorParent;

    public ModalDialog() {
        this(Attachments.getScreenPanel(), 500, 200, null);
    }

    public ModalDialog(float width, float height) {
        this(Attachments.getScreenPanel(), width, width, null);
    }

    public ModalDialog(float width, float height, RunnableWithCode dialogDismissed) {
        this(Attachments.getScreenPanel(), width, width, dialogDismissed);
    }

    public ModalDialog(UIPanelAPI interceptorParent, float width, float height, RunnableWithCode dialogDismissed) {
        super(width, height);
        mInterceptorParent = interceptorParent;

        delegate = dialogDismissed;
        mInputInterceptor = new ModalInterceptor(this);
    }

    public UIElementAPI getInterceptor() { return mInputInterceptor; }

    public void setCenter(float cx, float cy) {
        centerX = cx;
        centerY = cy;
        useCustomCenter = true;
    }

    public void show(float durIn, float durOut) {
        mFader.setDuration(durIn, durOut);
        reAttachInputInterceptor();

        final PositionAPI pos = mInterceptorParent.getPosition();
        if (useCustomCenter) {
            mPos.inBL(
                centerX - pos.getX() - pos.getWidth() / 2f,
                centerY - pos.getY() - pos.getHeight() / 2f);
        } else mPos.inMid();

        if (fadeInAndOut) mFader.fadeIn();
        else mFader.forceIn();

        dismissOption = -1;
    }

    public void resetOption() { dismissOption = -1;}

    public void dismiss(int option) {
        dismissOption = option;
        if (fadeInAndOut) {
            if (mOptionSet.contains(option)) mFader.forceOut();
            else mFader.fadeOut();
        }

        if (delegate != null) delegate.run(option);
    }

    public void makeOptionInstant(int option) {
        mOptionSet.add(option);
    }

    public final float getFaderBrightness() {
        return mFader.getBrightness();
    }

    protected boolean isFullyShown() {
        return mFader.getBrightness() == 1f;
    }

    protected boolean isBeingDismissed() {
        return dismissOption >= 0;
    }

    public void setSuspendEventInterception(boolean bool) {
        if (bool && !suspendEventInterception) {
            mInputInterceptor.detach();
        } else if (!bool && suspendEventInterception) {
            reAttachInputInterceptor();
        }

        suspendEventInterception = bool;
    }

    private final void reAttachInputInterceptor() {
        mInputInterceptor.detach();
        mInputInterceptor.attachTo(mInterceptorParent);

        final PositionAPI pos = mInterceptorParent.getPosition();
        mInputInterceptor.getPosition().setSize(pos.getWidth(), pos.getHeight()).inBL(0f, 0f);
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        if (suspendEventInterception || isBeingDismissed()) return;

        for (InputEventAPI e : events) {
            if (e.isConsumed()) continue;

            final boolean inside = mPos.containsEvent(e);

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

    @Override
    public void advanceImpl(float delta) {
        mFader.advance(delta);
    }

    @Override
    public void renderImpl(float alpha) {
        RenderUtils.drawQuad(
            0f, 0f, screenW, screenH, Color.BLACK,
            alpha * backgroundDimAmount * getFaderBrightness(),
            false
        );
    }

    public void outsideClickAbsorbed(InputEventAPI event) {};
}