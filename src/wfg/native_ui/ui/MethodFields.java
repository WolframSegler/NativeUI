package wfg.native_ui.ui;

import static wfg.native_ui.util.Globals.settings;

import java.util.List;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import rolflectionlib.util.RolfLectionUtil;

/**
 * Method fields for obfuscated vanilla UI classes.
 */
public final class MethodFields {
    private MethodFields() {};
    private static final Object clearChildrenMethod;
    private static final Object getChildrenCopyMethod;
    private static final Object getChildrenNonCopyMethod;
    private static final Object getFaderMethod;
    private static final Object addToPositionMethod;
    private static final Object removeFromPositionMethod;
    private static final Object positionSetParentMethod;
    private static final Object isSlidOutMethod;

    static {
        final UIPanelAPI panelIns = settings.createCustom(0f, 0f, null);
        final Class<?> panelClazz = panelIns.getClass();
        final Class<?> posClazz = panelIns.getPosition().getClass();

        clearChildrenMethod = RolfLectionUtil.getMethod(
            "clearChildren", panelClazz);
        getChildrenCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenCopy", panelClazz);
        getChildrenNonCopyMethod = RolfLectionUtil.getMethod(
            "getChildrenNonCopy", panelClazz);
        getFaderMethod = RolfLectionUtil.getMethod("getFader", panelClazz);
        isSlidOutMethod = RolfLectionUtil.getMethod("isSlidOut", panelClazz);
        addToPositionMethod = RolfLectionUtil.getMethod("add", posClazz, 1);
        removeFromPositionMethod = RolfLectionUtil.getMethod("remove", posClazz, 1);
        positionSetParentMethod = RolfLectionUtil.getMethod("setParent", posClazz, 1);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> clearChildren(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(clearChildrenMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenNonCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenNonCopyMethod, panel);
    }

    @SuppressWarnings("unchecked")
    public static final List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel) {
        return (List<UIComponentAPI>) RolfLectionUtil.invokeMethodDirectly(getChildrenCopyMethod, panel);
    }

    public static final void recomputePos(final PositionAPI position) {
        position.setSize(position.getWidth(), position.getHeight());
    }

    public static final Fader getPanelFader(UIComponentAPI panel) {
        return (Fader) RolfLectionUtil.invokeMethodDirectly(getFaderMethod, panel);
    }

    public static final boolean isSlidOut(UIComponentAPI panel) {
        return (boolean) RolfLectionUtil.invokeMethodDirectly(isSlidOutMethod, panel);
    }

    public static final PositionAPI addPositionOnly(UIComponentAPI comp, PositionAPI parentPos) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, parentPos);
        RolfLectionUtil.invokeMethodDirectly(addToPositionMethod, parentPos, position);
        return position;
    }

    public static final PositionAPI removePositionOnly(UIComponentAPI comp, PositionAPI parentPos) {
        final PositionAPI position = comp.getPosition();
        RolfLectionUtil.invokeMethodDirectly(positionSetParentMethod, position, (Object) null);
        RolfLectionUtil.invokeMethodDirectly(removeFromPositionMethod, parentPos, position);
        return position;
    }
}