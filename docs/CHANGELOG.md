# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

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
    