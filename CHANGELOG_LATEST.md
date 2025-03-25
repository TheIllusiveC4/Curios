The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/CHANGELOG.md).

## [9.4.0+1.21.1] - 2025.03.24
### Changed
- Updated tooltip logic to follow NeoForge conventions, including the use of `AddAttributeTooltipsEvent` and `GatherSkippedAttributeTooltipsEvent` [#496](https://github.com/TheIllusiveC4/Curios/issues/496)
### Fixed
- Fixed previous slot modifiers persisting after deserialization which caused inconsistent behavior when using extra slots
- Fixed caching errors that caused certain functions to misidentify curio inventory contents
- Fixed deprecated usage of `ICurio#getDropRule`
- Fixed datagen output not being deterministic [#497](https://github.com/TheIllusiveC4/Curios/issues/497)
- Fixed missing slot localization fallbacks on item tooltips
- Fixed slot modifier tooltip localizations
- Fixed `curios:set_curio_attributes` loot table function
