package wfg.wrap_ui.ui.systems;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.input.InputEventAPI;

import wfg.wrap_ui.ui.components.AudioFeedbackComp;
import wfg.wrap_ui.ui.components.ComponentContainer;
import wfg.wrap_ui.ui.components.InputSnapshot;
import wfg.wrap_ui.ui.components.NativeComponents;
import wfg.wrap_ui.ui.components.UIContextComp;
import wfg.wrap_ui.ui.panels.CustomPanel;

public final class AudioFeedbackSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType> {

    private static final int initCompTicks = 10;

    private final AudioFeedbackComp audio;
    private final UIContextComp context;

    public AudioFeedbackSystem(PanelType panel) {
        super(panel);
        final ComponentContainer comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.AUDIO_FEEDBACK, new AudioFeedbackComp());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        
        audio = panel.comp().get(NativeComponents.AUDIO_FEEDBACK);
        context = panel.comp().get(NativeComponents.UI_CONTEXT);
    }

    @Override
    public void processInput(List<InputEventAPI> events, InputSnapshot input) {
        if (audio == null || !audio.enabled) return;

        if (audio.accumulatedGameTicks > initCompTicks && context.isValid()) {
            SoundPlayerAPI player = Global.getSoundPlayer();

            if (input.hoverStarted && audio.enabled) {
                player.playUISound(audio.mouseOverSound, 1f, 1f);
            }

            if (input.LMBUpLastFrame && audio.enabled) {
                if (audio.useDisabledSound) {
                    player.playUISound(audio.buttonPressedDisabledSound, 1f, 1f);
                } else {
                    player.playUISound(audio.buttonPressedSound, 1f, 1f);
                }
            }
        }

        audio.accumulatedGameTicks++;
    }
}