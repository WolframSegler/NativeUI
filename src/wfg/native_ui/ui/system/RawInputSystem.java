package wfg.native_ui.ui.system;

import java.util.List;

import org.lwjgl.input.Mouse;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import wfg.native_ui.ui.component.InputSnapshotComp;
import wfg.native_ui.ui.component.NativeComponents;
import wfg.native_ui.ui.panel.CustomPanel;

public class RawInputSystem extends BaseSystem {

    private static final RawInputSystem INSTANCE = new RawInputSystem();
    public static RawInputSystem get() { return INSTANCE;}
    private RawInputSystem() {}

    @Override
    public void init(CustomPanel<?> element) {
        element.comp().setIfNotPresent(NativeComponents.INPUT_SNAPSHOT, new InputSnapshotComp());
    }

    @Override
    public void processInput(final CustomPanel<?> element, final List<InputEventAPI> events) {
        final InputSnapshotComp input = element.comp().get(NativeComponents.INPUT_SNAPSHOT);

        input.resetFrameFlags();

        boolean mouseMovePresent = false;

        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            
            if (event.isMouseMoveEvent()) {
                final PositionAPI pos = element.getPos();
                final float x = pos.getX();
                final float y = pos.getY();
                final float w = pos.getWidth();
                final float h = pos.getHeight();

                mouseMovePresent = true;
                input.mouseMoveEvent = event;

                final float mouseX = event.getX();
                final float mouseY = event.getY();

                // Check for mouse over panel
                final boolean hoveredBefore = input.hoveredLastFrame;
                input.hoveredLastFrame = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

                input.hoverStarted = input.hoveredLastFrame && !hoveredBefore;
                input.hoverEnded = !input.hoveredLastFrame && hoveredBefore;
            }

            if (event.isLMBDownEvent() && input.hoveredLastFrame) {
                input.LMBDownLastFrame = true;
                input.hasLMBClickedBefore = true;
                input.isActive = true;
            }

            if (event.isLMBUpEvent() || !Mouse.isButtonDown(0)) {
                if (input.hasLMBClickedBefore) input.LMBUpLastFrame = true;
                input.isActive = false;
                input.hasLMBClickedBefore = false;
            }

            if (event.isRMBDownEvent() && input.hoveredLastFrame) {
                input.RMBDownLastFrame = true;
                input.hasRMBClickedBefore = true;
            }

            if (event.isRMBUpEvent() || !Mouse.isButtonDown(1)) {
                if (input.hasRMBClickedBefore) input.RMBUpLastFrame = true;
                input.hasRMBClickedBefore = false;
            }
        }

        if (!mouseMovePresent) {
            input.hoveredLastFrame = false;
            input.mouseMoveEvent = null;
        }
    }
}