# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [1.18.2-5.0.9.2] - 2023.12.25
### Fixed
- Fixed validity checks not working in some cases

## [1.18.2-5.0.9.1] - 2023.06.15
### Fixed
- Fixed possible NullPointerException crash from SlotResult

## [1.18.2-5.0.9.0] - 2023.03.02
### Added
- Added `/curios drop` command for dropping curio items
### Fixed
- Fixed curio entity selectors counting slots with size 0 erroneously

## [1.18.2-5.0.8.0] - 2023.02.15
### Added
- Added new `ICurio#canWalkOnPowderedSnow` method for curio implementations that allow walking on Powdered Snow blocks
  (thanks bconlon!) [#273](https://github.com/TheIllusiveC4/Curios/pull/273)
- Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers [#276](https://github.com/TheIllusiveC4/Curios/issues/276)
- Added two commands:
  - `/curios replace <slot> <index> <player> with <item> [count]` for setting items to curio slots
  - `/curios list` for listing all registered slots and which mods they come from [#261](https://github.com/TheIllusiveC4/Curios/issues/261)
- Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and getting
  items from curio slots respectively

## [1.18.2-5.0.7.1] - 2022.06.20
### Fixed
- Fixed datapack functions failing to load when utilizing Curios slot commands [#240](https://github.com/TheIllusiveC4/Curios/issues/240)
- Fixed null attributes crashing clients when hovering over curio tooltips [#242](https://github.com/TheIllusiveC4/Curios/issues/242)

## [1.18.2-5.0.7.0] - 2022.04.12
### Added
- Added `it_it` localization (thanks BlackShadow77!) [#235](https://github.com/TheIllusiveC4/Curios/pull/235)
- Added `curios:set_curio_attributes` loot function
- Added support for `minecraft:freeze_immune_wearables` tagged items in curios slots
### Fixed
- Fixed curios Fortune levels being applied to other enchantments [#234](https://github.com/TheIllusiveC4/Curios/issues/234)

## [1.18.2-5.0.6.3] - 2022.03.02
### Changed
- Updated to Minecraft 1.18.2

## [1.18.1-5.0.6.2] - 2022.02.25
### Changed
- Updated `ko_kr.json` localization (thanks PixVoxel!) [#223](https://github.com/TheIllusiveC4/Curios/pull/223)
### Fixed
- Fixed curios being unequipped when used with additional slots and relogging [#218](https://github.com/TheIllusiveC4/Curios/issues/218)
- Fixed NPE crash with certain mods that implement their own Curios providers [#225](https://github.com/TheIllusiveC4/Curios/issues/225)

## [1.18.1-5.0.6.1] - 2022.02.15
### Changed
- Updated `ko_kr.json` localization (thanks mindy15963!) [#211](https://github.com/TheIllusiveC4/Curios/pull/211)
### Fixed
- Fixed `ICurioItem#getEquipSound` not being called [#222](https://github.com/TheIllusiveC4/Curios/issues/222)
- Fixed crashes with out-of-bound indices when unequipping items that provide slot attribute modifiers [#221](https://github.com/TheIllusiveC4/Curios/issues/221)

## [1.18.1-5.0.6.0] - 2022.01.18
### Added
- Added `uk_ua.json` localization (thanks Sushomeister!)
- Added new entity selector option, `curios=`. More information at the [wiki](https://github.com/TheIllusiveC4/Curios/wiki/Commands#entity-selector-options).
### Changed
- Updated `ru_ru.json` localization (thanks Sushomeister!)

## [1.18.1-5.0.5.2] - 2022.01.15
### Fixed
- Fixed deprecated usages of `ISlotHelper#unlockSlotType` and `ISlotHelper#lockSlotType` modifying slots differently
from previous behavior
- Fixed deprecated usages of `ISlotHelper#growSlotType` and `ISlotHelper#shrinkSlotType` logging client-side errors to
the console while in the Curios inventory

## [1.18.1-5.0.5.1] - 2021.01.09
### Fixed
- Fixed dragged items desyncing when switching between Curios and the Creative inventory screen [#202](https://github.com/TheIllusiveC4/Curios/issues/202)

## [1.18.1-5.0.5.0] - 2021.01.08
### Added
- [API] Added `findFirstCurio` and `findCurios` methods to `ICuriosHelper`
### Changed
- Updated `ko_kr` localization (thanks mindy15963!) [#200](https://github.com/TheIllusiveC4/Curios/pull/200)
### Deprecated
- [API] Deprecated `findEquippedCurio` methods in `ICuriosHelper`
### Fixed
- Fixed knockback resistance tooltips not being formatted correctly
- Fixed dragged items desyncing when switching between Curios and vanilla screens
- Fixed clearing inventory via Creative 'x' button resulting in lingering attribute modifiers when equipped 

## [1.18.1-5.0.4.2] - 2021.12.27
### Fixed
- Fixed client-side syncing errors when applying slot modifiers to slots with base size 0
- Fixed slot amount changes persistent when not applicable

## [1.18.1-5.0.4.1] - 2021.12.24
### Fixed
- Fixed crash related to Fortune loot functions [#198](https://github.com/TheIllusiveC4/Curios/issues/198)

## [1.18.1-5.0.4.0] - 2021.12.23
### Added
- [API] Added `makesPiglinNeutral` method for creating curios that can make piglins neutral like gold armor [#86](https://github.com/TheIllusiveC4/Curios/issues/86)
- [API] Added `isEnderMask` method for creating curios that can hide player heads from Endermen like carved pumpkins [#196](https://github.com/TheIllusiveC4/Curios/issues/196)
- Added mixins, developers building against this version of Curios and newer will need to make sure their environment is
configured for mixin dependency development (see README.md)
### Changed
- Replaced fortune global loot modifier with internal methods

## [1.18.1-5.0.3.1] - 2021.12.19
### Fixed
- Fixed slot amount changes not persisting when applicable
- Fixed slot amount changes causing loading errors [#195](https://github.com/TheIllusiveC4/Curios/issues/195)

## [1.18.1-5.0.3.0] - 2021.12.14
### Added
- Re-add Just Enough Items integration
### Changed
- Updated to Minecraft 1.18.1
- Updated Russian localization (thanks DrHesperus!) [#190](https://github.com/TheIllusiveC4/Curios/pull/190)

## [1.18-5.0.2.5] - 2021.12.14
### Changed
- [API] `ICurio#canEquipFromUse` and `ICurio#onEquipFromUse` methods are now called both client-side and server-side
  (previously server-side only)
### Fixed
- Fixed curio items not calling their `use` logic client-side [#192](https://github.com/TheIllusiveC4/Curios/issues/192)

## [1.18-5.0.2.4] - 2021.12.05
### Fixed
- Fixed crash with client-side player entities [#189](https://github.com/TheIllusiveC4/Curios/issues/189)

## [1.18-5.0.2.3] - 2021.12.02
### Fixed
- Fixed crash when running data generation in development environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.18-5.0.2.2] - 2021.12.02
### Fixed
- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)

## [1.18-5.0.2.1] - 2021.11.30
### Changed
- Updated to Minecraft 1.18
- Updated to Forge 38+
