The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.21.1/CHANGELOG.md).

## [9.2.0+1.21.1] - 2025.01.10
### Added
- [API] Added the following methods:
    - `ICurio#getAttributesTooltip(List<Component>, Item.TooltipContext)`
    - `ICurio#getDropRule(SlotContext, DamageSource, boolean)`
    - `ICurio#getAttributeModifiers(SlotContext, ResourceLocation)`
    - `ICurio#getSlotsTooltip(List<Component>, Item.TooltipContext)`
### Fixed
- Addressed memory leaks due to unneeded capability invalidation
### Changed
- Updated to Minecraft 1.21.1
- [API] Changed `ICurio#getLootingLevel(SlotContext, DamageSource, LivingEntity, int)` to `getLootingLevel(SlotContext, LootContext)`
- [API] Replaced `UUID` for attribute modifiers with `ResourceLocation` in all instances of the API
- Changed tooltip processing to use NeoForge's API
- Added caching to Curios lookups for optimization
### Deprecated
- [API] Deprecated the following methods:
    - `ICurio#getAttributesTooltip(List<Component>)`
    - `ICurio#getDropRule(SlotContext, DamageSource, int, boolean)`
    - `ICurio#getAttributeModifiers(SlotContext, UUID)`
    - And their respective pairings in `ICurioItem`
    - `CuriosApi#getSlotIcon(String)`
    - `CurioCanEquipEvent#(ItemStack, SlotContext)`
### Removed
- [API] Removed previously deprecated methods from `ICurio`, `ICurioItem`, `SlotContext`
