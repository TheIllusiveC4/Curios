The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.18.x/docs/CHANGELOG.md).

## [1.18.2-5.0.8.0] - 2023.02.15
### Added
- Added new `ICurio#canWalkOnPowderedSnow` method for curio implementations that allow walking on Powdered Snow blocks
  (thanks bconlon!) [#273](https://github.com/TheIllusiveC4/Curios/pull/273)
- Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers [#276](https://github.com/TheIllusiveC4/Curios/issues/276)
- Added two commands:
    - `/curios replace <slot> <index> <player> with <item> [count]` for setting items to curio slots
    - `/curios list` for listing all registered slots and which mods they come from [#261](https://github.com/TheIllusiveC4/Curios/issues/261)
- Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and getting
  items from curio slots respectively
