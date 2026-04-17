package wfg.native_ui.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;

public class Globals {
    private Globals() {}

    public static final SettingsAPI settings = Global.getSettings();
}