The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
Prior to version 5.2.0, this projected used [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.x/docs/CHANGELOG.md).

## [5.3.0+1.20.1] - 2023.09.04
### Added
- [API] Added `CuriosApi#registerCurio` method for more modular curio definitions
- [API] Added `CuriosApi#createCurioProvider` method for more convenient Curios capability attachments
- Added support for `"conditions"` in slot data and entity slot data in datapacks
### Fixed
- Fixed item insertions handled directly through item handlers not being validated [#238](https://github.com/TheIllusiveC4/Curios/issues/238)
- Fixed backwards compatibility with mods using `top.theillusivec4.curios.common.CuriosHelper.SlotAttributeWrapper`
