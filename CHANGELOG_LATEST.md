The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres
to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history,
go [here](https://github.com/TheIllusiveC4/Curios/blob/26.x/CHANGELOG.md).

## [17.0.0-beta+26.3] - 2026.09.21
### Changed
- [API] `ICuriosItemHandler#getEquippedCurios` now returns a `ResourceHandler<ItemResource>` instead of the now-removed `IItemHandlerModifiable`
- [API] `IDynamicStackHandler` extends `ResourceHandler<ItemResource>` instead of the now-removed `IItemHandlerModifiable`. The
  removed inherited methods are still declared in `IDynamicStackHandler`, so consumers won't be broken, but are deprecated
  for future removal.
- Updated to Minecraft 26.3

### Removed
- [API] Removed APIs that were deprecated and due for removal. See the full version history for the complete list.
