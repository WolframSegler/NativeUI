package wfg.native_ui.ui.components;

import wfg.native_ui.ui.UIContext;
import wfg.native_ui.ui.UIContext.Context;

public final class UIContextComp extends BaseComponent {
    public Context target = Context.NONE;
    public boolean ignore = false;

    public final boolean isValid() {
        return ignore || UIContext.is(target);
    }
}