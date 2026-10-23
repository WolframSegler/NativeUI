package wfg.native_ui.ui.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import wfg.native_ui.ui.MethodFields;

/**
 * Gets injected to vanilla UI hierarchies. Its presence indicates that the UI has yet to be refreshed.
 * Used to prevent constant UI-replacement.
 */
public class IdentityMarker extends BaseCustomUIPanelPlugin {
    private IdentityMarker() {};

    private static final UIComponentAPI mMarker = Global.getSettings().createCustom(0f, 0f, new IdentityMarker());

    /** attaches to the provided parent an {@link IdentityMarker}. */
    public static final void attach(UIPanelAPI parent) {
        throwIfNotVanilla(parent);
        parent.addComponent(mMarker);
    }

    /** @return if the provided object is an instace of {@link IdentityMarker}. */
    public static final boolean isMarker(Object obj) {
        return obj instanceof CustomPanelAPI custom && custom.getPlugin() instanceof IdentityMarker;
    }

    /** @return if the direct children of the {@code parent} contain an {@link IdentityMarker}. */
    public static final boolean isPresent(UIPanelAPI parent) {
        throwIfNotVanilla(parent);
        for (UIComponentAPI child : MethodFields.getChildrenNonCopy(parent)) {
            if (isMarker(child)) return true;
        }
        return false;
    }

    private static final void throwIfNotVanilla(UIPanelAPI panel) {
        if (!MethodFields.uiInternalCompClass.isInstance(panel)) {
            throw new IllegalArgumentException("Non vanilla parent detected. Use UIEventBus instead.");
        }
    }
}