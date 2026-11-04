package wfg.native_ui.ui.system;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.component.AudioFeedbackComp;
import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.core.UIEntityAPI;

public final class AudioFeedbackSystem extends BaseSystem {
    private static final SoundPlayerAPI player = Global.getSoundPlayer();

    private static final AudioFeedbackSystem INSTANCE = new AudioFeedbackSystem();
    public static AudioFeedbackSystem get() { return INSTANCE; }
    private AudioFeedbackSystem() {}

    @Override
    public void init(UIEntityAPI element) {
        element.comp().setIfNotPresent(NativeComponents.AUDIO_FEEDBACK, new AudioFeedbackComp());
        element.comp().setIfNotPresent(NativeComponents.INPUT_SNAPSHOT, new InputSnapshotComp());
    }

    private static final int INIT_COMP_TICKS = 10;

    @Override
    public void processInput(final UIEntityAPI element, final List<InputEventAPI> events) {
        final AudioFeedbackComp audio = element.comp().get(NativeComponents.AUDIO_FEEDBACK);
        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);

        if (audio == null || !audio.enabled) return;
        audio.iAccumulatedGameTicks++;

        if (audio.iAccumulatedGameTicks < INIT_COMP_TICKS) return;

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