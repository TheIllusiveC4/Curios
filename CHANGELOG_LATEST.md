The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres
to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history,
go [here](https://github.com/TheIllusiveC4/Curios/blob/26.x/CHANGELOG.md).

## [17.0.0+26.3] - 2026.11.03
### Added
- Added `ca_es` localization (thanks Nimlise!) [#653](https://github.com/TheIllusiveC4/Curios/pull/653)
### Changed
- Migrated Fortune and Looting hooks to NeoForge API events
- Updated required NeoForge to >=26.3.0.97
### Fixed
- Fixed slot modifiers failing to persist on world/data reload
- Fixed `/reload` command failing to populate slot data [#659](https://github.com/TheIllusiveC4/Curios/issues/659)
- Fixed transient ItemStack desyncs causing client disconnects (thanks lynnpye!) [#643](https://github.com/TheIllusiveC4/Curios/pull/643)
