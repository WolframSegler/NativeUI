v0.3.2-beta
- Fixed height compensation for tooltips inside the AnchorPanel method of NativeUiUtils.
- Replaced createPanel() with buildUI().
- Optimize some code by making the sprites a private static member.
- Added CheckboxButton Panel.
- Fixed DockPanel side not being applied at the constructor.
- Added some more documentation.
- Added an array map to utilities.
- Added a RadioPanel.
- The slider is now user adjustable by default.  

v0.3.1-beta
- Fixed issues with Dock Panel.
- Fixed alignment issues with SortableTable.
- Each component now has its own offset.
- Interaction shortcuts are now consumed.
- Fixed table column tooltips.
- Added outside click detector.
- Fixed lack of MouseMoveEvent not updating the mouse position.
- Fixed text padding inside DialogPanel.
- Fixed invisible panels getting a tooltip.

v0.3.0-beta
- Modified HasTooltip to reduce boilerplate.
- Turned all the interfaces of CustomPanel to components.
- CustomPanel now owns a ComponentContainer member.
- CustomPanel now owns the systems.
- Replaced all plugins for CustomPanel in favor of a forwarder plugin.
- Renamed the packaging from wrap_ui to native_ui.

v0.2.1-beta
- Misc. bug fixes.
- Made SpritePanel and SpritePanelWithTp a lot leaner in terms of composition.
- Added createTooltip and addTooltip to ComponentFactory for CustomPanelAPI independent Tooltips.
- Added addCaptionValueBlock to ComponentFactory to have values with titles.
- Removed ParentType from CustomPanel, as it does not need to be a CustomPanelAPI instance anymore.
- Removed CustomPanelAPI dependency fully. Ready to switch to a custom UIPanelAPI Impl. for the future.


v0.2.0-beta
- Created a carbon copy of DialogPanel because the API for vanilla version was too restrictive.
- Create new Attachments for UI injection.
- Switched to RolflectionLib.jar to not depend on kotlin.
- Removed old dialog wrappers.
- Misc. bug fixes.

v0.1.0-beta
- Initial release of WrapUI.