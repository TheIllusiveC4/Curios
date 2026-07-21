The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres
to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history,
go [here](https://github.com/TheIllusiveC4/Curios/blob/26.x/CHANGELOG.md).

## [16.0.0+26.2] - 2026.07.20

### Changed
- [API] `top.theillusivec4.curios.api.event.DropRulesEvent#getOverrides()` refactored `net.minecraft.util.Tuple`
  return type to `com.mojang.datafixers.util.Pair`
- Cleaned up mixin debug logs
- Updated to Minecraft 26.2

### Removed
- [API] Removed deprecated methods in `top.theillusivec4.curios.api.client.ICurioRenderer` that took a
  `MultiBufferSource` parameter:
  - `render` (use the `SubmitNodeCollector` replacement method)
  - `renderModel`
