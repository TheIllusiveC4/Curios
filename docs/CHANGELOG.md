# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project does not adhere to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
This project uses MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH.

## [0.12] - 2019.06.15
### Changed
- Updated to last Forge and mappings for 1.13.2

## [0.11] - 2019.04.28
### Added
- Slot icons for commonly used terms for potential slots

## [0.10] - 2019.04.11
### Added
- [API] Curio item tag dictionary is available as a holder class to provide commonly used terms for potential slots
### Changed
- [API] Major API changes to streamline methods and emphasize concurrent determinism so that the registry will always output the same results.
    - Slot registry converted to IMC process
    - Icon registry isolated to client-side
    - Some CuriosRegistry methods moved to CuriousAPI so that the latter contains all methods intended for third-party use
- [API] Debug "Amulet" slot changed to "Necklace" slot

## [0.9] - 2019.03.20
### Fixed
- Fixed mods.toml so that URL and Authors fields show up correctly in the mod menu
- Fixed right-click equip syncing
- Fixed new icons not being recognized by old slots

## [0.8] - 2019.03.17
### Added
- Reobfuscation publishing
- API and sources jars

### Changed
- Updated mod icon

## [0.7] - 2019.03.12
### Changed
- Attribute tooltips for curios have been slightly modified to match vanilla semantics and are now gold-colored

### Fixed
- [API] Fix possible thread safety issues [#1](https://github.com/TheIllusiveC4/Curios/issues/1)

## [0.6] - 2019.03.07
### Changed
- [API] CuriosAPI#getCurioEquipped methods now return data about the ItemStack as well

## [0.5] - 2019.03.07
### Fixed
- Fixed some syncing issues

## [0.4] - 2019.03.07
### Added
- [API] Added filtered CuriosAPI#getCurioEquipped method

### Changed
- Now able to access curios in Creative mode

## [0.3] - 2019.03.05
### Added
- [API] Added ICurio#getSyncTag and ICurio#readSyncTag(NBTTagCompound) for additional data syncing
- [API] Re-added ICurio#onEquipped(String, EntityLivingBase) and ICurio#onUnequipped(String, EntityLivingBase) methods
- [API] Added RenderHelper for holding helpful rendering utility methods
- [API] Change return of some CurioRegistry methods to be immutable to prevent access to certain registry data
- Added crown item

### Changed
- [API] Changed CurioAPI#getCurioEquippedOfType(String, Item, EntityLivingBase) to a more robust CurioAPI#getCurioEquipped(Item, EntityLivingBase) that returns more data about the found stack
- Modified speed bonus on ring item

### Fixed
- Fixed inverted tracking sync checks
- Fixed dedicated server crashes

## [0.2] - 2019.02.28
### Added
- [API] Added ICurio#playEquipSound(EntityLivingBase) method

### Removed
- [API] Removed ICurio#onEquipped(String, EntityLivingBase) and ICurio#onUnequipped(String, EntityLivingBase) methods

## [0.1] - 2019.02.24
Initial beta release