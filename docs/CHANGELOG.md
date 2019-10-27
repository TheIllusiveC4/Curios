# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project does not adhere to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
This project uses MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH.

## [1.0.5] - 2019.10.23
### Added
- [API] Added LivingCurioDropsEvent, fired inside Curio's LivingDropsEvent handler and allowing modders to edit the list of dropped curios added to the overall drops list.
### Changed
- Updated Russian localization (thanks Extegral!) [#27](https://github.com/TheIllusiveC4/Curios/pull/27)
### Fixed
- Fixed scroll wheel not working in the Curios GUI for slot list

## [1.0.4.1] - 2019.10.22
### Fixed
- Fixed UnsupportedOperationException crashes

## [1.0.4] - 2019.10.20
### Added
- [API] Added CuriosAPI#setSlotsForType helper method to set slot sizes for a given entity and curio type identifier [#26](https://github.com/TheIllusiveC4/Curios/pull/26)
- [API] Added built-in support for "hands" curio type
- [API] Added ICurio.RenderHelper#followBodyRotations to rotate models according to entity pose
- Added new test item for "hands", Curious Knuckles
### Changed
- Changed tooltip for curio tags on items [#26](https://github.com/TheIllusiveC4/Curios/pull/26)

## [1.0.3] - 2019.10.13
### Added
- [API] Added CuriosAPI#getSlotsForType helper method to retrieve slot sizes for a given entity and curio type identifier

## [1.0.2.1] - 2019.10.12
### Changed
- Updated Russian localization (thanks Extegral!) [#24](https://github.com/TheIllusiveC4/Curios/pull/24)
### Fixed
- Fixed attribute modifiers not being applied when respawning with keepInventory gamerule set to true [#23](https://github.com/TheIllusiveC4/Curios/issues/23)

## [1.0.2] - 2019.10.06
### Added
- [API] Added generic "curio" tag for items that can go into any curio slot

## [1.0.1] - 2019.10.06
### Added
- [API] Added ICurio#getTagsTooltip to allow modders to customize curio tags tooltip information
- Added size alteration to createCurios config option by appending a semicolon and a size amount
### Fixed
- Fixed curio attribute modifiers not respecting HideFlags

## [1.0.0.3] - 2019.09.24
### Added
- Added Russian localization (thanks Extegral!) [#21](https://github.com/TheIllusiveC4/Curios/pull/21)

## [1.0.0.2] - 2019.09.23
### Fixed
- Fixed localization of Curios modifier tooltips, each one will now require an explicit key for each identifier [#19](https://github.com/TheIllusiveC4/Curios/issues/19)

## [1.0.0.1] - 2019.09.21
### Fixed
- Fixed item duplication exploit when right-click equipping Curios
- Fixed creative GUI behavior that caused shift-right-clicking to unintentionally destroy all Curios [#17](https://github.com/TheIllusiveC4/Curios/issues/17)

## [1.0] - 2019.09.13
### Added
- Added Curios button to the Creative inventory screen
### Changed
- [API] Re-formatted and updated some javadocs
- Updated to Forge RB 1.14.4-28.1.0
### Fixed
- Fixed client desyncing with items in curio slots [#15](https://github.com/TheIllusiveC4/Curios/issues/14)

## [0.25] - 2019.09.08
### Fixed
- Fixed sneak transformations on curio renders when in creative flight [#14](https://github.com/TheIllusiveC4/Curios/issues/14)

## [0.24] - 2019.08.22
### Fixed
- Attempt #2 to fix startup crashes [#12](https://github.com/TheIllusiveC4/Curios/issues/12)

## [0.23] - 2019.08.17
### Added
- Shift-clicking the delete item slot in the creative GUI will now clear all 
curios as well as the inventory
### Fixed
- Attempted to fix crashes on startup related to networking errors [#12](https://github.com/TheIllusiveC4/Curios/issues/12)

## [0.22] - 2019.08.11
### Changed
- [API] Refactored API to remove references to main Curios mod
- Moved Curio type tooltip to right below the item's display name
### Fixed
- Fixed Curio items disappearing when returning from the End [#11](https://github.com/TheIllusiveC4/Curios/issues/11)

## [0.21] - 2019.08.05
### Changed
- Updated Forge version to 28.0.45 to accommodate for a breaking change

## [0.20] - 2019.08.03
### Changed
- [API] Added call to ICurio#onCurioTick(String, LivingEntity) from 
ICurio#onCurioTick(String, int, LivingEntity) for backwards compatibility

## [0.19] - 2019.08.03
### Added
- [API] ICurio#onCurioBreak and CuriosAPI#onBrokenCurio for implementing 
break animations for curios in curio slots, defaulting to vanilla behavior 
but allowing for overrides as well
- [API] Added ICurio#onCurioTick(String, int, LivingEntity) for 
index-sensitive tick calls
### Changed
- [API] onUnequipped and onEquipped methods now fire only when items are 
different and ignore durability
- [API] ICurio#onCurioTick(String, LivingEntity) has been deprecated in favor
 of the index-sensitive version
### Fixed
- Fixed bug with Curios stacks not being cleaned up in the slots

## [0.18] - 2019.07.24
### Changed
- Updated to 1.14.4 Forge
### Fixed
- Fixed player twitching when switching to/from Curios inventory

## [0.17] - 2019.07.13
### Fixed
- Fixed items being duplicated when attempting to shift-click from the Curios
 GUI
- Fixed Curios items not being able to be shift-clicked into an appropriate 
slot without capabilities

## [0.16] - 2019.07.11
### Fixed
- Fixed Curios items disappearing when returning from the End or using gamerule keepInventory on death
- Fixed Curious Crown night vision desyncing with client on login

## [0.15] - 2019.07.08
### Fixed
- Fixed Curios GUI sometimes being colored purple due to an enchanted item being rendered

## [0.14] - 2019.06.30
### Changed
- [API] Removed fallback for missing identifier lang entries, so modders and users need to define these explicitly if they're not provided internally by Curios
### Fixed
- Fixed missing identifier lang entries for some common Curio tags

## [0.13] - 2019.06.28
### Changed
- Ported to 1.14.3 Forge
- [API] CuriosAPI#getType now returns an Optional value
- [API] CuriosAPI#getCurioEquipped methods each now return an Optional ImmutableTriple
- [API] Moved IMC processing out of the API
### Removed
- [API] CuriosAPI#registerIcon has been removed and replaced with IMC processing
- [API] CuriosAPI#getIcons has been removed and replaced with CuriosAPI#getIcon(String)
- [API] CuriosAPI.FinderData class and usages have been removed and replaced with ImmutableTriple

## [0.12] - 2019.06.08
### Changed
- Updated to last 1.13.2 Forge and mappings

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