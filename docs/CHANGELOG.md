# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [1.19.2-5.1.6.0] - 2024.01.28
### Added
- Added slot predicates to curio advancement equip triggers

## [1.19.2-5.1.5.0] - 2024.01.21
### Added
- [API] Added data generation helper methods and classes for triggers to advancements

## [1.19.2-5.1.4.4] - 2023.12.26
### Fixed
- Fixed validity checks not working in some cases

## [1.19.2-5.1.4.3] - 2023.11.26
### Fixed
- Fixed `CurioAttributeModifierEvent` crashing when used for slot modifiers [#351](https://github.com/TheIllusiveC4/Curios/issues/351)

## [1.19.2-5.1.4.2] - 2023.11.02
### Fixed
- Fixed network decoding errors [#346](https://github.com/TheIllusiveC4/Curios/issues/346)

## [1.19.2-5.1.4.1] - 2023.05.05
### Fixed
- Fixed default curio slot texture not showing properly

## [1.19.2-5.1.4.0] - 2023.04.16
### Added
- Added `CurioAttributeModifierEvent` for editing curio attribute modifiers in slots [#292](https://github.com/TheIllusiveC4/Curios/issues/292)

## [1.19.2-5.1.3.0] - 2023.03.02
### Added
- Added `/curios drop` command for dropping curio items
### Fixed
- Fixed curio entity selectors counting slots with size 0 erroneously

## [1.19.2-5.1.2.2] - 2023.02.15
### Fixed
- Fixed `SlotModifiersUpdatedEvent` not always firing

## [1.19.2-5.1.2.1] - 2023.02.15
### Fixed
- Fixed equip sounds still not playing properly in the Curios screen

## [1.19.2-5.1.2.0] - 2023.02.15
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
### Changed
- `ICurio#onEquipFromUse` now fires when items are placed into slots in inventory screens
### Fixed
- Fixed equip sounds not playing properly in the Curios screen (thanks bconlon!) [#281](https://github.com/TheIllusiveC4/Curios/pull/281)

## [1.19.2-5.1.1.0] - 2022.08.08
### Added
- Re-added JEI integration
### Changed
- Updated to Minecraft 1.19.2
- Updated to Forge 43.0.0+
- Updated Polish (pl_pl) localization (thanks Greg-21!) [#247](https://github.com/TheIllusiveC4/Curios/pull/247)
- Updated Ukranian (uk_ua) localization (thanks Sushomeister!) [#245](https://github.com/TheIllusiveC4/Curios/pull/245)

## [1.19.1-5.1.0.5] - 2022.07.28
### Changed
- Updated to Minecraft 1.19.1
- Updated to Forge 42.0.0+

## [1.19-5.1.0.4] - 2022.07.11
### Changed
- Updated to and requires Forge 41.0.94+

## [1.19-5.1.0.3] - 2022.07.10
### Changed
- Updated to and requires Forge 41.0.64+
### Removed
- Temporarily removed JEI integration as the mod is not compatible currently with the required versions of Forge

## [1.19-5.1.0.2] - 2022.06.20
### Added
- Re-added JEI integration
### Fixed
- Fixed datapack functions failing to load when utilizing Curios slot commands [#240](https://github.com/TheIllusiveC4/Curios/issues/240)
- Fixed null attributes crashing clients when hovering over curio tooltips [#242](https://github.com/TheIllusiveC4/Curios/issues/242)

## [1.19-5.1.0.1] - 2022.06.09
### Changed
- Revert `CurioSlot` patch
- Updated to and requires Forge 41.0.8+

## [1.19-5.1.0.0] - 2022.06.07
### Changed
- Updated to Minecraft 1.19+
- Updated to Forge 41+
