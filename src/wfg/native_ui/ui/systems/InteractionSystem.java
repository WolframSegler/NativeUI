package wfg.native_ui.ui.systems;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.components.InputSnapshot;
import wfg.native_ui.ui.components.InteractionComp;
import wfg.native_ui.ui.components.NativeComponents;
import wfg.native_ui.ui.components.UIContextComp;
import wfg.native_ui.ui.panels.CustomPanel;

public final class InteractionSystem<
    PanelType extends CustomPanel<PanelType>
> extends BaseSystem<PanelType>{

    @SuppressWarnings("rawtypes")
    private final InteractionComp listen;
    private final UIContextComp context;

    public InteractionSystem(PanelType panel) {
        super(panel);

        final var comp = panel.comp();
        comp.setIfNotPresent(NativeComponents.INTERACTION, new InteractionComp<>());
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());

        listen = comp.get(NativeComponents.INTERACTION);
        context = comp.get(NativeComponents.UI_CONTEXT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processInput(List<InputEventAPI> events, InputSnapshot input) {
        if (!listen.enabled) return;

        // Click handling
        if (input.hoveredLastFrame && input.LMBUpLastFrame && listen.onClicked != null) {
            listen.onClicked.handle(panel, true);
        }
        if (input.hoveredLastFrame && input.RMBUpLastFrame && listen.onClicked != null) {
            listen.onClicked.handle(panel, false);
        }

        // Shortcut handling
        if (listen.shortcut > 0 && context.isValid() && listen.onShortcutPressed != null) {
            for (InputEventAPI event : events) {
                if (!event.isConsumed() && event.isKeyDownEvent() &&
                    event.getEventValue() == listen.shortcut
                ) {
                    listen.onShortcutPressed.run(panel);
                }
            }
        }

        if (input.hoverStarted && listen.onHoverStarted != null) listen.onHoverStarted.run(panel);
        if (input.hoveredLastFrame && listen.onHover != null) listen.onHover.run(panel);
        if (input.hoverEnded && listen.onHoverEnded != null) listen.onHoverEnded.run(panel);
    }
}