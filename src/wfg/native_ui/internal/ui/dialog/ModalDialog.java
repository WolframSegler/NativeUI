package wfg.native_ui.internal.ui.dialog;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static wfg.native_ui.util.Globals.settings;
import static wfg.native_ui.util.UIConstants.screenH;
import static wfg.native_ui.util.UIConstants.screenW;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.codex2.CodexDialog;

import wfg.native_ui.internal.ui.core.UIContainer;
import wfg.native_ui.internal.ui.interaction.EventInterceptor;
import wfg.native_ui.internal.ui.interaction.EventInterceptor.EventInterceptorListener;
import wfg.native_ui.ui.core.UIElementAPI;
import wfg.native_ui.ui.functional.RunnableWithCode;
import wfg.native_ui.util.RenderUtils;

public class ModalDialog extends UIContainer implements EventInterceptorListener {
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

    protected boolean mUseCustomCenter = false;
    protected boolean mSuspendEventInterception = false;
    protected float mCenterX, mCenterY;

    protected final EventInterceptor mInputInterceptor;
    protected final Set<Integer> mOptionSet = new HashSet<>();
    protected final UIPanelAPI mInterceptorParent;

    public ModalDialog() {
        this(settings.getScreenPanel(), 500, 200, null);
    }

    public ModalDialog(float width, float height) {
        this(settings.getScreenPanel(), width, width, null);
    }

    public ModalDialog(float width, float height, RunnableWithCode dialogDismissed) {
        this(settings.getScreenPanel(), width, width, dialogDismissed);
    }

    public ModalDialog(UIPanelAPI interceptorParent, float width, float height, RunnableWithCode dialogDismissed) {
        super(width, height);
        mFader.setBrightness(0f);
        mFader.setDuration(0.5f, 0.2f);

        mInterceptorParent = interceptorParent;

        delegate = dialogDismissed;
        mInputInterceptor = new EventInterceptor(this);
    }

    public UIElementAPI getInterceptor() { return mInputInterceptor; }

    public void setCenter(float cx, float cy) {
        mCenterX = cx;
        mCenterY = cy;
        mUseCustomCenter = true;
    }

    public void show(float durIn, float durOut) {
        mFader.setDuration(durIn, durOut);
        reAttachInputInterceptor();

        final PositionAPI pos = mInterceptorParent.getPosition();
        if (mUseCustomCenter) {
            mPos.inBL(
                mCenterX - pos.getX() - pos.getWidth() / 2f,
                mCenterY - pos.getY() - pos.getHeight() / 2f);
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

    protected boolean isFullyShown() {
        return mFader.getBrightness() == 1f;
    }

    public boolean dismissInterceptor() {
        return dismissOption >= 0 && mFader.getBrightness() < 0.5f;
    }

    public boolean suspendInterceptor() {
        return mSuspendEventInterception;
    }

    public void setSuspendEventInterception(boolean bool) {
        if (bool && !mSuspendEventInterception) {
            mInputInterceptor.detach();
        } else if (!bool && mSuspendEventInterception) {
            reAttachInputInterceptor();
        }

        mSuspendEventInterception = bool;
    }

    private final void reAttachInputInterceptor() {
        mInputInterceptor.detach();
        mInputInterceptor.attachTo(mInterceptorParent);

        final PositionAPI pos = mInterceptorParent.getPosition();
        mInputInterceptor.setSize(pos.getWidth(), pos.getHeight()).inBL(0f, 0f);
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        if (mSuspendEventInterception || dismissInterceptor()) return;

        super.processInputImpl(events);

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
    public void renderImpl(float alpha) {
        RenderUtils.drawQuad(
            0f, 0f, screenW, screenH, Color.BLACK,
            alpha * backgroundDimAmount,
            false
        );

        super.renderImpl(alpha);
    }
}