The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres
to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history,
go [here](https://github.com/TheIllusiveC4/Curios/blob/26.x/CHANGELOG.md).

## [15.0.0+26.1.2] - 2026.07.19

### Added
- [API] Added `SlotResult#getItemAccess` and `SlotContext#getItemAccess` as helper methods
  to access curios storage
- [API] Added `ICurioSlot` interface to expose slot methods and fields on menus

### Fixed
- Fixed `/data` commands errors when using curio slots [#617](https://github.com/TheIllusiveC4/Curios/issues/617)
- Fixed resource path crash from system locale [#611](https://github.com/TheIllusiveC4/Curios/issues/611)
- Fixed commands not syncing or working as expected
- Fixed cascading slot modifier crash [#609](https://github.com/TheIllusiveC4/Curios/issues/609)
- Fixed attribute modifier loading in single-player [#614](https://github.com/TheIllusiveC4/Curios/issues/614)
