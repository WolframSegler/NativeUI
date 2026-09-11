# Using NativeUI in Your Mod - AI Rules

## Setup & Initialization
Declare the dependency in `mod_info.json` (for the mod loader), version might change:

```json
"dependencies": [
    {
        "id": "wfg_native_ui",
        "name": "NativeUI",
        "version": "prompt user to provide the latest version"
    }
]
```

The IDE needs to know the path to `native_ui.jar`. Assuming the parent dir of the root is `mods/`, here is the `pom.xml` entry:
```xml
<dependency>
    <groupId>wfg</groupId>
    <artifactId>native_ui</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/../NativeUI/jars/main/native_ui.jar</systemPath>
</dependency>
```

Source files are under `NativeUI/src/wfg/native_ui/`.

## Style Rules

### Number Suffixes
- **int** – no suffix: `42`
- **long** – always lowercase `l` suffix: `1234567l`
- **float** – always lowercase `f` suffix, whole numbers without `.0`: `6f` **not** `6.0f`
- **double** – always lowercase `d` suffix, whole numbers without `.0`: `7d` **not** `7.0d`

### Early Returns
Return immediately when a precondition fails. Keep the happy path unindented.

```java
public final void process(final Player player) {
    if (player == null) return;
    if (!player.isAlive()) return;
    doSomething();
}
```

### Single-Line Conditionals
If the body is short, omit braces and keep it on one line:

```java
if (value == 0) return;
if (list.isEmpty()) throw new IllegalStateException();
```

### `final` on Everything Possible
- **Methods**: always mark as `final`, even `private` or `static`.
- **Classes**: declare `final` unless explicitly designed for extension (utility classes are `final`).
- **Local variables**: mark as `final` whenever possible (except loop indices). Refactor to allow single assignment. Use `switch` expressions or `Math.clamp()` to avoid mutable intermediates.

```java
// multiple assignment, wrong
int clamped = rawValue;
if (rawValue < min) clamped = min;
else if (rawValue > max) clamped = max;

// single assignment, correct
final int clamped = Math.clamp(rawValue, min, max);


// eager default, wrong
String display = "Unknown";
if (state == ACTIVE) display = "Active";

// final from branch, correct
final String display;
if (state == ACTIVE) display = "Active";
else display = "Unknown";

// switch expression, correct (preferred)
final String display = switch (state) {
    case ACTIVE -> "Active";
    default -> "Unknown";
};
```

### Constants
- Extract reused values into `private static final` fields at the top of the class.
- Use `UPPER_SNAKE_CASE`: `BG_COLOR`, `MAX_HEALTH`.
- Compute compile-time constants (`static final` strings, math) in the field declaration.

### Naming
- **Members**: `camelCase`. Use prefixes: `m` for instance, `s` for static, `g` for global (e.g., `mPanel`, `gPanel`, `sPanel`). No underscore after prefix.
- **Constants**: `UPPER_SNAKE_CASE`.

### Encapsulation
Make fields `public` unless they need side-effect logic. **No** trivial getters/setters.

### Lambdas
Always use lambdas over anonymous classes:
```java
list.forEach(item -> process(item));
```

### Switch Expressions
Prefer `switch` expressions over `if-else` chains for value assignments:
```java
final String name = switch (type) {
    case A -> "Alpha";
    case B -> "Beta";
    default -> "Unknown";
};
```

### DRY
Extract reused logic into reusable `static final` methods.

### Modifier Order
Follow this exact sequence: `visibility` `static` `final` `abstract` `volatile` `synchronized`
```java
private static final int TIMEOUT = 5000;
public final synchronized void reset() { … }
```

### Method Header Formatting
- Opening brace on same line as closing parenthesis.
- For long signatures: break after `(`, align parameters on next line, and keep `)` `{` together.
```java
public final void process(Player player) {
    ...
}

public final void buildUI(
    Container parent,
    String title,
    boolean resizable,
    boolean addOutline,
    int outlineThickness
) {
    ...
}
```

- Never put brace on its own line.
- Never leave a closing parenthesis orphaned at the end of the last parameter line.

### UIConstants
Always import all constants statically:
```java
import static wfg.native_ui.util.UIConstants.*;
```
Then use the constants directly by name—no qualification.

Never hardcode 3, 5, or 10 for layout; always use these constants.
- `pad` (3) - tight spacing, often used for paragraphs or dense grids.
- `hpad` (5) – half of opad. Used when doing layout calculations.
- `opad` (10) – outer section padding. Used for inter-panel padding and significant gaps.

For semantic correctness, do `n * pad` and similar when layouting:
```java
public void buildUI() {
    final int panelSize = 37;
    final UIElement elem1 = new UIElement(panelSize, panelSize);
    final UIElement elem2 = new UIElement(panelSize, panelSize);
    final UIElement elem3 = new UIElement(panelSize, panelSize);

    add(elem1).inTL(pad, pad);
    add(elem2).inTL(pad, 2 * pad + panelSize);
    add(elem3).inTL(pad, 3 * pad + 2 * panelSize);
}
```

- `BUTTON_W` and `BUTTON_H` are suggested defaults. Use them if no explicit size is specified by the user.
Always use the precomputed color constants (if present). Do not call `Misc.getTextColor()` etc. directly in UI code:
- `text_color`, `highlight`, `positive`, `negative`, `base`, `dark`, `grid`, `bright`, `gray`, `glowHighlight`, `btnTxtColor`, `btnBgColorDark`
- `bgAlpha` (0.85f) – standard background opacity.
- `screenW`, `screenH` are the current screen size (pixels).
- The class contains additional constants (e.g.,`UI_BORDER_1–4`). If you need a value that isn't listed here, check the UIConstants source before hardcoding anything.

### NumFormat

Use the `NumFormat` utility class for all number formatting. Import the class normally:
```java
import wfg.native_ui.NumFormat;
```

Then call the methods directly on the class:

- `NumFormat.engNotate(long)` — engineer notation (3 significant digits) for large positive numbers:
  - `924` → `"924"`
  - `9245` → `"9.25K"`
  - `79245` → `"79.2K"`
  - `1024000000` → `"1.02B"`
  Automatically prepends `MINUS` ("−") for negative values.

- `NumFormat.formatCredit(long)` — comma‑grouped credit string with credit symbol (`¢`), e.g. `"1,234¢"`.
  - `NumFormat.formatCreditAbs(long)` for absolute value.

- `NumFormat.reverseEngNotate(float)` — readable multiplier display when value is barely above 1 (tiny deviations):
  - e.g., `1.002f` → `"1.0m"`.

- `NumFormat.formatMagnitudeAware(double)` — adaptive precision: small values get precise formatting, large values use engineer notation.

- `NumFormat.formatAdaptivePrecision(double)` — 1‑2 decimal places with rounding.

- `NumFormat.MINUS` — the correct minus sign character (`"−"`). Use this instead of `"-"` when you need to display a negative sign manually.

**Never** write your own number‑to‑string logic, delegate to `NumFormat`.

## Core Elements

### `UIElementAPI` and `UIElement`
- Use `PositionAPI pos()` instead of `getPosition()`.
- Use the convenience methods instead of first querying the position: `getX()`, `getY()`, `getCenterX()`, `getCenterY()`, `getWidth()`, `getHeight()`,
- `UIElementAPI` implements `UIComponentAPI`.
- Direct control over the parent: `UIPanelAPI getParent()`, `PositionAPI setParent(UIPanelAPI)`. `setParent` returns this element's PositionAPI for chaining.
- `advance`, `render` and `processInput` are final and call `advanceImpl`, `renderImpl` and `processInputImpl` instead. So override the `Impl` methods.
- `UIElement` has four members: `mFader`, `mPos`, `mParent` and `mOpacity`. All protected.
- Convenience methods for parent interaction (null safe) `bringToFront()`, `sendToBack()` and `detach()`.
- Three constructors for `UIElement`: empty, `(float width, float height)` and `(PositionAPI pos)`.
- The universal traversal order for UI elements in Starsector is `render -> processInput -> advance`. This information should not be needed, as the logic contained within the three methods should be order agnostic.

### `UIEntityAPI` and `UIEntity`
- Extends `UIElementAPI` and adds 3 new methods: `getUIComponentContainer()`, `getUISystemContainer()` and `initSystems()`.
- Use Short-hand alias methods `comp()` and `system()` instead of the `getUIComponentContainer()` and `getUISystemContainer()`.
- `initSystems()` uses `UIElementFlags` the class implements to automatically attach native systems. Custom systems must be attached manually by child constructor.
- Never access the private `UIEntity` members `compContainer` and `systemContainer` directly, as they are initialized lazily.
- Same constructors as `UIElement`.
- Overrides `advanceImpl`, `renderImpl` and `processInputImpl` to call the equivalent methods of `System`s. Classes that extend `UIEntity` must call `super` when overriding these.

### `UIContainerAPI` and `UIContainer`
- `UIContainerAPI` extends `UIPanelAPI`, but use `add()` and `remove()` instead of `addComponent()` and `removeComponent()`.
- New methods `getChildren()`, `getChildrenCopy()` and `clearChildren()`.
- Methods `addPos(UIComponentAPI)` and `removePos(UIComponentAPI)` adds/removes the component to/from PositionAPI hierarchy only.
- Overrides `advanceImpl`, `renderImpl` and `processInputImpl` to call `System` methods and children methods.
- `renderImpl` calls two default methods `renderBelowImpl()` and `renderAboveImpl()` relative to the children's render calls. Override these when extending `UIContainer` unless render order needs to be modified.
- `UIContainer` adds one new private field `ArrayList<UIComponentAPI> children`.
- Same constructors as `UIElement`.

## PositionAPI
- `PositionAPI` has no `setX()` or `setY()` methods; coordinates are always computed from the relative alignment parameters.
- The `getX` and `getY` methods return absolute screen coordinates fit for OpenGL draw calls (origin bottom left).
- When the size or the offsets of the `PositionAPI` changes, it triggers the internal `recompute` method, which updates its position and calls `recompute` on all children and sister `PositionAPI`'s that are positioned relative to it. `recompute` is an expensive method.
- When a position method is called `inTL`, `inBR`, `inBMid` etc., they all call the internal `relativeTo` method.

### The recompute formula
For each element, posX and posY (the bottom-left corner) are calculated as:
```java
posX = base.posX + (baseAnchorX * base.width) + (selfAlignX * width) + marginX + offsetX
posY = base.posY + (baseAnchorY * base.height) + (selfAlignY * width) + marginY + offsetY
```
- **`base`**: The reference `PositionAPI` instance. If `null`, the element's `parent` is used. All positioning is relative to the base's bottom-left corner (base.posX, base.posY).
- **`baseAnchorX`, `baseAnchorY`**: Fractions (0 to 1) that define an anchor point on the base's bounding box. Multiplying by the base's width/height converts these fractions into pixel coordinates relative to the base's origin.
    - **`baseAnchorX`**: `0` = left edge, `0.5` = horizontal centre, `1` = right edge.
    - **`baseAnchorY`**: `0` = bottom edge, `0.5` = vertical centre, `1` = top edge.
- **`selfAlignX`, `selfAlignY`**: Fractions that specify how the element’s own bounding box is aligned relative to the anchor point. Multiplying by the element's own width/height gives the necessary shift.
    - **`selfAlignX`**: `0` = element's left edge at the anchor, `-0.5` = element's horizontal centre at the anchor, `-1` = element's right edge at the anchor.
    - **`selfAlignY`**: `0` = element's bottom edge at the anchor, `-0.5` = element's vertical centre at the anchor, `-1` = element's top edge at the anchor.
- **`marginX`, `marginY`**: Pixel offsets added after the alignment calculation. These are the gap/spacing values passes to methods like `inTL(gapX, gapY)`.
- **`offsetX`, `offsetY`**: Additional manual offsets, separate from alignment margins. They default to 0 and can be adjusted via a dedicated offset setter.

### The relativeTo formula
Every positioning shortcut like `inTL(x, y)`, `inMid()`, `aboveLeft(target, gap)`, etc., simply calls:
```java
relativeTo(target, baseAnchorX, baseAnchorY, selfAlignX, selfAlignY, marginX, marginY) // setting target as null uses parent
```
- **Examples**:
    - `inTL(gapX, gapY)` -> `relativeTo(null, 0f, 1f, 0f, -1f, gapX, -gapY)`.
    - `inBMid(gapY)` -> `relativeTo(null, 0.5f, 0f, -0.5f, 0f, 0f, gapY)`.
- You cannot call `relativeTo` directly, as it is not present inside `PositionAPI`.

## Components
NativeUI uses a composition‑oriented, ECS‑inspired model. Every `UIEntity` holds a `UIComponentContainer`, which stores components; systems provide behaviour.
If a feature is reused by many different elements, it should be a **component + system** pair (e.g., tooltips, hover glow). UIElement-specific logic stays as local code.

### UIComponentContainer
- **Native components**: fixed set defined by the `NativeComponents` enum (e.g., `BACKGROUND`, `TOOLTIP`, `INTERACTION`). Accessed via `get(NativeComponents.TYPE)` and `set(NativeComponents.TYPE, comp)`.
- **Custom components**: an open list for user-added logic. Accessed via `getCustom(Class<T>)` and `addCustom(comp)`.
- Check existence with `has()` / `hasCustom()`. For safe lazy init use `setIfNotPresent()` / `addCustomIfNotPresent()` (preferred over `if(!has()) set()`).
- Only systems attach components. Components are useless without a system to act on them, so each system adds the components it needs if they aren't already present.
- All components extend `BaseComponent`:
    - `enabled` (boolean) – systems must respect this flag.
    - Do not subclass components to add behaviour. Instead, configure them via their public fields or functional hooks (e.g., `onClicked`, `onHover`).
    - **Write rule**: if an entity exposes a convenience setter for a component field (e.g., `setTooltipEnabled()`), use that setter instead of writing the field directly. Direct writes can leave dependent state out of sync.

### How to access components
```java
public final BackgroundComp bg = container.get(NativeComponents.BACKGROUND);
public final MyCustomComp cc = container.getCustom(MyCustomComp.class);
```

## Systems
Systems are **singleton**, **stateless** objects that provide behaviour to entities via components. They do not store per-element data; all element-specific state lives in components, which the system itself attaches during initialisation. Native systems are registered in the `NativeSystems` enum. Custom systems follow the same pattern but are added manually.

### UISystemContainer

Every `UIEntityAPI` holds a `UISystemContainer` accessed via `element.system()`. It works exactly like the component container:
- **Native systems**: fixed set from `NativeSystems` enum (e.g.,  `BACKGROUND`, `TOOLTIP`, `INTERACTION`). Use `get(NativeSystems.TYPE)` / `set(NativeSystems.TYPE, system, element)`.
- **Custom systems**: open list, accessed via `getCustom(Class<T>)` / `addCustom(system, element)`.
- Check existence with `has()` / `hasCustom()`. For lazy initialisation, use `setIfNotPresent()` / `addCustomIfNotPresent()`.

### BaseSystem
All systems extend `BaseSystem`. They are **singletons**. Get the instance via a static `get()` method, and the constructor is private.

- `init(UIEntityAPI element)`: called once when the system is attached. This is where the system attaches the components it needs by calling `element.comp().setIfNotPresent(...)` and optionally registers other systems with `element.system().setIfNotPresent(...)`.
- `processInput(UIEntityAPI element, List<InputEventAPI> events)`, `advance(UIEntityAPI element, float delta)`, `renderBelow(UIEntityAPI element, float alpha)` and `renderAbove(UIEntityAPI element, float alpha)`.
- `onRemove(UIEntityAPI element)` cleanup when the system is removed from an element (if needed).
- Systems must not store element-specific state in fields. If per-element data is needed, put it in a component.

```java
public final class MySystem extends BaseSystem {
    private MySystem() {};
    private static final MySystem INSTANCE = new MySystem();
    public static final MySystem get() { return INSTANCE; }

    @Override
    public void init(UIEntityAPI element) {
        element.comp().setIfNotPresent(NativeComponents.INTERACTION, new InteractionComp<>());
        element.system().setIfNotPresent(NativeSystems.INPUT_SNAPSHOT, RawInputSystem.get(), element);
        element.system().addCustomIfNotPresent(MyOtherSystem.get(), element); // presence is checked using class type.
    }
}
```

### UIElementFlags

```java
import wfg.native_ui.ui.core.UIElementFlags;
```

`UIElementFlags` is a set of **marker interfaces** that a `UIEntity` subclass can implement to opt into native systems.  
No methods need to be overridden; simply `implements` the desired flags.

The `UIEntity` constructors call `initSystems()`, which checks `instanceof` for each flag and calls `system().setIfNotPresent()` with the appropriate singleton system.  
You never need to call `initSystems()` manually; it runs automatically from every constructor.

Example:

```java
public final class MyPanel extends UIEntity
    implements UIElementFlags.HasInteraction, UIElementFlags.HasTooltip {

    public MyPanel() {
        super(); // InteractionSystem and TooltipSystem are automatically registered
    }
}
```
Custom systems must still be registered manually (see `init()` example above). The flags only cover the built‑in native systems.

## Attachments

`Attachments` provides **static access points** to vanilla UI panels across all game states (campaign, combat, title). Use these methods to obtain a parent `UIPanelAPI` where you can insert NativeUI elements.

```java
import wfg.native_ui.ui.Attachments;
```
All methods return `UIPanelAPI` (may be `null` if the required state is not active). Not null-safe, so check the return before use.

### Key accessors

- **`getScreenPanel()`** – universal: returns the main screen panel depending on current state.

- **`getCoreUI()`** – in campaign:  
  - If an interaction dialog is active, returns the interaction's core UI.  
  - Otherwise, returns the campaign core UI (the main panel with tabs).

- **`getCurrentTab()`** – in campaign:  
  - Returns the panel of the currently selected tab (FLEET, CHARACTER, etc.), either from an interaction or the campaign core.

- **`getCampaignScreenPanel()`** – campaign only: the outermost campaign screen panel (the one used as the dialog/tooltip parent).

- **`getCombatScreenPanel()`** – combat only: the combat widget panel. The root panel for `CombatState`, which extends `AppState`.

- **`getWarroomPanel()`** – combat only: the warroom overlay. The tactical map root panel.

- **`getTutorialOverlay()`** – combat only: the tutorial overlay panel. Rarely used.

- **`getTitleScreenPanel()`** – title screen only: the title screen panel. root panel.

- **`getInteractionDialog()`** – returns the current `InteractionDialogAPI` if interacting with an entity; `null` otherwise.

- **`getInteractionCoreUI()`** – returns the core UI panel of the current interaction dialog, if one is active.

- **`getInteractionCurrentTab()`** – returns the currently active tab panel within an interaction dialog.

- **`getCampaignCoreUI()`** – returns the campaign core UI (ignoring any interaction dialog).

- **`getCampaignCurrentTab()`** – returns the current tab panel in the campaign core UI.

### Usage

Use the returned panel as a parent to attach NativeUI containers.
Critical – Always use `wrapAndAdd()`, never `addComponent()`. Starting with Starsector v0.9.85, vanilla UIPanelAPI provides:
```java
UIComponentAPI wrapAndAdd(UIComponentAPI component, FaderUtil fader); // FaderUtil is required, since each UIComponentAPI must have their own FaderUtil.
```
This wraps the NativeUI component so vanilla treats it as an opaque child, not as a core implementation class.
- **Never** call `parent.addComponent(myNativePanel)` on a vanilla panel. That will cause a `ClassCastException` because the game expects its own internal UI classes.
- Always use `wrapAndAdd()` (pass `UIElement` fader) to safely attach NativeUI container.

**Example:**
```java
final UIPanelAPI parent = Attachments.getCurrentTab();
if (parent == null) return;

final MyNativePanel myPanel = new MyNativePanel();
parent.wrapAndAdd(myPanel, myPanel.getFader()); // ✅ correct
// parent.addComponent(myPanel); // ❌ will throw ClassCastException
```

Important: The returned panels are vanilla UIPanelAPI objects. You can add your NativeUI components as children, but follow the usual NativeUI rules inside your own container.

## UIEventBus
`UIEventBus` is a **cross‑mod communication channel** that notifies listeners about panel lifecycle events.  
You do not need it for standard single‑mod usage; it exists so other mods can react to your UI panels being attached, detached, or refreshed.

### How it works
- Panels that want to be identifiable implement `IdentifiedPanel` and return a unique `getPanelId()`.
- Listeners register via `UIEventBus.addListener(listener)` (implementing `UILifecycleListener`).
- When NativeUI adds/removes/refreshes a panel, the bus fires the corresponding method on all registered listeners, passing the panel and its ID (or `null` if not identifiable).

### Listener interface

```java
public interface UILifecycleListener {
    void panelAttached(UIComponentAPI comp, String compID);
    void panelDetached(UIComponentAPI comp, String compID);
    default void panelRefreshed(UIComponentAPI comp, String compID) {}
}
```

## NativeUiUtils

Utility class with common helper methods for UI layout, positioning, color manipulation, and input checks.

- **color helpers**
  - `adjustBrightness(Color base, float factor)` – returns a new color with RGB scaled by `factor`, alpha preserved.
  - `setAlpha(Color color, float alphaMult)` / `setAlpha(Color color, int alpha)` – returns a new color with alpha multiplied or set, RGB unchanged.
  - `lerpColor(Color c1, Color c2, float t)` – linear interpolation between two colors (0 ≤ t ≤ 1).

- **Positioning / anchoring**
  - `anchorPanel(UIComponentAPI panel, UIComponentAPI anchor, AnchorType type, int gap)` – manually calculates a relative offset so `panel` sits at a chosen edge/corner of `anchor`, with a pixel `gap`. Returns the offset; does **not** handle screen bounds.
  - `anchorPanelWithBounds(panel, anchor, type, gap)` – calls `anchorPanel` then clamps the resulting position to stay within the screen, respecting `opad`. Automatically compensates for tooltip codex entries.
  - `swapPositions(UIComponentAPI comp1, UIComponentAPI comp2)` – swaps the visual positions of two components.
  - `mouseCornerPos(TooltipMakerAPI tooltip, int opad)` – positions a tooltip near the mouse, with screen‑edge clamping.

- **Tooltip layout**
  - `resetFlowLeft(TooltipMakerAPI tooltip, float gap)` – resets the tooltip’s flow so the next element is drawn from the top‑left corner, preserving the current height.

- **Sprite rotation**
  - `rotateSprite(Vector2f origin, Vector2f target, SpriteAPI sprite)` – sets the sprite’s angle to face from `origin` to `target`.

- **Input / mouse utilities**
  - `containsMouse(PositionAPI pos)` / `containsMouse(Rect rect)` – returns `true` if the mouse is inside the given region.
  - `containsPoint(Rect rect, float x, float y)` – generic point-in-rectangle test.
  - `intersects(PositionAPI a, PositionAPI b)` – AABB intersection test.
  - `isMouseDown()` – left or right mouse button currently pressed.
  - `isCtrlDown()`, `isShiftDown()`, `isAltDown()` – keyboard modifier checks.

- **Helper types**
  - `Rect` – simple mutable rectangle (x, y, w, h) with `containsEvent(InputEventAPI)`.
  - `AnchorType` enum – comprehensive set of anchor points (`LeftTop`, `RightMid`, `BottomLeft`, `MidTopRight`, etc.) for use with `anchorPanel`/`anchorPanelWithBounds`.

All methods are `static`. Import via `import wfg.native_ui.util.NativeUiUtils;`.

## RenderUtils

- **drawFramedBorder** – draws a rectangular border frame with adjustable thickness, either outward or inward.
- **drawQuad** – draws a filled rectangle (quad) with optional additive blending.
- **drawAdditiveGlow** – renders a sprite with additive blend and a colored glow effect.
- **setGlColor** – sets the current OpenGL color using a `Color` and alpha multiplier.
- **drawSpriteOutline** – draws a soft outline around a sprite using repeated rotated renders.
- **drawGradientSprite** – draws a textured line segment with a perpendicular alpha gradient (fades from start to end through middle).
- **drawHighlightBar** – draws a progress‑bar‑like shape with a shiny top and a gradient body, optionally as a dark overlay.
- **blendColors** – blends two colors by a given factor (0 = pure first, 1 = pure second).
- **drawPolygon** – draws a filled convex polygon from an array of XY vertex pairs (CCW order).
- **buildCornersVertices** – builds a vertex array for a rectangle with corner cuts, suitable for `drawPolygon`.
- **drawRoundedBorder** – draws a rounded, 9‑slice border using the sprite prefixes defined in `UIConstants` (UI_BORDER_1–4).
- **drawGradientQuad** – draws a filled quad with a different color at each corner (smooth gradient).
- **quadWithBlend** – draws a quad with a special blending mode (GL_SRC_ALPHA, GL_ZERO) used for stencil‑like masking.
- **quadNoBlend** – draws a quad without blending, useful for resetting color masks or solid shapes.

## Example Index

- **BaseContainer** -> `wfg.native_ui.example.container.BaseContainerExample`
- **DockPanel** -> `wfg.native_ui.example.container.DockPanelExample`
- **ScrollPanel** -> `wfg.native_ui.example.container.ScrollPanelExample`
- **VanillaScrollPanelExample** -> `wfg.native_ui.example.container.VanillaScrollPanelExample`
- **DialogPanel** -> `wfg.native_ui.example.dialog.DialogPanelExample`
- **UIClickable** -> `wfg.native_ui.example.interaction.UIClickableExample`
- **GridTable** -> `wfg.native_ui.example.table.GridTableExample`
- **SortableTable** -> `wfg.native_ui.example.table.SortableTableExample`
- **IdentityMarker** -> no example (trivial identifier wrapper)
- **AbstractSpriteElement** -> no example (abstract base class; use `SpriteElement`)
- **SpriteElement** -> no example (trivial; see source)
- **IconValuePair** / **IconValuePairTp** -> `wfg.native_ui.example.visual.IconValuePairExample`
- **InteractiveSprite** -> no example (trivial; see source)
- **PieChart** -> `wfg.native_ui.example.visual.PieChartExample`
- **TextWrapper** -> `wfg.native_ui.example.visual.TextWrapperExample`
- **Button** -> `wfg.native_ui.example.widget.ButtonExample`
- **CheckboxButton** -> `wfg.native_ui.example.widget.CheckboxButtonExample`
- **DockButton** -> `wfg.native_ui.example.widget.DockButtonExample`
- **DockClickable** -> no example (trivial; see `DockButton` for similar pattern)
- **MultiSelect** -> `wfg.native_ui.example.widget.MultiSelectExample`
- **RadioPanel** -> `wfg.native_ui.example.widget.RadioPanelExample`
- **Slider** -> `wfg.native_ui.example.widget.SliderExample`
- **TextField** -> `wfg.native_ui.example.widget.TextFieldExample`
- **ToastExample** -> `wfg.native_ui.example.widget.ToastExample`