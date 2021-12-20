# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [1.17.1-5.0.2.6] - 2021.12.19
### Fixed
- Fixed slot amount changes not persisting when applicable
- Fixed slot amount changes causing loading errors [#195](https://github.com/TheIllusiveC4/Curios/issues/195)

## [1.17.1-5.0.2.5] - 2021.12.15
### Changed
- [API] `ICurio#canEquipFromUse` and `ICurio#onEquipFromUse` methods are now called both client-side and server-side
  (previously server-side only)
### Fixed
- Fixed curio items not calling their `use` logic client-side [#192](https://github.com/TheIllusiveC4/Curios/issues/192)

## [1.17.1-5.0.2.4] - 2021.12.05
### Fixed
- Fixed crash with client-side player entities [#189](https://github.com/TheIllusiveC4/Curios/issues/189)

## [1.17.1-5.0.2.3] - 2021.12.02
### Fixed
- Fixed crash when running data generation in development environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.17.1-5.0.2.2] - 2021.12.02
### Fixed
- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)
- Fixed potential crash on dedicated servers [#184](https://github.com/TheIllusiveC4/Curios/issues/184)

## [1.17.1-5.0.2.1] - 2021.11.30
### Fixed
- Fixed resource loading crashing when invalid texture files are found [#183](https://github.com/TheIllusiveC4/Curios/issues/183)

## [1.17.1-5.0.2.0] - 2021.11.30
Please note that this update is more experimental than most and is marked as a beta. Be cautious about adding this to
stable worlds.
### Added
- [API] Added slot modifier system, see [the wiki page](https://github.com/TheIllusiveC4/Curios/wiki/Slot-Modifiers) for
  more info. This is the main reason the update is experimental. Although this system was designed to be backwards
  compatible, be wary of issues with older slot modification methods. [#178](https://github.com/TheIllusiveC4/Curios/issues/178)
- [API] Added `CurioEquipEvent` and `CurioUnequipEvent` to allow modders the ability to intercept and change
  equip/unequip results [#174](https://github.com/TheIllusiveC4/Curios/issues/174)
- [API] Added new slot texture registration method: textures located in the `assets/curios/textures/slot` directory in
  any mod or resource pack will be automatically stitched to the texture atlas and usable by slots [#145](https://github.com/TheIllusiveC4/Curios/issues/145)
- [API] Added `ICuriosItemHandler#saveInventory` and `ICuriosItemHandler#loadInventory` for saving/loading the Curios
  inventory more conveniently [#164](https://github.com/TheIllusiveC4/Curios/issues/164)
### Fixed
- Fixed syncing slot shrinking client-side [#179](https://github.com/TheIllusiveC4/Curios/issues/179)

## [1.17.1-5.0.1.0] - 2021.09.11
### Added
- Added Polish translation (thanks Greg-21!) [#163](https://github.com/TheIllusiveC4/Curios/pull/163)
- Re-added Just Enough Items integration
### Changed
- Updated to Forge 37.0.42+
### Fixed
- Fixed slots desyncing when shrinking/growing amounts

## [1.17.1-5.0.0.1] - 2021.08.04
### Fixed
- Fixed NPE when using cosmetic slots [#157](https://github.com/TheIllusiveC4/Curios/issues/157)

## [1.17.1-5.0.0.0] - 2021.07.27
### Added
- Added `getStack` method to `ICurio`
- Added slot context-sensitive alternatives to methods in `ICurio` and `ICurioItem`
- Added new rendering system, see the [GitHub wiki](https://github.com/TheIllusiveC4/Curios/wiki/1.16.5-to-1.17:-Updates-and-Changes#rendering-system) for more details
### Changed
- Updated to Minecraft 1.17.1
### Deprecated
- Deprecated methods in `ICurio` and `ICurioItem` without slot contexts
- Deprecated locking and unlocking slot states
### Removed
- Removed `render` and `canRender` from `ICurio` and `ICurioItem`
- Removed ring, amulet, crown, and knuckles items
    