# NativeUI
Composition-oriented UI framework with use of panels instead of custom plugins.

# Features
- Global UI attachment points via [`Attachments`](src/wfg/native_ui/ui/Attachments.java).
- Core UI classes [`UIElement`](src/wfg/native_ui/internal/ui/core/UIElement.java), [`UIEntity`](src/wfg/native_ui/internal/ui/core/UIEntity.java)  and [`UIContainer`](src/wfg/native_ui/internal/ui/core/UIContainer.java) for full control over the UI hierarchy.
- [`Systems`](src/wfg/native_ui/ui/system/BaseSystem.java) and [`Components`](src/wfg/native_ui/ui/component/BaseComponent.java) for tooltips, hoverGlow etc. to reduce boilerplate, with a more composition-oriented approach. Native systems can be attached by implementing [`UIElementFlags`](src/wfg/native_ui/ui/core/UIElementFlags.java).
- [`UIEventBus`](src/wfg/native_ui/ui/event/UIEventBus.java) is used by [`UIElement`](src/wfg/native_ui/internal/ui/core/UIElement.java) and [`UIContainer`](src/wfg/native_ui/internal/ui/core/UIContainer.java) to let mods cross-communicate and handle attachment and detachment events.

# UI Elements
- [`UIClickable`](src/wfg/native_ui/ui/functional/UIClickable.java) has click handling and click sounds. Can take in a callback.
- [`Button`](src/wfg/native_ui/ui/functional/Button.java) implementation similar to vanilla, without using `ButtonAPI`.
- [`CheckboxButton`](src/wfg/native_ui/ui/functional/CheckboxButton.java) subclassing Button and using the checkbox sprite.
- [`RadioPanel`](src/wfg/native_ui/ui/widget/RadioPanel.java) for having a list of mutually exclusive options to choose from.
- [`MultiSelect`](src/wfg/native_ui/ui/widget/MultiSelect.java) for having multiple options to choose from.
- Simple [`PieChart`](src/wfg/native_ui/ui/visual/PieChart.java).
- Carbon copy of the vanilla [`Slider`](src/wfg/native_ui/ui/widget/Slider.java) used in settings (the blue one).
- [`SortableTable`](src/wfg/native_ui/ui/table/SortableTable.java) similar to `UITable` in functionality, avoiding the obfuscated vanilla table.
- [`GridTable`](src/wfg/native_ui/ui/table/GridTable.java) stacks widgets in a grid with uniform gaps in-between.
- [`SpriteElement`](src/wfg/native_ui/ui/visual/AbstractSpriteElement.java) with optional tooltip; essentially a `SpriteAPI` wrapper.
- [`ScrollPanel`](src/wfg/native_ui/ui/container/ScrollPanel.java) for vertical and horizontal scroll.
- [`DialogPanel`](src/wfg/native_ui/ui/dialog/DialogPanel.java) is the vanilla Folding Dialog Panel but without the annoying vanilla API.
- [`DockPanel`](src/wfg/native_ui/ui/container/DockPanel.java) docks to the specified side of the screen and can move in and out of the viewport.
- [`DockClickable`](src/wfg/native_ui/ui/functional/DockClickable.java) and [`DockButton`](src/wfg/native_ui/ui/functional/DockButton.java) manage the lifecycle of a [`DockPanel`](src/wfg/native_ui/ui/container/DockPanel.java).
- [`IconValuePair`](src/wfg/native_ui/ui/visual/IconValuePair.java) standardizes the formatting of icon-value pairs. Also includes a variant with tooltip support.
- [`TextWrapper`](src/wfg/native_ui/ui/visual/TextWrapper.java) wraps an [`AudioSystem`](src/wfg/native_ui/ui/system/AudioFeedbackSystem.java) and a [`TooltipSystem`](src/wfg/native_ui/ui/system/TooltipSystem.java) around a `LabelAPI` in a slim package.
- [`Toast`](src/wfg/native_ui/ui/widget/Toast.java) holographic notification docked to the left or right edge with an optional countdown bar.

# Utils
- [`UIConstants`](src/wfg/native_ui/util/UIConstants.java) houses commonly used values and colors to improve readability. Intended for static import.
- [`NativeUiUtils`](src/wfg/native_ui/util/NativeUiUtils.java) contains miscellaneous helper methods used throughout the framework.
- [`RenderUtils`](src/wfg/native_ui/util/RenderUtils.java) provides draw calls for common OpenGL boilerplate.
- [`NumFormat`](src/wfg/native_ui/util/NumFormat.java) formats very large or very small numbers.
- Android [`ArrayMap`](src/wfg/native_ui/util/ArrayMap.java) is a lightweight ordered map backed by arrays, intended for cases where a HashMap would be unnecessary.
- [`IdentityMarker`](src/wfg/native_ui/ui/util/IdentityMarker.java) can be used to check the "freshness" of a vanilla UI element.
- [`ComponentFactory`](src/wfg/native_ui/ui/ComponentFactory.java) provides compositions of already existing UI elements that do not deserve their own class plus some other misc stuff.

# Usage
- All panels that wish to use Systems must implement [`UIEntityAPI`](src/wfg/native_ui/ui/core/UIEntityAPI.java).

<br><br><br>

# QnA
- How do I see the java documentation for classes?
    - Make sure to include the src file as a dependency. The JAR file does not contain the documentation.
- I want to contribute, what conventions should I follow?
    - Please consult [`CONTRIBUTING`](CONTRIBUTING.md).
- How can I make my agent write more competent NativeUI code?
    - All the extra details about the framework an agent needs to know is contained within [`AGENTS`](AGENTS.md). Point your agent to it.

## Credits
rolfosian - for providing the [reflection library](https://github.com/rolfosian/RolflectionLib-SS).