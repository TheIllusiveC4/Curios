The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.3/CHANGELOG.md).

## [7.0.0-beta+1.20.3] - 2023.12.05
### Changed
- Updated to Minecraft 1.20.3
- [NeoForge] Reworked curios capabilities to work with revamped capability system
  - Capabilities can be found in `top.theillusivec4.curios.api.CuriosCapability`
  - `LazyOptional` fields converted to regular `Optional` fields
  - Removed `CuriosApi#createProvider`
### Removed
- Removed `"location"` from `"curios:equip_curio"` triggers
