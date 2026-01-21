package wfg.wrap_ui.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.CampaignState;
import com.fs.starfarer.combat.CombatState;
import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

import rolflectionlib.util.RolfLectionUtil;

/**
 * Utility class providing access points to UI elements across different game contexts:
 * campaign, interaction, and combat. Allows retrieving core panels, tab panels,
 * dialogs, and overlays depending on the current mode. Is not null-safe.
 */
public class Attachments {

    private static final Object getWarRoomPanelMethod = RolfLectionUtil.getMethod(
        "getWarroom",CombatState.class
    );
    private static final Object getTitleScreenPanelMethod = RolfLectionUtil.getMethod(
        "getScreenPanel", TitleScreenState.class
    );
    private static final Object getTutorialOverlayMethod = RolfLectionUtil.getMethod(
        "getTutorialOverlay", CombatState.class
    );
    private static final Object getCombatScreenPanelMethod = RolfLectionUtil.getMethod(
        "getWidgetPanel", CombatState.class
    );
    private static final Object getCampaignScreenPanelMethod = RolfLectionUtil.getMethod(
        "getScreenPanel", CampaignState.class
    );
    private static final Object getCampaignCoreMethod = RolfLectionUtil.getMethod(
        "getCore", CampaignState.class
    );
    private static final Object getEncounterDialogMethod = RolfLectionUtil.getMethod(
        "getEncounterDialog", CampaignState.class
    );
    private static final Class<?> coreUIclass = RolfLectionUtil.getReturnType(getCampaignCoreMethod);
    private static final Class<?> encounterDialogClass = RolfLectionUtil.getReturnType(getEncounterDialogMethod);
    private static final Object getCoreUIcurrentTabMethod = RolfLectionUtil.getMethod(
        "getCurrentTab", coreUIclass
    );
    private static final Object getEncounterDialogCoreUIMethod = RolfLectionUtil.getMethod(
        "getCoreUI", encounterDialogClass
    );

    /**
     * Must be interacting with an entity.
     */
    public static final InteractionDialogAPI getInteractionDialog() {
        return Global.getSector().getCampaignUI().getCurrentInteractionDialog();
    }

    /**
     * Returns the core UI panel of the current interaction.
     * Must be interacting with an entity.
     */
    public static final UIPanelAPI getInteractionCoreUI() {
        final UIPanelAPI encounter = (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getEncounterDialogMethod, getCampaignState());
        if (encounter == null) return null;

        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getEncounterDialogCoreUIMethod, encounter);
    }

    /**
     * Must be in campaign mode. Retrieves campaign core UI
     */
    public static final UIPanelAPI getCampaignCoreUI() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCampaignCoreMethod, getCampaignState());
    }

    /**
     * Works in-general in campaign mode.
     */
    public static final UIPanelAPI getCoreUI() {
        return getInteractionCoreUI() == null ? getCampaignCoreUI() : getInteractionCoreUI();
    }

    /**
     * Works in-general.
     */
    public static final UIPanelAPI getScreenPanel() {
        switch (Global.getSettings().getCurrentState()) {
            case CAMPAIGN: return getCoreUI();
            case COMBAT: getCombatScreenPanel();
            case TITLE: default: return getTitleScreenPanel();
        }
    }

    /**
     * Returns the panel for the current tab (FLEET, CHARACTER, COMMAND etc.) within an interaction.
     * Must be interacting with an entity.
     */
    public static final UIPanelAPI getInteractionCurrentTab() {
        final UIPanelAPI encounter = (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getEncounterDialogMethod, getCampaignState());
        if (encounter == null) return null;

        final UIPanelAPI coreUI = (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getEncounterDialogCoreUIMethod, encounter);
        if (coreUI == null) return null;

        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCoreUIcurrentTabMethod, coreUI);
    }

    /**
     * Returns the panel for the current tab (FLEET, CHARACTER, COMMAND etc.) in the campaign screen.
     * Must be in campaign mode.
     */
    public static final UIPanelAPI getCampaignCurrentTab() {
        final UIPanelAPI core = (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCampaignCoreMethod, getCampaignState());

        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCoreUIcurrentTabMethod, core);
    }

    /**
     * Works in-general
     */
    public static final UIPanelAPI getCurrentTab() {
        return getInteractionCurrentTab() == null ? getCampaignCurrentTab() : getInteractionCurrentTab();
    }

    /**
     * Also known as {@code getDialogParent()}.
     * Must be in campaign mode.
     */
    public static final UIPanelAPI getCampaignScreenPanel() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCampaignScreenPanelMethod, getCampaignState());
    }

    /**
     * Must be in combat mode.
     */
    public static final UIPanelAPI getCombatScreenPanel() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getCombatScreenPanelMethod, getCombatState());
    }

    /**
     * Must be in combat mode.
     */
    public static final UIPanelAPI getTutorialOverlay() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getTutorialOverlayMethod, getCombatState());
    }

    /**
     * Must be in combat mode.
     */
    public static final UIPanelAPI getWarroomPanel() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getWarRoomPanelMethod, getCombatState());
    }

    /**
     * Must be in Title mode.
     */
    public static final UIPanelAPI getTitleScreenPanel() {
        return (UIPanelAPI) RolfLectionUtil.invokeMethodDirectly(
            getTitleScreenPanelMethod, getTitleState());
    }


    /**
     * @return The instance of CampaignState if the currentState is Campaign state. Fails otherwise.
     */
    public static final CampaignState getCampaignState() {
        return ((CampaignState)AppDriver.getInstance().getCurrentState());
    }

    /**
     * @return The instance of CombatState if the current state is CombatState. Fails otherwise.
     */
    public static final CombatState getCombatState() {
        return ((CombatState)AppDriver.getInstance().getCurrentState());
    }

    /**
     * @return The instance of TitleScreenState if the current state is TitleState. Fails otherwise.
     */
    public static final TitleScreenState getTitleState() {
        return ((TitleScreenState)AppDriver.getInstance().getCurrentState());
    }
}