The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/CHANGELOG.md).

## [9.6.0+1.21.1] - 2026.11.18
### Changed
- Updated `pt_br` localization (thanks PrincessStellar!) [#549](https://github.com/TheIllusiveC4/Curios/pull/549)
- Quick-moving an item in the Curios screen will now prioritize specific slots over generic `curio` slots (thanks zBinFinn!) [#588](https://github.com/TheIllusiveC4/Curios/pull/588)
- Migrated Fortune and Looting hooks to NeoForge API events
- Updated required NeoForge to >=21.1.265
### Fixed
- Fixed curio entity parsing for datapacks (thanks mattan42!) [#657](https://github.com/TheIllusiveC4/Curios/issues/625)
- Fixed tooltip rendering on items in the Curios screen (thanks TorranTR!) [#625](https://github.com/TheIllusiveC4/Curios/issues/625)
- Fixed transient ItemStack desyncs causing client disconnects (thanks lynnpye!) [#643](https://github.com/TheIllusiveC4/Curios/pull/643)
