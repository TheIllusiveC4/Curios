The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.19.3/docs/CHANGELOG.md).

## [1.19.3-5.1.3.0] - 2023.02.13
### Added
- Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers [#276](https://github.com/TheIllusiveC4/Curios/issues/276)
- Added two commands:
  - `/curios replace <slot> <index> <player> with <item> [count]` for setting items to curio slots
  - `/curios list` for listing all registered slots and which mods they come from [#261](https://github.com/TheIllusiveC4/Curios/issues/261)
- Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and getting
  items from curio slots respectively
### Changed
- `ICurio#onEquipFromUse` now fires when items are placed into slots in inventory screens
### Fixed
- Fixed equip sounds not playing properly in the Curios screen (thanks bconlon!) [#281](https://github.com/TheIllusiveC4/Curios/pull/281)
