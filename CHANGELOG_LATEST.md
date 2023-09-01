The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
Prior to version 5.2.0, this projected used [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.x/docs/CHANGELOG.md).

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