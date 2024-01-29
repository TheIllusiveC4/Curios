# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
Prior to version 5.2.0, this projected used [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [5.6.0+1.20.1] - 2024.01.28
### Added
- Added slot predicates to curio advancement equip triggers

## [5.5.0+1.20.1] - 2024.01.19
### Added
- [API] Added data generation helper methods and classes for generating entities/slots data files and adding curios
triggers to advancements, see [documentation](https://docs.illusivesoulworks.com/curios/Developing%20with%20Curios/data-generation)
for more information

## [5.4.7+1.20.1] - 2023.12.25
### Fixed
- Fixed validity checks not working in some cases
- Fixed `CurioUnequipEvent` not firing when swapping items from use

## [5.4.6+1.20.1] - 2023.12.15
### Changed
- NBT predicates and certain inventory checks will now include Curios items automatically [#357](https://github.com/TheIllusiveC4/Curios/issues/357)
### Fixed
- Fixed NPE crashes if a mod tries to get the Curios inventory from a `null` entity [#358](https://github.com/TheIllusiveC4/Curios/issues/358)

## [5.4.5+1.20.1] - 2023.11.29
### Fixed
- Fixed slots desyncing after dying or dimension changing [#353](https://github.com/TheIllusiveC4/Curios/issues/353)

## [5.4.4+1.20.1] - 2023.11.28
### Changed
- Curios inventory keybinding now also closes the current container to mimic the behavior of the vanilla inventory keybinding [#352](https://github.com/TheIllusiveC4/Curios/issues/352)
### Fixed
- Fixed slots failing to sync after world load [#347](https://github.com/TheIllusiveC4/Curios/issues/347)
- Fixed lost icons when reloading datapacks

## [5.4.3+1.20.1] - 2023.11.26
### Fixed
- Fixed `CurioAttributeModifierEvent` crashing when used for slot modifiers [#351](https://github.com/TheIllusiveC4/Curios/issues/351)

## [5.4.2+1.20.1] - 2023.11.02
### Fixed
- Fixed network decoding errors [#346](https://github.com/TheIllusiveC4/Curios/issues/346)

## [5.4.1+1.20.1] - 2023.10.25
### Fixed
- Fixed `list` command not outputting all possible data

## [5.4.0+1.20.1] - 2023.10.23
### Added
- Added `replace` fields to entity files to clear previously assigned slots to entities
### Changed
- Using set operations in slot files with `replace` set to true will now reset previous add and remove operations
### Fixed
- Fixed `replace` fields not working properly for slot loading, this may cause current slot configurations to change when
updating

## [5.3.5+1.20.1] - 2023.10.03
### Fixed
- Fixed `keepInventory` gamerule applying to non-player entities for curios slots

## [5.3.4+1.20.1] - 2023.09.24
### Fixed
- Fixed items not showing their curio tooltips on clients connected to dedicated servers [#337](https://github.com/TheIllusiveC4/Curios/issues/337)

## [5.3.3+1.20.1] - 2023.09.21
### Fixed
- Fixed the `"replace"` field causing incorrect slot amounts during slot loading

## [5.3.2+1.20.1] - 2023.09.19
### Fixed
- Fixed slot loading on client-only entities

## [5.3.1+1.20.1] - 2023.09.04
### Fixed
- Fixed multiplayer client-side command argument errors

## [5.3.0+1.20.1] - 2023.09.04
### Added
- [API] Added `CuriosApi#registerCurio` method for more modular curio definitions
- [API] Added `CuriosApi#createCurioProvider` method for more convenient Curios capability attachments
- Added support for `"conditions"` in slot data and entity slot data in datapacks
### Fixed
- Fixed item insertions handled directly through item handlers not being validated [#238](https://github.com/TheIllusiveC4/Curios/issues/238)
- Fixed backwards compatibility with mods using `top.theillusivec4.curios.common.CuriosHelper.SlotAttributeWrapper`

## [5.2.0+1.20.1] - 2023.08.31
### Added
- Added `keepCurios` configuration option to `curios-server.toml` [#181](https://github.com/TheIllusiveC4/Curios/issues/181)
- Added inventory searching methods from `ICuriosHelper` to `ICuriosItemHandler`
- Re-added JEI integration
### Changed
- Updated `uk_ua` localization (thanks unroman!) [#320](https://github.com/TheIllusiveC4/Curios/pull/320)
- Updated slot tooltip to use `"curios.tooltip.slot": "Slot:"` in localization files [#329](https://github.com/TheIllusiveC4/Curios/issues/329)
### Deprecated
- Deprecated `ICuriosHelper`, `ISlotHelper`, and `IIconHelper`, to be removed in Minecraft 1.22. Check javadocs for
  replacement functionality and methods.
- Marked previous deprecations for removal in Minecraft 1.21.
### Fixed
- Fixed slots not being recognized in server-side command arguments [#327](https://github.com/TheIllusiveC4/Curios/issues/327)
- Fixed equip from use behavior to properly validate unequip behavior first [#332](https://github.com/TheIllusiveC4/Curios/issues/332)
- Fixed `CuriosEquipEvent` firing erroneously [#305](https://github.com/TheIllusiveC4/Curios/issues/305)
- Fixed item insertions handled directly through item handlers not being validated [#238](https://github.com/TheIllusiveC4/Curios/issues/238)
- Fixed status effects not rendering in the Curios GUI [#95](https://github.com/TheIllusiveC4/Curios/issues/95)

## [5.2.0-beta.3+1.20.1] - 2023.06.12
### Changed
- Updated to Minecraft 1.20.1
### Fixed
- Fixed crash upon loading world with a mod that uses legacy IMC slot registration [#314](https://github.com/TheIllusiveC4/Curios/issues/314)

## [5.2.0-beta.2+1.20] - 2023.06.11
### Fixed
- Fixed log spam [#312](https://github.com/TheIllusiveC4/Curios/issues/312)

## [5.2.0-beta.1+1.20] - 2023.06.11
### Added
- Added new slot and entity registration through datapacks, see [Curios Documentation](https://docs.illusivesoulworks.com/category/curios) for more information
### Changed
- Updated to Minecraft 1.20
- Curios capabilities have been extended natively to all `LivingEntity` entity types
### Deprecated
- Deprecated `SlotTypeMessage` and `SlotTypePreset`, registration through datapacks is now recommended over IMC
### Removed
- Removed Curios server configuration file

## [1.19.4-5.1.5.3] - 2023.05.05
### Fixed
- Fixed default curio slot texture not showing properly

## [1.19.4-5.1.5.2] - 2023.04.28
### Changed
- Updated `ru_ru` localization (thanks Heimdallr-1!) [#302](https://github.com/TheIllusiveC4/Curios/pull/302)

## [1.19.4-5.1.5.1] - 2023.04.20
### Changed
- Curios swapping will now take into account all valid slots instead of just the first valid slot
### Fixed
- Fixed duplication bug when equipping from use in the hotbar

## [1.19.4-5.1.5.0] - 2023.04.16
### Added
- Added `CurioAttributeModifierEvent` for editing curio attribute modifiers in slots [#292](https://github.com/TheIllusiveC4/Curios/issues/292)
### Changed
- Curios that can be equipped from use in the hotbar will now swap with existing curios in valid slots [#301](https://github.com/TheIllusiveC4/Curios/issues/301)

## [1.19.4-5.1.4.3] - 2023.04.06
### Fixed
- Fixed toggle visibility buttons causing menu issues when clicked [#296](https://github.com/TheIllusiveC4/Curios/issues/296)

## [1.19.4-5.1.4.2] - 2023.03.16
### Changed
- Updated to Minecraft 1.19.4
- Updated slot textures to match new Minecraft slot textures

## [1.19.3-5.1.4.1] - 2023.03.15
### Added
- Added `vi_vn` localization (thanks ZzThanhBaozZ!) [#289](https://github.com/TheIllusiveC4/Curios/pull/289)
### Changed
- Updated `it_it` localization (thanks WVam!) [#288](https://github.com/TheIllusiveC4/Curios/pull/288)
### Fixed
- Fixed crashes when slots get resized to negative amounts dynamically

## [1.19.3-5.1.4.0] - 2023.02.27
### Added
- Added `/curios drop` command for dropping curio items
### Changed
- Updated `zh_cn` localization (thanks WadjetSama!) [#285](https://github.com/TheIllusiveC4/Curios/issues/285)
- Updated `it_it` localization (thanks WVam!) [#284](https://github.com/TheIllusiveC4/Curios/pull/284)
### Fixed
- Fixed curio entity selectors counting slots with size 0 erroneously

## [1.19.3-5.1.3.1] - 2023.02.15
### Fixed
- Fixed `SlotModifiersUpdatedEvent` not always firing

## [1.19.3-5.1.3.0] - 2023.02.13
### Added
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

## [1.19.3-5.1.2.0] - 2023.01.09
### Added
- Added new `ICurio#canWalkOnPowderedSnow` method for curio implementations that allow walking on Powdered Snow blocks
  (thanks bconlon!) [#273](https://github.com/TheIllusiveC4/Curios/pull/273)

## [1.19.3-5.1.1.2] - 2022.12.08
### Fixed
- Fix version requirements for Minecraft and Forge

## [1.19.3-5.1.1.1] - 2022.12.08
### Changed
- Updated to Minecraft 1.19.3
- Updated to Forge 44.0.0+
- Updated Portuguest (pt_br) localization (thanks FITFC!) [#265](https://github.com/TheIllusiveC4/Curios/pull/265)
### Removed
- Removed JEI integration temporarily until the mod is ported to 1.19.3

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
