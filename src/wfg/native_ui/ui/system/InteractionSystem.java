package wfg.native_ui.ui.system;

import java.util.List;

import com.fs.starfarer.api.input.InputEventAPI;

import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.InteractionComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.component.UIComponentContainer;
import wfg.native_ui.ui.component.UIContextComp;
import wfg.native_ui.ui.panel.CustomPanel;

public final class InteractionSystem extends BaseSystem {

    private static final InteractionSystem INSTANCE = new InteractionSystem();
    public static InteractionSystem get() { return INSTANCE;}
    private InteractionSystem() {}

    @Override
    public void init(CustomPanel element) {
        final UIComponentContainer comp = element.comp();
        comp.setIfNotPresent(NativeComponents.UI_CONTEXT, new UIContextComp());
        comp.setIfNotPresent(NativeComponents.INTERACTION, new InteractionComp<>());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void processInput(final CustomPanel element, List<InputEventAPI> events) {
        final var comp = element.comp();
        final UIContextComp context = comp.get(NativeComponents.UI_CONTEXT);
        final InteractionComp listen = comp.get(NativeComponents.INTERACTION);
        final InputSnapshotComp input = comp.get(NativeComponents.INPUT_SNAPSHOT);

        if (!listen.enabled) return;

        // Click handling
        if (input.hoveredLastFrame && input.LMBUpLastFrame && listen.onClicked != null) {
            listen.onClicked.handle(element, true);
        }
        if (input.hoveredLastFrame && input.RMBUpLastFrame && listen.onClicked != null) {
            listen.onClicked.handle(element, false);
        }

        // Shortcut handling
        if (listen.shortcut > 0 && context.isValid() && listen.onShortcutPressed != null) {
            for (InputEventAPI event : events) {
                if (!event.isConsumed() && event.isKeyDownEvent() &&
                    event.getEventValue() == listen.shortcut
                ) {
                    listen.onShortcutPressed.run(element);
                    event.consume();
                    break;
                }
            }
        }

        if (input.hoverStarted && listen.onHoverStarted != null) listen.onHoverStarted.run(element);
        if (input.hoverEnded && listen.onHoverEnded != null) listen.onHoverEnded.run(element);
        if (input.hoveredLastFrame && listen.onHover != null) listen.onHover.run(element);
    }
}