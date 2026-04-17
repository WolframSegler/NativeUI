package wfg.native_ui.ui.system;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.component.UIContextComp;
import wfg.native_ui.ui.panel.CustomPanel;

public final class AudioFeedbackSystem extends BaseSystem {
    private static final SoundPlayerAPI player = Global.getSoundPlayer();

    private static final AudioFeedbackSystem INSTANCE = new AudioFeedbackSystem();
    public static AudioFeedbackSystem get() { return INSTANCE;}
    private AudioFeedbackSystem() {}

    @Override
    public void init(CustomPanel element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.AUDIO_FEEDBACK, new AudioFeedbackComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
    }

    private static final int initCompTicks = 10;

    @Override
    public void processInput(final CustomPanel element, final List<InputEventAPI> events) {
        final AudioFeedbackComp audio = element.comp().get(NativeComponents.AUDIO_FEEDBACK);
        final UIContextComp context = element.comp().get(NativeComponents.UI_CONTEXT);
        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);

        if (audio == null || !audio.enabled || !context.isValid()) return;
        audio.accumulatedGameTicks++;

        if (audio.accumulatedGameTicks < initCompTicks) return;

        if (input.hoverStarted) {
            player.playUISound(audio.mouseOverSound, 1f, 1f);
        }

        if (input.LMBUpLastFrame && !audio.hoverOnly) {
            player.playUISound(audio.useDisabledSound ?
                audio.buttonPressedDisabledSound : audio.buttonPressedSound, 1f, 1f
            );
        }
    }
}