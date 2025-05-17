# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres
to [Semantic Versioning](http://semver.org/spec/v2.0.0.html). Prior to version 5.2.0, this projected
used [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [11.0.1+1.21.5] - 2025.05.17

### Fixed

- Fixed loot context crash

## [11.0.0+1.21.5] - 2025.05.15

### Changed
- 
- Updated to Minecraft 1.21.5

## [10.0.0+1.21.4] - 2025.05.15

For an in-depth overview of API changes, refer to [this](https://docs.illusivesoulworks.com/curios/porting/1211to1214)
guide.

### Added

- Added ways for developers to listen to state changes (when an item is the same) and differentiate them from item changes:
  - `CurioChangeEvent` is now abstract, with added `CurioChangeEvent.Item` and `CurioChangeEvent.State` subclasses
  - `onStateChange` method added to `ICurio` and `ICurioItem`
- A new preset slot type, `feet`, has been added with an included default icon
- Added slot type predicates for more advanced checking in `CurioAttributeModifier`
- Added new data generation options, including the ability to generate tags within the same provider as the Curios data
- Added `curios:player_like` entity type tag, with entries for players and armor stands
- Added `entities` field to slot type data files for marking default entity types to add the slot type to automatically,
without needing a corresponding `curios/entities` file
- Added the ability to create slots directly inside `curios/entities` data files, without needing a corresponding
`curios/slots` file
- Added `curios:generic_exclusions` item tag for declaring curio items that should not be equipable into the generic
`curio` slot by default
- Added preset slot type constants and tags to `CuriosSlotTypes` and `CuriosTags` respectively

### Changed

- Updated to Minecraft 1.21.4
- Following the state change additions, `onUnequip` and `onEquip` no longer fire if the detected items are the same
- `ICurioRenderer#render` parameters have been adjusted for Minecraft changes
- `ICurio.DropRule` has moved to `top.theillusivec4.curios.api.common.DropRule`
- `CurioAttributeModifierEvent` now uses `CurioAttributeModifiers` instead of maps
- Moved `SetCurioAttributesFunction` to the official API package

### Removed

- Removed all previously deprecated classes and methods

### Deprecated

- Deprecated slot type methods in `CuriosApi`, replaced by methods in `CuriosSlotTypes`.
- Deprecated `getAttributeModifiers` in `ICurio` and `ICurioItem`
- Deprecated `CuriosRendererRegistry`, replaced by methods in `ICurioRenderer`
- Deprecated the previous utility methods in `ICurioRenderer`, replaced by `setupHumanoidAnimations` and
  `copyHumanoidProperties`
- Deprecated attribute map methods in `CurioAttributeModifierEvent`
- Deprecated `CuriosTooltip`
- Deprecated `ISlotType#getIdentifier`, replaced by `getId`
- Deprecated `ICurioSlotExtension#getSlotTooltip`, replaced by `getSlotTooltip` with an additional parameter for
  original tooltip lines

## [9.5.1+1.21.1] - 2025.05.14

### Fixed

- Fixed crash on world load after updating from `9.4.2+1.21.1` or
  below [#520](https://github.com/TheIllusiveC4/Curios/issues/520)

## [9.5.0+1.21.1] - 2025.05.14

### Added

- [API] Added active states for slots to allow developers to enable or disable slots on entities during gameplay,
  accessible through new methods `ICuriosItemHandler#isSlotActive` and `ICuriosItemHandler#setSlotActive`

### Changed

- Updated `ja_jp` localization [#513](https://github.com/TheIllusiveC4/Curios/pull/513)

## [9.4.2+1.21.1] - 2025.04.08

### Changed

- `Inventory#contains(Predicate<ItemStack>)` and `Inventory#hasAnyMatching(Predicate<ItemStack>)` now also check the
  curios inventory

## [9.4.1+1.21.1] - 2025.04.04

### Fixed

- Fixed missing inventory when a player fails to join a
  server [#481](https://github.com/TheIllusiveC4/Curios/issues/481)
- Fixed Curios panel rendering beneath EMI themed panels [#503](https://github.com/TheIllusiveC4/Curios/issues/503)

## [9.4.0+1.21.1] - 2025.03.24

### Changed

- Updated tooltip logic to follow NeoForge conventions, including the use of `AddAttributeTooltipsEvent` and
  `GatherSkippedAttributeTooltipsEvent` [#496](https://github.com/TheIllusiveC4/Curios/issues/496)

### Fixed

- Fixed previous slot modifiers persisting after deserialization which caused inconsistent behavior when using extra
  slots
- Fixed caching errors that caused certain functions to misidentify curio inventory contents
- Fixed deprecated usage of `ICurio#getDropRule`
- Fixed datagen output not being deterministic [#497](https://github.com/TheIllusiveC4/Curios/issues/497)
- Fixed missing slot localization fallbacks on item tooltips
- Fixed slot modifier tooltip localizations
- Fixed `curios:set_curio_attributes` loot table function

## [9.3.1+1.21.1] - 2025.03.12

### Fixed

- Fixed slot tooltip rendering

## [9.3.0+1.21.1] - 2025.03.10

### Added

- Added `ICurioSlotExtension` for defining additional slot behavior
- Added `CuriosTags` for more convenient access to commonly used Curios
  tags [#495](https://github.com/TheIllusiveC4/Curios/pull/495)

## [9.2.3+1.21.1] - 2025.03.07

### Added

- Added `lzh` localization [#488](https://github.com/TheIllusiveC4/Curios/issues/488)

### Changed

- Added `tr_tr` localization [#482](https://github.com/TheIllusiveC4/Curios/pull/482)

### Fixed

- Fixed slot modifier collisions causing desyncs when using the Curios
  inventory [#479](https://github.com/TheIllusiveC4/Curios/issues/479)
- Fixed `ISlotData#operation` to use the correct `String` argument instead of
  `AttributeModifier.Operation` [#484](https://github.com/TheIllusiveC4/Curios/issues/484)
- Fixed tooltip ordering when using `CurioAttributeModifierEvent`

## [9.2.2+1.21.1] - 2025.01.11

### Added

- Added exclusion areas on the Curios screen for EMI and REI recipe viewers

## [9.2.1+1.21.1] - 2025.01.10

### Fixed

- Fixed invalid mixin target

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
- [API] Changed `ICurio#getLootingLevel(SlotContext, DamageSource, LivingEntity, int)` to
  `getLootingLevel(SlotContext, LootContext)`
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

## [8.1.0+1.20.6] - 2024.10.23

### Added

- Added `from` expansion additions to the `/curios replace`
  command [#450](https://github.com/TheIllusiveC4/Curios/issues/450)

### Changed

- Slot types that use the `curios:all` validator will no longer be listed on item tooltips
- Updated `pl_pl` localization [#441](https://github.com/TheIllusiveC4/Curios/pull/441)

### Fixed

- Fixed cosmetic toggles not updating positions properly

## [8.0.2+1.20.6] - 2024.08.31

### Fixed

- [NeoForge] Fixed erroneous data resources being added
- [NeoForge] Fixed potential recursive loop in Curios inventory
- Fixed slot resizing crash
- Fixed certain valid items being marked as invalid during loading and datapack reloading
- Fixed slot modifiers not being synced when the new inventory size is 0
- Fixed attribute modifier collisions

## [8.0.1+1.20.6] - 2024.06.18

### Changed

- Slot names without a localization will default to its identifier instead of its localization key

### Fixed

- [Forge] Fixed items disappearing from Curios inventory upon relogging when placed in slot indices after the
  first [#422](https://github.com/TheIllusiveC4/Curios/issues/422)

## [8.0.0+1.20.6] - 2024.06.13

### Added

- [API] Added `CuriosTooltip` helper class to build Curios-style tooltips
- Added data fixers for Curios inventory items [#411](https://github.com/TheIllusiveC4/Curios/pull/421)

## [8.0.0-beta.6+1.20.6] - 2024.06.09

### Fixed

- [NeoForge] Fixed block-breaking crash [#421](https://github.com/TheIllusiveC4/Curios/pull/421)

## [8.0.0-beta.5+1.20.6] - 2024.06.03

### Fixed

- Fixed API package references
- [Forge] Fixed `ICurioItem` items not being registered properly

## [8.0.0-beta.4+1.20.6] - 2024.05.27

### Changed

- [NeoForge] Refactored `CurioCanEquipEvent` and `CurioCanUnequipEvent` to use NeoForge's `TriState` enum as the
  result [#414](https://github.com/TheIllusiveC4/Curios/issues/414)

## [8.0.0-beta.3+1.20.6] - 2024.05.23

### Fixed

- Fixed crash with JEI integration [#412](https://github.com/TheIllusiveC4/Curios/issues/412)
- [NeoForge] Fixed non-player entities constructing without curios inventory data

## [8.0.0-beta.2+1.20.6] - 2024.05.16

### Fixed

- [NeoForge] Fixed client-side item syncing issue [#407](https://github.com/TheIllusiveC4/Curios/issues/407)

## [8.0.0-beta+1.20.6] - 2024.05.02

### Added

- [API] Added `CuriosApi#withSlotModifier` to generate `ItemAttributeModifiers` with a slot modifier attached

### Changed

- Changed the default interface to the experimental menu and removed the legacy menu
- [API] Changed `Attribute` to `Holder<Attribute>`, affecting the following:
    - `ICurio#getAttributeModifiers`
    - `ICurioItem#getAttributeModifiers`
    - `SlotAttribute#getOrCreate`
    - `CuriosApi#getAttributeModifiers`
    - `CuriosApi#addSlotModifier`
    - `CuriosApi#addModifier`
    - All modifier methods in `CurioAttributeModifierEvent`
- [API] Changed `CurioEquipEvent` and `CurioUnequipEvent` to `CurioCanEquipEvent` and `CurioCanUnequipEvent`
- [API] Added `HolderLookup.Provider` to the signatures of `IDynamicStackHandler#serializeNbt` and
  `IDynamicStackHandler#deserializeNbt`

### Removed

- Removed `addModifier`, `addSlotModifier`, and `getAttributeModifiers` methods from `ICuriosHelper`, use the methods in
  `CuriosApi` with the same name instead
- [Forge - API] Forge removed stack capabilities so curios can now only be registered through `CuriosApi#registerCurio`
  or implementing `ICurioItem` on the item
- [Forge - API] Removed `CuriosApi#createCurioProvider`

## [7.4.0+1.20.4] - 2024.04.29

### Added

- [API] Added `CuriosApi#getCurioPredicates`
- Added `tok` localization

### Changed

- New interface no longer shifts the screen to the right
- Scrolling through pages in the new interface is twice as fast
- Lowered the maximum value of `maxSlotsPerPage` configuration option from 64 to 48

### Fixed

- Fixed generic curio slots from failing validation checks when only those slots exist on an
  entity [#402](https://github.com/TheIllusiveC4/Curios/issues/402)

## [7.4.0-beta.2+1.20.4] - 2024.04.08

### Fixed

- Fixed potential backwards compatibility issue
- Fixed validators replacing instead of merging when defined

## [7.4.0-beta+1.20.4] - 2024.04.08

### Added

- Added a new opt-in user interface for the Curios screen, enable by setting "enableExperimentalMenu" to true in the
  curios-server.toml configuration file
- Added a configuration setting for configuring slots to the curios-common.toml configuration file
- Added "validators" as a field to the slot data files
- [API] Added the following methods to `ICuriosItemHandler`:
    - `isEquipped(Item)`
    - `isEquipped(Predicate<ItemStack>)`
- [API] Added the following methods to `CuriosApi`:
    - `getSlotUuid(SlotContext)`
    - `registerCurioPredicates(ResourceLocation, Predicate<SlotResult>)`
    - `getCurioPredicate(ResourceLocation)`
    - `testCurioPredicates(Set<ResourceLocation>, SlotResult)`

### Changed

- Slot types now exist client-side and are synced from the server
- Slot validations for item stacks are no longer tied solely to item tags and now follow the "validators" field added to
  the slot data files

### Deprecated

- Deprecated the following methods in `CuriosApi`, replaced by client and server-aware methods as listed in the
  javadocs:
    - `getSlot(String)`
    - `getSlotIcon(String)`
    - `getSlots()`
    - `getPlayerSlots()`
    - `getEntitySlots(EntityType<?>)`
    - `getItemStackSlots(ItemStack)`

## [7.3.4+1.20.4] - 2024.03.11

### Fixed

- Fixed tooltip crash [#388](https://github.com/TheIllusiveC4/Curios/issues/388)

## [7.3.3+1.20.4] - 2024.03.11

### Changed

- Non-curio equippable items (such as armor) now support slot modifiers
- Insertion order of curio attribute modifiers are preserved instead of randomized in tooltips

## [7.3.2+1.20.4] - 2024.02.27

### Fixed

- [Forge] Fixed crashes when calling Curios methods for powdered snow, Enderman visibility, and Piglin
  checks [#381](https://github.com/TheIllusiveC4/Curios/issues/381)

## [7.3.1+1.20.4] - 2024.02.16

### Fixed

- [NeoForge] Fixed mobs not spawning with assigned slots

## [7.3.0+1.20.4] - 2024.02.07

### Added

- [API] Added more slot modifier methods

### Fixed

- Fixed slots being assigned to entities erroneously
- [NeoForge] Fixed items being lost if a slot is removed before logging into a world

## [7.2.0+1.20.4] - 2024.01.29

### Added

- Added slot predicates to curio advancement equip triggers

## [7.1.0+1.20.4] - 2024.01.23

### Added

- [API] Added data generation helper methods and classes for generating entities/slots data files and adding curios
  triggers to advancements,
  see [documentation](https://docs.illusivesoulworks.com/curios/Developing%20with%20Curios/data-generation)
  for more information

### Changed

- [NeoForge] Updated to and requires NeoForge 20.4.117+ [#368](https://github.com/TheIllusiveC4/Curios/issues/368)

### Fixed

- [NeoForge] Fixed `curios:item_handler` capability returning an empty inventory
  client-side [#363](https://github.com/TheIllusiveC4/Curios/issues/363)

## [7.0.0+1.20.4] - 2023.12.26

### Fixed

- Fixed validity checks not working in some cases

## [7.0.0-beta.3+1.20.4] - 2023.12.15

### Added

- Added Forge version

### Changed

- NBT predicates and certain inventory checks will now include Curios items
  automatically [#357](https://github.com/TheIllusiveC4/Curios/issues/357)

### Fixed

- Fixed NPE crashes if a mod tries to get the Curios inventory from a `null`
  entity [#358](https://github.com/TheIllusiveC4/Curios/issues/358)
- [NeoForge] Fixed shearing not applying Fortune enchantment bonuses from curios

## [7.0.0-beta.2+1.20.4] - 2023.12.07

### Added

- Added `zh_tw` localization (thanks Lobster0228!) [#356](https://github.com/TheIllusiveC4/Curios/pull/356)

### Changed

- Updated to Minecraft 1.20.4

## [7.0.0-beta+1.20.3] - 2023.12.05

### Changed

- Updated to Minecraft 1.20.3
- [NeoForge] Reworked curios capabilities to work with revamped capability system
    - Capabilities can be found in `top.theillusivec4.curios.api.CuriosCapability`
    - `LazyOptional` fields converted to regular `Optional` fields
    - Removed `CuriosApi#createProvider`

## [6.1.0+1.20.2] - 2023.12.02

### Added

- Added NeoForge support

### Changed

- Curios inventory keybinding now also closes the current container to mimic the behavior of the vanilla inventory
  keybinding [#352](https://github.com/TheIllusiveC4/Curios/issues/352)

### Fixed

- Fixed slots failing to sync after world load [#347](https://github.com/TheIllusiveC4/Curios/issues/347)
- Fixed lost icons when reloading datapacks
- Fixed network decoding errors [#346](https://github.com/TheIllusiveC4/Curios/issues/346)
- Fixed `CurioAttributeModifierEvent` crashing when used for slot
  modifiers [#351](https://github.com/TheIllusiveC4/Curios/issues/351)

## [6.0.2+1.20.2] - 2023.10.25

### Changed

- Updated to and requires Forge 48.0.32 or above

### Fixed

- Fixed `list` command not outputting all possible data

## [6.0.1+1.20.2] - 2023.10.03

### Fixed

- Fixed `keepInventory` gamerule applying to non-player entities for curios slots

## [6.0.0+1.20.2] - 2023.09.26

### Changed

- Updated to Minecraft 1.20.2

## [5.3.4+1.20.1] - 2023.09.24

### Fixed

- Fixed items not showing their curio tooltips on clients connected to dedicated
  servers [#337](https://github.com/TheIllusiveC4/Curios/issues/337)

## [5.3.3+1.20.1] - 2023.09.21

### Fixed

- Fixed the `"replace"` field causing incorrect slot amounts during slot loading

## [5.3.2+1.20.1] - 2023.09.19

### Fixed

- Fixed slot loading on client-only entities

## [5.3.1+1.20.1] - 2023.09.04

### Fixed

- Fixed multiplayer client-side command argument errors

## [5.3.0+1.20.1] - 2023.09.04

### Added

- [API] Added `CuriosApi#registerCurio` method for more modular curio definitions
- [API] Added `CuriosApi#createCurioProvider` method for more convenient Curios capability attachments
- Added support for `"conditions"` in slot data and entity slot data in datapacks

### Fixed

- Fixed item insertions handled directly through item handlers not being
  validated [#238](https://github.com/TheIllusiveC4/Curios/issues/238)
- Fixed backwards compatibility with mods using `top.theillusivec4.curios.common.CuriosHelper.SlotAttributeWrapper`

## [5.2.0+1.20.1] - 2023.08.31

### Added

- Added `keepCurios` configuration option to
  `curios-server.toml` [#181](https://github.com/TheIllusiveC4/Curios/issues/181)
- Added inventory searching methods from `ICuriosHelper` to `ICuriosItemHandler`
- Re-added JEI integration

### Changed

- Updated `uk_ua` localization (thanks unroman!) [#320](https://github.com/TheIllusiveC4/Curios/pull/320)
- Updated slot tooltip to use `"curios.tooltip.slot": "Slot:"` in localization
  files [#329](https://github.com/TheIllusiveC4/Curios/issues/329)

### Deprecated

- Deprecated `ICuriosHelper`, `ISlotHelper`, and `IIconHelper`, to be removed in Minecraft 1.22. Check javadocs for
  replacement functionality and methods.
- Marked previous deprecations for removal in Minecraft 1.21.

### Fixed

- Fixed slots not being recognized in server-side command
  arguments [#327](https://github.com/TheIllusiveC4/Curios/issues/327)
- Fixed equip from use behavior to properly validate unequip behavior
  first [#332](https://github.com/TheIllusiveC4/Curios/issues/332)
- Fixed `CuriosEquipEvent` firing erroneously [#305](https://github.com/TheIllusiveC4/Curios/issues/305)
- Fixed item insertions handled directly through item handlers not being
  validated [#238](https://github.com/TheIllusiveC4/Curios/issues/238)
- Fixed status effects not rendering in the Curios GUI [#95](https://github.com/TheIllusiveC4/Curios/issues/95)

## [5.2.0-beta.3+1.20.1] - 2023.06.12

### Changed

- Updated to Minecraft 1.20.1

### Fixed

- Fixed crash upon loading world with a mod that uses legacy IMC slot
  registration [#314](https://github.com/TheIllusiveC4/Curios/issues/314)

## [5.2.0-beta.2+1.20] - 2023.06.11

### Fixed

- Fixed log spam [#312](https://github.com/TheIllusiveC4/Curios/issues/312)

## [5.2.0-beta.1+1.20] - 2023.06.11

### Added

- Added new slot and entity registration through datapacks,
  see [Curios Documentation](https://docs.illusivesoulworks.com/category/curios) for more information

### Changed

- Updated to Minecraft 1.20
- Curios capabilities have been extended natively to all `LivingEntity` entity types

### Deprecated

- Deprecated `SlotTypeMessage` and `SlotTypePreset`, registration through datapacks is now recommended over IMC

### Removed

- Removed Curios server configuration file

## [1.19.4-5.1.5.3] - 2023.05.05

### Fixed

- Fixed default curio slot texture not showing properly

## [1.19.4-5.1.5.2] - 2023.04.28

### Changed

- Updated `ru_ru` localization (thanks Heimdallr-1!) [#302](https://github.com/TheIllusiveC4/Curios/pull/302)

## [1.19.4-5.1.5.1] - 2023.04.20

### Changed

- Curios swapping will now take into account all valid slots instead of just the first valid slot

### Fixed

- Fixed duplication bug when equipping from use in the hotbar

## [1.19.4-5.1.5.0] - 2023.04.16

### Added

- Added `CurioAttributeModifierEvent` for editing curio attribute modifiers in
  slots [#292](https://github.com/TheIllusiveC4/Curios/issues/292)

### Changed

- Curios that can be equipped from use in the hotbar will now swap with existing curios in valid
  slots [#301](https://github.com/TheIllusiveC4/Curios/issues/301)

## [1.19.4-5.1.4.3] - 2023.04.06

### Fixed

- Fixed toggle visibility buttons causing menu issues when
  clicked [#296](https://github.com/TheIllusiveC4/Curios/issues/296)

## [1.19.4-5.1.4.2] - 2023.03.16

### Changed

- Updated to Minecraft 1.19.4
- Updated slot textures to match new Minecraft slot textures

## [1.19.3-5.1.4.1] - 2023.03.15

### Added

- Added `vi_vn` localization (thanks ZzThanhBaozZ!) [#289](https://github.com/TheIllusiveC4/Curios/pull/289)

### Changed

- Updated `it_it` localization (thanks WVam!) [#288](https://github.com/TheIllusiveC4/Curios/pull/288)

### Fixed

- Fixed crashes when slots get resized to negative amounts dynamically

## [1.19.3-5.1.4.0] - 2023.02.27

### Added

- Added `/curios drop` command for dropping curio items

### Changed

- Updated `zh_cn` localization (thanks WadjetSama!) [#285](https://github.com/TheIllusiveC4/Curios/issues/285)
- Updated `it_it` localization (thanks WVam!) [#284](https://github.com/TheIllusiveC4/Curios/pull/284)

### Fixed

- Fixed curio entity selectors counting slots with size 0 erroneously

## [1.19.3-5.1.3.1] - 2023.02.15

### Fixed

- Fixed `SlotModifiersUpdatedEvent` not always firing

## [1.19.3-5.1.3.0] - 2023.02.13

### Added

- Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers [#276](https://github.com/TheIllusiveC4/Curios/issues/276)
- Added two commands:
    - `/curios replace <slot> <index> <player> with <item> [count]` for setting items to curio slots
    - `/curios list` for listing all registered slots and which mods they come
      from [#261](https://github.com/TheIllusiveC4/Curios/issues/261)
- Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and getting
  items from curio slots respectively

### Changed

- `ICurio#onEquipFromUse` now fires when items are placed into slots in inventory screens

### Fixed

- Fixed equip sounds not playing properly in the Curios screen (thanks
  bconlon!) [#281](https://github.com/TheIllusiveC4/Curios/pull/281)

## [1.19.3-5.1.2.0] - 2023.01.09

### Added

- Added new `ICurio#canWalkOnPowderedSnow` method for curio implementations that allow walking on Powdered Snow blocks
  (thanks bconlon!) [#273](https://github.com/TheIllusiveC4/Curios/pull/273)

## [1.19.3-5.1.1.2] - 2022.12.08

### Fixed

- Fix version requirements for Minecraft and Forge

## [1.19.3-5.1.1.1] - 2022.12.08

### Changed

- Updated to Minecraft 1.19.3
- Updated to Forge 44.0.0+
- Updated Portuguest (pt_br) localization (thanks FITFC!) [#265](https://github.com/TheIllusiveC4/Curios/pull/265)

### Removed

- Removed JEI integration temporarily until the mod is ported to 1.19.3

## [1.19.2-5.1.1.0] - 2022.08.08

### Added

- Re-added JEI integration

### Changed

- Updated to Minecraft 1.19.2
- Updated to Forge 43.0.0+
- Updated Polish (pl_pl) localization (thanks Greg-21!) [#247](https://github.com/TheIllusiveC4/Curios/pull/247)
- Updated Ukranian (uk_ua) localization (thanks Sushomeister!) [#245](https://github.com/TheIllusiveC4/Curios/pull/245)

## [1.19.1-5.1.0.5] - 2022.07.28

### Changed

- Updated to Minecraft 1.19.1
- Updated to Forge 42.0.0+

## [1.19-5.1.0.4] - 2022.07.11

### Changed

- Updated to and requires Forge 41.0.94+

## [1.19-5.1.0.3] - 2022.07.10

### Changed

- Updated to and requires Forge 41.0.64+

### Removed

- Temporarily removed JEI integration as the mod is not compatible currently with the required versions of Forge

## [1.19-5.1.0.2] - 2022.06.20

### Added

- Re-added JEI integration

### Fixed

- Fixed datapack functions failing to load when utilizing Curios slot
  commands [#240](https://github.com/TheIllusiveC4/Curios/issues/240)
- Fixed null attributes crashing clients when hovering over curio
  tooltips [#242](https://github.com/TheIllusiveC4/Curios/issues/242)

## [1.19-5.1.0.1] - 2022.06.09

### Changed

- Revert `CurioSlot` patch
- Updated to and requires Forge 41.0.8+

## [1.19-5.1.0.0] - 2022.06.07

### Changed

- Updated to Minecraft 1.19+
- Updated to Forge 41+

## [1.18.2-5.0.9.1] - 2023.06.15

### Fixed

- Fixed possible NullPointerException crash from SlotResult

## [1.18.2-5.0.9.0] - 2023.03.02

### Added

- Added `/curios drop` command for dropping curio items

### Fixed

- Fixed curio entity selectors counting slots with size 0 erroneously

## [1.18.2-5.0.8.0] - 2023.02.15

### Added

- Added new `ICurio#canWalkOnPowderedSnow` method for curio implementations that allow walking on Powdered Snow blocks
  (thanks bconlon!) [#273](https://github.com/TheIllusiveC4/Curios/pull/273)
- Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers [#276](https://github.com/TheIllusiveC4/Curios/issues/276)
- Added two commands:
    - `/curios replace <slot> <index> <player> with <item> [count]` for setting items to curio slots
    - `/curios list` for listing all registered slots and which mods they come
      from [#261](https://github.com/TheIllusiveC4/Curios/issues/261)
- Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and getting
  items from curio slots respectively

## [1.18.2-5.0.7.1] - 2022.06.20

### Fixed

- Fixed datapack functions failing to load when utilizing Curios slot
  commands [#240](https://github.com/TheIllusiveC4/Curios/issues/240)
- Fixed null attributes crashing clients when hovering over curio
  tooltips [#242](https://github.com/TheIllusiveC4/Curios/issues/242)

## [1.18.2-5.0.7.0] - 2022.04.12

### Added

- Added `it_it` localization (thanks BlackShadow77!) [#235](https://github.com/TheIllusiveC4/Curios/pull/235)
- Added `curios:set_curio_attributes` loot function
- Added support for `minecraft:freeze_immune_wearables` tagged items in curios slots

### Fixed

- Fixed curios Fortune levels being applied to other
  enchantments [#234](https://github.com/TheIllusiveC4/Curios/issues/234)

## [1.18.2-5.0.6.3] - 2022.03.02

### Changed

- Updated to Minecraft 1.18.2

## [1.18.1-5.0.6.2] - 2022.02.25

### Changed

- Updated `ko_kr.json` localization (thanks PixVoxel!) [#223](https://github.com/TheIllusiveC4/Curios/pull/223)

### Fixed

- Fixed curios being unequipped when used with additional slots and
  relogging [#218](https://github.com/TheIllusiveC4/Curios/issues/218)
- Fixed NPE crash with certain mods that implement their own Curios
  providers [#225](https://github.com/TheIllusiveC4/Curios/issues/225)

## [1.18.1-5.0.6.1] - 2022.02.15

### Changed

- Updated `ko_kr.json` localization (thanks mindy15963!) [#211](https://github.com/TheIllusiveC4/Curios/pull/211)

### Fixed

- Fixed `ICurioItem#getEquipSound` not being called [#222](https://github.com/TheIllusiveC4/Curios/issues/222)
- Fixed crashes with out-of-bound indices when unequipping items that provide slot attribute
  modifiers [#221](https://github.com/TheIllusiveC4/Curios/issues/221)

## [1.18.1-5.0.6.0] - 2022.01.18

### Added

- Added `uk_ua.json` localization (thanks Sushomeister!)
- Added new entity selector option, `curios=`. More information at
  the [wiki](https://github.com/TheIllusiveC4/Curios/wiki/Commands#entity-selector-options).

### Changed

- Updated `ru_ru.json` localization (thanks Sushomeister!)

## [1.18.1-5.0.5.2] - 2022.01.15

### Fixed

- Fixed deprecated usages of `ISlotHelper#unlockSlotType` and `ISlotHelper#lockSlotType` modifying slots differently
  from previous behavior
- Fixed deprecated usages of `ISlotHelper#growSlotType` and `ISlotHelper#shrinkSlotType` logging client-side errors to
  the console while in the Curios inventory

## [1.18.1-5.0.5.1] - 2021.01.09

### Fixed

- Fixed dragged items desyncing when switching between Curios and the Creative inventory
  screen [#202](https://github.com/TheIllusiveC4/Curios/issues/202)

## [1.18.1-5.0.5.0] - 2021.01.08

### Added

- [API] Added `findFirstCurio` and `findCurios` methods to `ICuriosHelper`

### Changed

- Updated `ko_kr` localization (thanks mindy15963!) [#200](https://github.com/TheIllusiveC4/Curios/pull/200)

### Deprecated

- [API] Deprecated `findEquippedCurio` methods in `ICuriosHelper`

### Fixed

- Fixed knockback resistance tooltips not being formatted correctly
- Fixed dragged items desyncing when switching between Curios and vanilla screens
- Fixed clearing inventory via Creative 'x' button resulting in lingering attribute modifiers when equipped

## [1.18.1-5.0.4.2] - 2021.12.27

### Fixed

- Fixed client-side syncing errors when applying slot modifiers to slots with base size 0
- Fixed slot amount changes persistent when not applicable

## [1.18.1-5.0.4.1] - 2021.12.24

### Fixed

- Fixed crash related to Fortune loot functions [#198](https://github.com/TheIllusiveC4/Curios/issues/198)

## [1.18.1-5.0.4.0] - 2021.12.23

### Added

- [API] Added `makesPiglinNeutral` method for creating curios that can make piglins neutral like gold
  armor [#86](https://github.com/TheIllusiveC4/Curios/issues/86)
- [API] Added `isEnderMask` method for creating curios that can hide player heads from Endermen like carved
  pumpkins [#196](https://github.com/TheIllusiveC4/Curios/issues/196)
- Added mixins, developers building against this version of Curios and newer will need to make sure their environment is
  configured for mixin dependency development (see README.md)

### Changed

- Replaced fortune global loot modifier with internal methods

## [1.18.1-5.0.3.1] - 2021.12.19

### Fixed

- Fixed slot amount changes not persisting when applicable
- Fixed slot amount changes causing loading errors [#195](https://github.com/TheIllusiveC4/Curios/issues/195)

## [1.18.1-5.0.3.0] - 2021.12.14

### Added

- Re-add Just Enough Items integration

### Changed

- Updated to Minecraft 1.18.1
- Updated Russian localization (thanks DrHesperus!) [#190](https://github.com/TheIllusiveC4/Curios/pull/190)

## [1.18-5.0.2.5] - 2021.12.14

### Changed

- [API] `ICurio#canEquipFromUse` and `ICurio#onEquipFromUse` methods are now called both client-side and server-side
  (previously server-side only)

### Fixed

- Fixed curio items not calling their `use` logic client-side [#192](https://github.com/TheIllusiveC4/Curios/issues/192)

## [1.18-5.0.2.4] - 2021.12.05

### Fixed

- Fixed crash with client-side player entities [#189](https://github.com/TheIllusiveC4/Curios/issues/189)

## [1.18-5.0.2.3] - 2021.12.02

### Fixed

- Fixed crash when running data generation in development
  environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.18-5.0.2.2] - 2021.12.02

### Fixed

- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot
  type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)

## [1.18-5.0.2.1] - 2021.11.30

### Changed

- Updated to Minecraft 1.18
- Updated to Forge 38+

## [1.17.1-5.0.2.7] - 2022.01.15

### Fixed

- Fixed deprecated usages of `ISlotHelper#unlockSlotType` and `ISlotHelper#lockSlotType` modifying slots differently
  from previous behavior
- Fixed deprecated usages of `ISlotHelper#growSlotType` and `ISlotHelper#shrinkSlotType` logging client-side errors to
  the console while in the Curios inventory
- Fixed knockback resistance tooltips not being formatted correctly
- Fixed dragged items desyncing when switching between Curios and vanilla screens
- Fixed clearing inventory via Creative 'x' button resulting in lingering attribute modifiers when equipped
- Fixed client-side syncing errors when applying slot modifiers to slots with base size 0
- Fixed slot amount changes persistent when not applicable

## [1.17.1-5.0.2.6] - 2021.12.19

### Fixed

- Fixed slot amount changes not persisting when applicable
- Fixed slot amount changes causing loading errors [#195](https://github.com/TheIllusiveC4/Curios/issues/195)

## [1.17.1-5.0.2.5] - 2021.12.15

### Changed

- [API] `ICurio#canEquipFromUse` and `ICurio#onEquipFromUse` methods are now called both client-side and server-side
  (previously server-side only)

### Fixed

- Fixed curio items not calling their `use` logic client-side [#192](https://github.com/TheIllusiveC4/Curios/issues/192)

## [1.17.1-5.0.2.4] - 2021.12.05

### Fixed

- Fixed crash with client-side player entities [#189](https://github.com/TheIllusiveC4/Curios/issues/189)

## [1.17.1-5.0.2.3] - 2021.12.02

### Fixed

- Fixed crash when running data generation in development
  environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.17.1-5.0.2.2] - 2021.12.02

### Fixed

- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot
  type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)
- Fixed potential crash on dedicated servers [#184](https://github.com/TheIllusiveC4/Curios/issues/184)

## [1.17.1-5.0.2.1] - 2021.11.30

### Fixed

- Fixed resource loading crashing when invalid texture files are
  found [#183](https://github.com/TheIllusiveC4/Curios/issues/183)

## [1.17.1-5.0.2.0] - 2021.11.30

Please note that this update is more experimental than most and is marked as a beta. Be cautious about adding this to
stable worlds.

### Added

- [API] Added slot modifier system, see [the wiki page](https://github.com/TheIllusiveC4/Curios/wiki/Slot-Modifiers) for
  more info. This is the main reason the update is experimental. Although this system was designed to be backwards
  compatible, be wary of issues with older slot modification
  methods. [#178](https://github.com/TheIllusiveC4/Curios/issues/178)
- [API] Added `CurioEquipEvent` and `CurioUnequipEvent` to allow modders the ability to intercept and change
  equip/unequip results [#174](https://github.com/TheIllusiveC4/Curios/issues/174)
- [API] Added new slot texture registration method: textures located in the `assets/curios/textures/slot` directory in
  any mod or resource pack will be automatically stitched to the texture atlas and usable by
  slots [#145](https://github.com/TheIllusiveC4/Curios/issues/145)
- [API] Added `ICuriosItemHandler#saveInventory` and `ICuriosItemHandler#loadInventory` for saving/loading the Curios
  inventory more conveniently [#164](https://github.com/TheIllusiveC4/Curios/issues/164)

### Fixed

- Fixed syncing slot shrinking client-side [#179](https://github.com/TheIllusiveC4/Curios/issues/179)

## [1.17.1-5.0.1.0] - 2021.09.11

### Added

- Added Polish translation (thanks Greg-21!) [#163](https://github.com/TheIllusiveC4/Curios/pull/163)
- Re-added Just Enough Items integration

### Changed

- Updated to Forge 37.0.42+

### Fixed

- Fixed slots desyncing when shrinking/growing amounts

## [1.17.1-5.0.0.1] - 2021.08.04

### Fixed

- Fixed NPE when using cosmetic slots [#157](https://github.com/TheIllusiveC4/Curios/issues/157)

## [1.17.1-5.0.0.0] - 2021.07.27

### Added

- Added `getStack` method to `ICurio`
- Added slot context-sensitive alternatives to methods in `ICurio` and `ICurioItem`
- Added new rendering system, see
  the [GitHub wiki](https://github.com/TheIllusiveC4/Curios/wiki/1.16.5-to-1.17:-Updates-and-Changes#rendering-system)
  for more details

### Changed

- Updated to Minecraft 1.17.1

### Deprecated

- Deprecated methods in `ICurio` and `ICurioItem` without slot contexts
- Deprecated locking and unlocking slot states

### Removed

- Removed `render` and `canRender` from `ICurio` and `ICurioItem`
- Removed ring, amulet, crown, and knuckles items

## [1.16.5-4.1.0.0] - 2023.03.03

### Added

- [API] Added `SlotModifiersUpdatedEvent` to listen for broadcasted dynamic changes to slot sizes on players due to slot
  modifiers
- [API] Added `ICuriosHelper#setEquippedCurio` and `ICuriosHelper#findCurio()` for setting items into curio slots and
  getting
  items from curio slots respectively
- Added new commands:
    - `/curios drop` for dropping curio items from slots
    - `/curios list` for listing curio slots and origins
    - `/curios replace` for setting curio items into slots
- Added `curios:set_curio_attributes` loot function

### Fixed

- Fixed curio entity selectors counting slots with size 0 erroneously
- Fixed startup crashes with ConcurrentModificationException

## [1.16.5-4.0.8.2] - 2022.03.31

### Changed

- Updated `fr_fr.json` localization (thanks HollishKid!) [#228](https://github.com/TheIllusiveC4/Curios/pull/228)

### Fixed

- Fixed slot modifiers incorrectly persisting in some cases

## [1.16.5-4.0.8.1] - 2022.02.25

### Changed

- Updated `ko_kr.json` localization (thanks PixVoxel!) [#223](https://github.com/TheIllusiveC4/Curios/pull/223)

### Fixed

- Fixed curios being unequipped when used with additional slots and
  relogging [#218](https://github.com/TheIllusiveC4/Curios/issues/218)
- Fixed NPE crash with certain mods that implement their own Curios
  providers [#225](https://github.com/TheIllusiveC4/Curios/issues/225)

## [1.16.5-4.0.8.0] - 2021.01.18

### Added

- Added `uk_ua.json` localization (thanks Sushomeister!)
- Added new entity selector option, `curios=`. More information at
  the [wiki](https://github.com/TheIllusiveC4/Curios/wiki/Commands#entity-selector-options).

### Changed

- Updated `ru_ru.json` localization (thanks Sushomeister!)

## [1.16.5-4.0.7.0] - 2021.01.08

### Added

- [API] Added `findFirstCurio` and `findCurios` methods to `ICuriosHelper`

### Changed

- Updated `ko_kr` localization (thanks mindy15963!) [#200](https://github.com/TheIllusiveC4/Curios/pull/200)

### Deprecated

- [API] Deprecated `findEquippedCurio` methods in `ICuriosHelper`

### Fixed

- Fixed knockback resistance tooltips not being formatted correctly
- Fixed clearing inventory via Creative 'x' button resulting in lingering attribute modifiers when equipped

## [1.16.5-4.0.6.8] - 2021.12.27

### Fixed

- Fixed client-side syncing errors when applying slot modifiers to slots with base size 0
- Fixed slot amount changes persistent when not applicable

## [1.16.5-4.0.6.7] - 2021.12.19

### Fixed

- Fixed slot amount changes not persisting when applicable
- Fixed slot amount changes causing loading errors [#195](https://github.com/TheIllusiveC4/Curios/issues/195)

## [1.16.5-4.0.6.6] - 2021.12.15

### Changed

- [API] `ICurio#canEquipFromUse` and `ICurio#onEquipFromUse` methods are now called both client-side and server-side
  (previously server-side only)

### Fixed

- Fixed curio items not calling their `use` logic client-side [#192](https://github.com/TheIllusiveC4/Curios/issues/192)

## [1.16.5-4.0.6.5] - 2021.12.05

### Fixed

- Fixed crash with client-side player entities [#189](https://github.com/TheIllusiveC4/Curios/issues/189)

## [1.16.5-4.0.6.4] - 2021.12.02

### Fixed

- Fixed crash when running data generation in development
  environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.16.5-4.0.6.3] - 2021.12.01

### Fixed

- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot
  type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)
- Fixed potential crash on dedicated servers [#184](https://github.com/TheIllusiveC4/Curios/issues/184)

## [1.16.5-4.0.6.2] - 2021.11.30

### Fixed

- Fixed resource loading crashing when invalid texture files are
  found [#183](https://github.com/TheIllusiveC4/Curios/issues/183)

## [1.16.5-4.0.6.1] - 2021.11.30

### Fixed

- Fixed syncing slot shrinking client-side, actually this time

## [1.16.5-4.0.6.0] - 2021.11.30

Please note that this update is more experimental than most and is marked as a beta. Be cautious about adding this to
stable worlds.

### Added

- [API] Added slot modifier system, see [the wiki page](https://github.com/TheIllusiveC4/Curios/wiki/Slot-Modifiers) for
  more info. This is the main reason the update is experimental. Although this system was designed to be backwards
  compatible, be wary of issues with older slot modification
  methods. [#178](https://github.com/TheIllusiveC4/Curios/issues/178)
- [API] Added `CurioEquipEvent` and `CurioUnequipEvent` to allow modders the ability to intercept and change
  equip/unequip results [#174](https://github.com/TheIllusiveC4/Curios/issues/174)
- [API] Added new slot texture registration method: textures located in the `assets/curios/textures/slot` directory in
  any mod or resource pack will be automatically stitched to the texture atlas and usable by
  slots [#145](https://github.com/TheIllusiveC4/Curios/issues/145)
- [API] Added `ICuriosItemHandler#saveInventory` and `ICuriosItemHandler#loadInventory` for saving/loading the Curios
  inventory more conveniently [#164](https://github.com/TheIllusiveC4/Curios/issues/164)

### Fixed

- Fixed syncing slot shrinking client-side [#179](https://github.com/TheIllusiveC4/Curios/issues/179)

## [1.16.5-4.0.5.3] - 2021.09.11

### Added

- Added Polish translation (thanks Greg-21!) [#163](https://github.com/TheIllusiveC4/Curios/pull/163)

### Fixed

- Fixed slots desyncing when shrinking/growing amounts
- Fixed null NBT tag crashes when syncing [#152](https://github.com/TheIllusiveC4/Curios/issues/152)
- Fixed class-loading errors by annotating curio render methods with
  `OnlyIn(Dist.CLIENT)` [#121](https://github.com/TheIllusiveC4/Curios/issues/121)

## [1.16.5-4.0.5.2] - 2021.06.07

### Added

- Added Catalogue integration
- Added Spanish translation (thanks FrannDzs!) [#139](https://github.com/TheIllusiveC4/Curios/pull/139)

### Changed

- Slots can now be assigned 0 size

### Fixed

- Fixed curio-item use desyncs [#141](https://github.com/TheIllusiveC4/Curios/issues/141)
- Fixed shift-clicking curio slot priority [#108](https://github.com/TheIllusiveC4/Curios/issues/108)
- Fixed curio button offsets [#140](https://github.com/TheIllusiveC4/Curios/issues/140)

## [1.16.5-4.0.5.1] - 2021.04.14

### Changed

- Reverted some changes from previous version so that invalidation only happens due to tag updates, modified curio
  behavior may cause lingering items but fixes issues with invalidating curios from various
  mods [#124](https://github.com/TheIllusiveC4/Curios/issues/124)

### Fixed

- Fixed button offset with Quark backpack (thanks BookerCatch!) [#119](https://github.com/TheIllusiveC4/Curios/pull/119)

## [1.16.5-4.0.5.0] - 2021.03.07

### Added

- `ICurio#onEquip(SlotContext, ItemStack)`
- `ICurio#onUnequip(SlotContext, ItemStack)`
- `ICuriosHelper#isStackValid(SlotContext, ItemStack)`

### Changed

- Items that are invalidated while in a slot, due to modified tags or changed curio behavior, will
  now automatically eject from its slot and be given to the player
- Corrected Chinese translation (thanks qsefthuopq!) [#106](https://github.com/TheIllusiveC4/Curios/pull/106)

### Deprecated

- `ICurio#onEquip(String, int, LivingEntity)`
- `Icurio#onUnequip(String, int, LivingEntity)`

## [1.16.5-4.0.4.0] - 2021.01.29

### Added

- `SlotContext` for providing and retrieving slot context throughout the API
- `ICurio#getEquipSound(SlotContext)` [#104](https://github.com/TheIllusiveC4/Curios/issues/104)
- `ICurio#canEquipFromUse(SlotContext)`
- `ICurio#onEquipFromUse(SlotContext)`
- `ICurio#getAttributeModifiers(SlotContext, UUID)`
- `ICurioItem#getEquipSound(SlotContext, ItemStack)`
- `ICurioItem#canEquipFromUse(SlotContext, ItemStack)`
- `ICurioItem#onEquipFromUse(SlotContext, ItemStack)`
- `ICurioItem#getAttributeModifiers(SlotContext, UUID, ItemStack)`
- `ICuriosHelper#getAttributeModifiers(SlotContext, UUID, ItemStack)`

### Deprecated

- `ICurio#canRightClickEquip()` [#102](https://github.com/TheIllusiveC4/Curios/issues/102)
- `ICurio#playRightClickEquipSound(LivingEntity)` [#102](https://github.com/TheIllusiveC4/Curios/issues/102)
- `ICurio#getAttributeModifiers(String)`
- `ICurioItem#canRightClickEquip(ItemStack)` [#102](https://github.com/TheIllusiveC4/Curios/issues/102)
-

`ICurioItem#playRightClickEquipSound(LivingEntity, ItemStack)` [#102](https://github.com/TheIllusiveC4/Curios/issues/102)

- `ICurioItem#getAttributeModifiers(String, ItemStack)`
- `ICuriosHelper#getAttributeModifiers(String, ItemStack)`

## [1.16.4-4.0.3.5] - 2021.01.14

### Fixed

- Fixed crash related to performing logic on empty itemstacks

## [1.16.4-4.0.3.4] - 2021.01.12

### Fixed

- Fixed command slot type identification for dedicated servers

## [1.16.4-4.0.3.3] - 2020.12.31

### Added

- Added Korean localization (thanks othuntgithub!) [#97](https://github.com/TheIllusiveC4/Curios/pull/97)

## [1.16.4-4.0.3.2] - 2020.12.26

### Changed

- Reverted ICurio#onUnequip change from 4.0.3.1

## [1.16.4-4.0.3.1] - 2020.12.26

### Changed

- [API] ICurio#onUnequip now passes the actual ItemStack instance instead of a copy

## [1.16.4-4.0.3.0] - 2020.11.24

### Added

- [API] ICurioItem interface for simple hard-dependency item implementations

### Changed

- Minor improvements to "Curio" slot type and tag for universal acceptance

### Fixed

- Fixed infinite log spam when using recipe book [#91](https://github.com/TheIllusiveC4/Curios/issues/91)
- Fixed command client crashes [#89](https://github.com/TheIllusiveC4/Curios/issues/89)
- Fixed overextended texture in Curios GUI

## [1.16.4-4.0.2.1] - 2020.11.09

### Changed

- Updated to 1.16.4
- "Curio" slot type now accepts any curio item [#78](https://github.com/TheIllusiveC4/Curios/issues/78)

### Fixed

- Fixed NPE with Mahou Tsukai's scrying [#77](https://github.com/TheIllusiveC4/Curios/issues/77)
- Fixed empty tags being attached to items [#80](https://github.com/TheIllusiveC4/Curios/issues/80)
- Fixed render buttons toggling wrong
  slot [#75](https://github.com/TheIllusiveC4/Curios/issues/75) [#84](https://github.com/TheIllusiveC4/Curios/issues/84)

## [1.16.3-4.0.2.0] - 2020.09.20

Update courtesy of Extegral, thanks! [#72](https://github.com/TheIllusiveC4/Curios/pull/72)

### Added

- [API] Added ICurio#showAttributesTooltip for toggling attribute tooltips per curio
- [API] Added ICurio#getFortuneBonus for adding fortune levels when curio is equipped
- [API] Added ICurio#getLootingBonus for adding looting levels when curio is equipped
- Added curios:equip_curio criterion trigger for listening to curio equips

### Changed

- Updated Russian localization

### Fixed

- Fixed bug with recipe book persisting on Curios screen when going from Survival to Creative

## [1.16.3-4.0.1.0] - 2020.09.14

### Added

- Added Brazilian localization (thanks Mikeliro!) [#65](https://github.com/TheIllusiveC4/Curios/pull/65)

### Changed

- Updated to Minecraft 1.16.3

### Fixed

- Fixed crashing when attempting to unlock/lock slots in equip
  handlers [#68](https://github.com/TheIllusiveC4/Curios/issues/68)
- Fixed misaligned render toggle buttons with non-natively-positioned slots

## [1.16.2-4.0.0.1] - 2020.08.26

### Changed

- Updated to Forge 33.0.21

## [1.16.2-4.0.0.0] - 2020.08.13

### Changed

- Updated to Minecraft 1.16.2
- Archive base name changed from "curios" to "curios-forge"
- Mod version no longer contains "FORGE"

## [3.0.0.2] - 2020.08.03

### Fixed

- Fixed toggle render buttons desyncing when scrolling
- Fixed recipe book offsets in Curios GUI

## [3.0.0.1] - 2020.08.02

### Changed

- [API] Moved ICuriosItemHandler#handleInvalidStacks call to tick event

### Fixed

- Fixed hidden slots still being handled by Curios GUI

## [3.0] - 2020.07.21

### Fixed

- Fixed Curios button disappearing when switching tabs in Creative
  menu [#55](https://github.com/TheIllusiveC4/Curios/issues/55)
- Fixed being able to right-click Curios items into any slot [#56](https://github.com/TheIllusiveC4/Curios/issues/56)

## [3.0-beta4] - 2020.07.17

### Fixed

- Fixed crashes with Forge 32.0.67+ [#53](https://github.com/TheIllusiveC4/Curios/issues/53)

## [3.0-beta3] - 2020.07.14

### Changed

- [API] IMC messages can now process iterable collections of SlotTypeMessage

## [3.0-beta2] - 2020.07.02

### Added

- [API] Added ICuriosHelper#getEquippedCurios that obtains all equipped, non-cosmetic curios

### Changed

- [API] IDynamicStackHandler now extends IItemHandlerModifiable

### Fixed

- Fixed NPE when registering no icon for a slot type

## [3.0-beta1] - 2020.07.01

### Added

- Opt-in cosmetic slots for slot types
- Toggleable rendering for equipped curio items
- "Bracelet" has been added as a potential preset slot type

### Changed

- Ported to 1.16.1 Forge
- Slot types are now handled server-side, allowing for per-world slot configurations
- Icon registration is now done alongside slot registration without needing a separate IMC message
- ItemStacks with different durabilities will trigger curio change events
- Names:
    - LivingCurioChangeEvent -> CurioChangeEvent
    - LivingCurioDropRulesEvent -> DropRulesEvent
    - LivingCurioDropsEvent -> CurioDropsEvent
    - CurioIMCMessage -> SlotTypeMessage
    - CuriosAPI -> CuriosApi
    - ICurioItemHandler -> ICuriosItemHandler:
        - getCurioMap -> getCurios
        - setCurioMap -> setCurios
        - getStackHandler -> getStacksHandler
        - enableCurio -> unlockSlotType
        - disableCurio -> lockSlotType
        - addCurioSlot -> growSlotType
        - removeCurioSlot -> shrinkSlotType
        - getDisabled -> getLockedSlots
        - addInvalid -> loseInvalidStack
        - dropInvalidCache -> handleInvalidStacks
    - ICurio:
        - onCurioTick -> curioTick
        - onCurioAnimate -> curioAnimate
        - onEquipped -> onEquip
        - onUnequipped -> onUnequip
        - playRightClickEquipSound -> playEquipSound
        - onCurioBreak -> curioBreak
        - shouldSyncToTracking -> canSync
        - getSyncTag -> writeSyncData
        - readSyncTag -> readSyncData
        - hasRender -> canRender
    - Commands:
        - enable -> unlock
        - disable -> lock
- Abstraction:
    - CurioType abstracted to ISlotType
    - CurioStackHandler abstracted to ICurioStacksHandler
    - CuriosApi split into three helper interfaces (IIconHelper on the client, ISlotHelper on the server, ICuriosHelper
      for both)
- CurioTags functionality refactored into SlotTypePresets
- The above changes are only for the API, the rest of the classes have also had extensive changes

## [2.0.2.7] - 2021.01.14

### Fixed

- Fixed crash related to performing logic on empty itemstacks

## [2.0.2.6] - 2020.08.01

### Fixed

- Curios GUI no longer tries to render list without visible slots

## [2.0.2.5] - 2020.08.01

### Fixed

- Fixed hidden slots still being handled by Curios GUI

## [2.0.2.4] - 2020.06.03

### Changed

- Curios scroll bar has more precise scrolling and the scroll position now persists during a session

## [2.0.2.3] - 2020.05.31

### Fixed

- Fixed empty item states being ticked while in Curio slots [#50](https://github.com/TheIllusiveC4/Curios/issues/50)

## [2.0.2.2] - 2020.04.19

### Changed

- Changed Recipe Book to dedicated implementation [#48](https://github.com/TheIllusiveC4/Curios/issues/48)
- Compressed texture files (thanks Darkhax!) [#49](https://github.com/TheIllusiveC4/Curios/pull/49)

## [2.0.2.1] - 2020.03.26

### Added

- Added Chinese localization (thank you EnterFor!)

## [2.0.2] - 2020.03.21

### Added

- Added support for curio attribute modifiers using NBT tags, simply replace "AttributeModifiers" with "
  CurioAttributeModifiers"

## [2.0.1] - 2020.03.08

### Added

- [API] ICurio#onCurioAnimate - Called every tick client-side only while curio is equipped

## [2.0] - 2020.02.24

### Fixed

- Fixed compatibility issue with Ensorcellation's Soulbound
  enchantment [#41](https://github.com/TheIllusiveC4/Curios/issues/41)

## [2.0-beta2] - 2020.01.26

### Changed

- Updated to 1.15.2

### Added

- Re-added JEI integration
- Added Japanese localization (thanks MORIMORI0317!)

### Fixed

- Fixed generic curio slot icon showing missing texture

## [2.0-beta] - 2019.12.30

### Changed

- Ported to 1.15.1
- [API] ICurio#doRender -> ICurio#render
- Curio rendering no longer automatically applies sneaking translations
- Curio slot icons need to be manually stitched into the block texture atlas

## [1.0.6.1] - 2020.1.24

### Added

- Japanese localization (thanks MORIMORI0317!)

## [1.0.6] - 2019.12.03

### Added

- [API] ICurio#getDropRule for specifying drop on death behavior (DEFAULT, ALWAYS_DROP, ALWAYS_KEEP, DESTROY)
- [API] LivingCurioDropRulesEvent for specifying drop on death behavior overrides based off a Predicate<ItemStack>

## [1.0.5.4] - 2019.11.30

### Added

- Added separate config options for Curio button positioning in Creative
  GUI [#35](https://github.com/TheIllusiveC4/Curios/issues/35)
- Added recipe book button to Curios GUI [#31](https://github.com/TheIllusiveC4/Curios/issues/31)

### Fixed

- Fixed rendering bug with JEI integration [#35](https://github.com/TheIllusiveC4/Curios/issues/35)

## [1.0.5.3] - 2019.11.12

### Added

- Added config options for Curio button positioning [#33](https://github.com/TheIllusiveC4/Curios/issues/33)

### Changed

- Picked up items will now persist when switching to and from the Curios
  GUI [#32](https://github.com/TheIllusiveC4/Curios/issues/32)

## [1.0.5.2] - 2019.10.31

### Changed

- Updated Russian localization (thanks Extegral!) [#30](https://github.com/TheIllusiveC4/Curios/pull/30)

## [1.0.5.1] - 2019.10.30

### Changed

- Using the curios command with an invalid slot type will now throw an error (thanks
  ItsTheBdoge!) [#29](https://github.com/TheIllusiveC4/Curios/pull/29)

### Fixed

- Fixed JEI bookmarks overlapping with Curios GUI

## [1.0.5] - 2019.10.23

### Added

- [API] Added LivingCurioDropsEvent, fired inside Curio's LivingDropsEvent handler and allowing modders to edit the list
  of dropped curios added to the overall drops list.

### Changed

- Updated Russian localization (thanks Extegral!) [#27](https://github.com/TheIllusiveC4/Curios/pull/27)

### Fixed

- Fixed scroll wheel not working in the Curios GUI for slot list

## [1.0.4.1] - 2019.10.22

### Fixed

- Fixed UnsupportedOperationException crashes

## [1.0.4] - 2019.10.20

### Added

- [API] Added CuriosAPI#setSlotsForType helper method to set slot sizes for a given entity and curio type identifier (
  thanks ItsTheBdoge!) [#26](https://github.com/TheIllusiveC4/Curios/pull/26)
- [API] Added built-in support for "hands" curio type
- [API] Added ICurio.RenderHelper#followBodyRotations to rotate models according to entity pose
- Added new test item for "hands", Curious Knuckles

### Changed

- Changed tooltip for curio tags on items [#26](https://github.com/TheIllusiveC4/Curios/pull/26)

## [1.0.3] - 2019.10.13

### Added

- [API] Added CuriosAPI#getSlotsForType helper method to retrieve slot sizes for a given entity and curio type
  identifier

## [1.0.2.1] - 2019.10.12

### Changed

- Updated Russian localization (thanks Extegral!) [#24](https://github.com/TheIllusiveC4/Curios/pull/24)

### Fixed

- Fixed attribute modifiers not being applied when respawning with keepInventory gamerule set to
  true [#23](https://github.com/TheIllusiveC4/Curios/issues/23)

## [1.0.2] - 2019.10.06

### Added

- [API] Added generic "curio" tag for items that can go into any curio slot

## [1.0.1] - 2019.10.06

### Added

- [API] Added ICurio#getTagsTooltip to allow modders to customize curio tags tooltip information
- Added size alteration to createCurios config option by appending a semicolon and a size amount

### Fixed

- Fixed curio attribute modifiers not respecting HideFlags

## [1.0.0.3] - 2019.09.24

### Added

- Added Russian localization (thanks Extegral!) [#21](https://github.com/TheIllusiveC4/Curios/pull/21)

## [1.0.0.2] - 2019.09.23

### Fixed

- Fixed localization of Curios modifier tooltips, each one will now require an explicit key for each
  identifier [#19](https://github.com/TheIllusiveC4/Curios/issues/19)

## [1.0.0.1] - 2019.09.21

### Fixed

- Fixed item duplication exploit when right-click equipping Curios
- Fixed creative GUI behavior that caused shift-right-clicking to unintentionally destroy all
  Curios [#17](https://github.com/TheIllusiveC4/Curios/issues/17)

## [1.0] - 2019.09.13

### Added

- Added Curios button to the Creative inventory screen

### Changed

- [API] Re-formatted and updated some javadocs
- Updated to Forge RB 1.14.4-28.1.0

### Fixed

- Fixed client desyncing with items in curio slots [#15](https://github.com/TheIllusiveC4/Curios/issues/14)

## [0.25] - 2019.09.08

### Fixed

- Fixed sneak transformations on curio renders when in creative
  flight [#14](https://github.com/TheIllusiveC4/Curios/issues/14)

## [0.24] - 2019.08.22

### Fixed

- Attempt #2 to fix startup crashes [#12](https://github.com/TheIllusiveC4/Curios/issues/12)

## [0.23] - 2019.08.17

### Added

- Shift-clicking the delete item slot in the creative GUI will now clear all
  curios as well as the inventory

### Fixed

- Attempted to fix crashes on startup related to networking
  errors [#12](https://github.com/TheIllusiveC4/Curios/issues/12)

## [0.22] - 2019.08.11

### Changed

- [API] Refactored API to remove references to main Curios mod
- Moved Curio type tooltip to right below the item's display name

### Fixed

- Fixed Curio items disappearing when returning from the End [#11](https://github.com/TheIllusiveC4/Curios/issues/11)

## [0.21] - 2019.08.05

### Changed

- Updated Forge version to 28.0.45 to accommodate for a breaking change

## [0.20] - 2019.08.03

### Changed

- [API] Added call to ICurio#onCurioTick(String, LivingEntity) from
  ICurio#onCurioTick(String, int, LivingEntity) for backwards compatibility

## [0.19] - 2019.08.03

### Added

- [API] ICurio#onCurioBreak and CuriosAPI#onBrokenCurio for implementing
  break animations for curios in curio slots, defaulting to vanilla behavior
  but allowing for overrides as well
- [API] Added ICurio#onCurioTick(String, int, LivingEntity) for
  index-sensitive tick calls

### Changed

- [API] onUnequipped and onEquipped methods now fire only when items are
  different and ignore durability
- [API] ICurio#onCurioTick(String, LivingEntity) has been deprecated in favor
  of the index-sensitive version

### Fixed

- Fixed bug with Curios stacks not being cleaned up in the slots

## [0.18] - 2019.07.24

### Changed

- Updated to 1.14.4 Forge

### Fixed

- Fixed player twitching when switching to/from Curios inventory

## [0.17] - 2019.07.13

### Fixed

- Fixed items being duplicated when attempting to shift-click from the Curios
  GUI
- Fixed Curios items not being able to be shift-clicked into an appropriate
  slot without capabilities

## [0.16] - 2019.07.11

### Fixed

- Fixed Curios items disappearing when returning from the End or using gamerule keepInventory on death
- Fixed Curious Crown night vision desyncing with client on login

## [0.15] - 2019.07.08

### Fixed

- Fixed Curios GUI sometimes being colored purple due to an enchanted item being rendered

## [0.14] - 2019.06.30

### Changed

- [API] Removed fallback for missing identifier lang entries, so modders and users need to define these explicitly if
  they're not provided internally by Curios

### Fixed

- Fixed missing identifier lang entries for some common Curio tags

## [0.13] - 2019.06.28

### Changed

- Ported to 1.14.3 Forge
- [API] CuriosAPI#getType now returns an Optional value
- [API] CuriosAPI#getCurioEquipped methods each now return an Optional ImmutableTriple
- [API] Moved IMC processing out of the API

### Removed

- [API] CuriosAPI#registerIcon has been removed and replaced with IMC processing
- [API] CuriosAPI#getIcons has been removed and replaced with CuriosAPI#getIcon(String)
- [API] CuriosAPI.FinderData class and usages have been removed and replaced with ImmutableTriple

## [0.12] - 2019.06.08

### Changed

- Updated to last 1.13.2 Forge and mappings

## [0.11] - 2019.04.28

### Added

- Slot icons for commonly used terms for potential slots

## [0.10] - 2019.04.11

### Added

- [API] Curio item tag dictionary is available as a holder class to provide commonly used terms for potential slots

### Changed

- [API] Major API changes to streamline methods and emphasize concurrent determinism so that the registry will always
  output the same results.
    - Slot registry converted to IMC process
    - Icon registry isolated to client-side
    - Some CuriosRegistry methods moved to CuriousAPI so that the latter contains all methods intended for third-party
      use
- [API] Debug "Amulet" slot changed to "Necklace" slot

## [0.9] - 2019.03.20

### Fixed

- Fixed mods.toml so that URL and Authors fields show up correctly in the mod menu
- Fixed right-click equip syncing
- Fixed new icons not being recognized by old slots

## [0.8] - 2019.03.17

### Added

- Reobfuscation publishing
- API and sources jars

### Changed

- Updated mod icon

## [0.7] - 2019.03.12

### Changed

- Attribute tooltips for curios have been slightly modified to match vanilla semantics and are now gold-colored

### Fixed

- [API] Fix possible thread safety issues [#1](https://github.com/TheIllusiveC4/Curios/issues/1)

## [0.6] - 2019.03.07

### Changed

- [API] CuriosAPI#getCurioEquipped methods now return data about the ItemStack as well

## [0.5] - 2019.03.07

### Fixed

- Fixed some syncing issues

## [0.4] - 2019.03.07

### Added

- [API] Added filtered CuriosAPI#getCurioEquipped method

### Changed

- Now able to access curios in Creative mode

## [0.3] - 2019.03.05

### Added

- [API] Added ICurio#getSyncTag and ICurio#readSyncTag(NBTTagCompound) for additional data syncing
- [API] Re-added ICurio#onEquipped(String, EntityLivingBase) and ICurio#onUnequipped(String, EntityLivingBase) methods
- [API] Added RenderHelper for holding helpful rendering utility methods
- [API] Change return of some CurioRegistry methods to be immutable to prevent access to certain registry data
- Added crown item

### Changed

- [API] Changed CurioAPI#getCurioEquippedOfType(String, Item, EntityLivingBase) to a more robust
  CurioAPI#getCurioEquipped(Item, EntityLivingBase) that returns more data about the found stack
- Modified speed bonus on ring item

### Fixed

- Fixed inverted tracking sync checks
- Fixed dedicated server crashes

## [0.2] - 2019.02.28

### Added

- [API] Added ICurio#playEquipSound(EntityLivingBase) method

### Removed

- [API] Removed ICurio#onEquipped(String, EntityLivingBase) and ICurio#onUnequipped(String, EntityLivingBase) methods

## [0.1] - 2019.02.24

Initial beta release
