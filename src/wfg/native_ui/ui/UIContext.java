package wfg.native_ui.ui;

/**
 * Tracks the current high-level UI context the player is in.
 * Used to enable or disable panel functionality depending on where it is shown.
 */
public class UIContext {
    public enum Context {
        NONE,
        DIALOG,
        MARKET_DETAIL_SCREEN,
        CAMPAIGN
    }

    private static Context currentContext = Context.NONE;

    public static Context getContext() {
        return currentContext;
    }

    public static void setContext(Context newState) {
        currentContext = newState;
    }

    public static boolean is(Context state) {
        return currentContext == state;
    }

    public static void reset() {
        currentContext = Context.NONE;
    }
}