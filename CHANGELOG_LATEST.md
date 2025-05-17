The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.21.4/CHANGELOG.md).

## [10.0.1+1.21.4] - 2025.05.17

### Fixed

- Fixed loot context crash

## [10.0.0+1.21.4] - 2025.05.15

For an in-depth overview of API changes, refer to [this](https://docs.illusivesoulworks.com/curios/porting/1211to1214)
guide.

### Added

- Added ways for developers to listen to state changes (when an item is the same) and differentiate them from item changes:
    - `CurioChangeEvent` is now abstract, with added `CurioChangeEvent.Item` and `CurioChangeEvent.State` subclasses
    - `onStateChange` method added to `ICurio` and `ICurioItem`
- A new preset slot type, `feet`, has been added with an included default icon
- Added slot type predicates for more advanced checking in `CurioAttributeModifier`
- Added new data generation options, including the ability to generate tags within the same provider as the Curios data
- Added `curios:player_like` entity type tag, with entries for players and armor stands
- Added `entities` field to slot type data files for marking default entity types to add the slot type to automatically,
  without needing a corresponding `curios/entities` file
- Added the ability to create slots directly inside `curios/entities` data files, without needing a corresponding
  `curios/slots` file
- Added `curios:generic_exclusions` item tag for declaring curio items that should not be equipable into the generic
  `curio` slot by default
- Added preset slot type constants and tags to `CuriosSlotTypes` and `CuriosTags` respectively

### Changed

- Updated to Minecraft 1.21.4
- Following the state change additions, `onUnequip` and `onEquip` no longer fire if the detected items are the same
- `ICurioRenderer#render` parameters have been adjusted for Minecraft changes
- `ICurio.DropRule` has moved to `top.theillusivec4.curios.api.common.DropRule`
- `CurioAttributeModifierEvent` now uses `CurioAttributeModifiers` instead of maps
- Moved `SetCurioAttributesFunction` to the official API package

### Removed

- Removed all previously deprecated classes and methods

### Deprecated

- Deprecated slot type methods in `CuriosApi`, replaced by methods in `CuriosSlotTypes`.
- Deprecated `getAttributeModifiers` in `ICurio` and `ICurioItem`
- Deprecated `CuriosRendererRegistry`, replaced by methods in `ICurioRenderer`
- Deprecated the previous utility methods in `ICurioRenderer`, replaced by `setupHumanoidAnimations` and
  `copyHumanoidProperties`
- Deprecated attribute map methods in `CurioAttributeModifierEvent`
- Deprecated `CuriosTooltip`
- Deprecated `ISlotType#getIdentifier`, replaced by `getId`
- Deprecated `ICurioSlotExtension#getSlotTooltip`, replaced by `getSlotTooltip` with an additional parameter for
  original tooltip lines
