package wfg.native_ui.internal.ui.core;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.core.UIElementFlags;
import wfg.native_ui.ui.core.UIEntityAPI;
import wfg.native_ui.ui.system.AudioFeedbackSystem;
import wfg.native_ui.ui.system.BackgroundSystem;
import wfg.native_ui.ui.system.BaseSystem;
import wfg.native_ui.ui.system.DebugBgSystem;
import wfg.native_ui.ui.system.HoverGlowSystem;
import wfg.native_ui.ui.system.InteractionSystem;
import wfg.native_ui.ui.system.NativeSystems;
import wfg.native_ui.ui.system.OutlineSystem;
import wfg.native_ui.ui.system.RawInputSystem;
import wfg.native_ui.ui.system.TooltipSystem;
import wfg.native_ui.ui.system.UISystemContainer;

public class UIEntity extends UIElement implements UIEntityAPI {

    private UIComponentContainer mCompContainer = null;
    private UISystemContainer mSystemContainer = null;

    public UIEntity(float width, float height) {
        super(width, height);
        initSystems();
    }

    public UIEntity() {
        initSystems();
    }

    public final UIComponentContainer getUIComponentContainer() { return comp(); }
    public final UIComponentContainer comp() {
        if (mCompContainer == null) mCompContainer = new UIComponentContainer();
        return mCompContainer;
    }

    public final UISystemContainer getUISystemContainer() { return system(); }
    public final UISystemContainer system() {
        if (mSystemContainer == null) mSystemContainer = new UISystemContainer();
        return mSystemContainer;
    }

    public void initSystems() {
        if (this instanceof UIElementFlags.HasInputSnapshot) {
            system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasBackground) {
            system().setIfNotPresent(NativeSystems.BACKGROUND, BackgroundSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasDebugBg) {
            system().setIfNotPresent(NativeSystems.DEBUG_BG, DebugBgSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasOutline) {
            system().setIfNotPresent(NativeSystems.OUTLINE, OutlineSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasHoverGlow) {
            system().setIfNotPresent(NativeSystems.HOVER_GLOW, HoverGlowSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasTooltip) {
            system().setIfNotPresent(NativeSystems.TOOLTIP, TooltipSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasAudioFeedback) {
            system().setIfNotPresent(NativeSystems.AUDIO_FEEDBACK, AudioFeedbackSystem.get(), this);
        }

        if (this instanceof UIElementFlags.HasInteraction) {
            system().setIfNotPresent(NativeSystems.INTERACTION, InteractionSystem.get(), this);
        }
    }

    @Override
    public void renderImpl(float alpha) {
        for (BaseSystem system : system().getAll()) {
            system.renderBelow(this, alpha);
        }
        for (BaseSystem system : system().getAll()) {
            system.renderAbove(this, alpha);
        }
    }

    @Override
    public void processInputImpl(List<InputEventAPI> events) {
        for (BaseSystem system : system().getAll()) {
            system.processInput(this, events);
        }
    }

    @Override
    public void advanceImpl(float delta) {
        for (BaseSystem system : system().getAll()) {
            system.advance(this, delta);
        }
    }

    @Override
    public void notifyRemoved() {
        for (BaseSystem system : system().getAll()) {
            system.onRemove(this);
        }    
    }
}