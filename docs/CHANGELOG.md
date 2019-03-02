# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project does not adhere to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
This project uses MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH.

## [0.3] - 2019.03.02
### Added
- [API] Added ICurio#getSyncTag and ICurio#readSyncTag(NBTTagCompound) for additional data syncing

### Changed
- [API] Changed CurioAPI#getCurioEquippedOfType(String, Item, EntityLivingBase) to a more robust CurioAPI#getCurioEquipped(Item, EntityLivingBase) that returns more data about the found stack

### Fixed
- Fixed inverted tracking sync checks

## [0.2] - 2019.02.28
### Added
- [API] Added ICurio#playEquipSound(EntityLivingBase) method

### Removed
- [API] Removed ICurio#onEquipped(String, EntityLivingBase) and ICurio#onUnequipped(String, EntityLivingBase) methods

## [0.1] - 2019.02.24
Initial beta release