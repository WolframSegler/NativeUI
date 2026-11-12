package wfg.native_ui.example.widget;

import static wfg.native_ui.util.Globals.settings;

import java.awt.Color;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;

import wfg.native_ui.internal.ui.Side;
import wfg.native_ui.ui.widget.Toast;

/**
 * In this example, the {@link Toast} slides in from the top-left to warn the player of their fuel level.
 */
public final class ToastExample {
    private ToastExample() {};

    public static final void notifyIfFuelBelow(float fraction) {
        if (settings.getCurrentState() != GameState.CAMPAIGN) return;
        final CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        final float maxFuel = cargo.getMaxFuel();
        final float fuelRatio = maxFuel > 0f ? cargo.getFuel() / maxFuel : 0f;
        
        if (fuelRatio < fraction) notifyFuelLow();
    }
    
    public static final void notifyFuelLow() {
        if (settings.getCurrentState() != GameState.CAMPAIGN) return;

        final CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        final float maxFuel = cargo.getMaxFuel();
        final float fuelRatio = maxFuel > 0f ? cargo.getFuel() / maxFuel : 0f;
        final String text = "Fuel left: " + Integer.toString((int) cargo.getFuel()) + " (" + Math.round(fuelRatio * 100f) + "%)"; 

        final float w = 200;
        final float h = 50;

        final Toast fuelToast = new Toast(w, h, Side.LEFT);
        fuelToast.mHoloColor = new Color(255, 159, 0);
        final LabelAPI textLbl = settings.createLabel(text, Fonts.DEFAULT_SMALL);
        /** The overridden add method adds the label to the content container. */
        fuelToast.add(textLbl).setSize(w, h);

        fuelToast.open();

        /** Optional audio to notify the player. */
        Global.getSoundPlayer().playUISound("ui_intel_log_update", 1f, 1f);
    }
}