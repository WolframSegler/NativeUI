package wfg.native_ui.ui.components;

import com.fs.starfarer.api.input.InputEventAPI;

public class InputSnapshotComp extends BaseComponent {
    public boolean LMBDownLastFrame = false;
    public boolean LMBUpLastFrame = false;
    public boolean hasLMBClickedBefore = false;

    public boolean RMBDownLastFrame = false;
    public boolean RMBUpLastFrame = false;
    public boolean hasRMBClickedBefore = false;

    public boolean hoveredLastFrame = false;
    public boolean hoverStarted = false;
    public boolean hoverEnded = false;
    public boolean isActive = false;

    public InputEventAPI mouseMoveEvent = null;

    public void resetFrameFlags() {
        LMBDownLastFrame = false;
        LMBUpLastFrame = false;

        RMBDownLastFrame = false;
        RMBUpLastFrame = false;
    }
}