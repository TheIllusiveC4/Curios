# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [1.16.5-4.0.5.0] - 2021.03.07
### Added
- `ICurio#onEquip(SlotContext, ItemStack)`
- `ICurio#onUnequip(SlotContext, ItemStack)`
- `ICuriosHelper#isStackValid(SlotContext, ItemStack)`
### Changed
- Items that are invalidated while in a slot, due to modified tags or changed curio behavior, will 
  now automatically eject from its slot and be given to the player
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
- `ICurioItem#playRightClickEquipSound(LivingEntity, ItemStack)` [#102](https://github.com/TheIllusiveC4/Curios/issues/102)
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
- Fixed render buttons toggling wrong slot [#75](https://github.com/TheIllusiveC4/Curios/issues/75) [#84](https://github.com/TheIllusiveC4/Curios/issues/84)

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
- Fixed crashing when attempting to unlock/lock slots in equip handlers [#68](https://github.com/TheIllusiveC4/Curios/issues/68)
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
- Fixed Curios button disappearing when switching tabs in Creative menu [#55](https://github.com/TheIllusiveC4/Curios/issues/55)
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
    - CuriosApi split into three helper interfaces (IIconHelper on the client, ISlotHelper on the server, ICuriosHelper for both)
- CurioTags functionality refactored into SlotTypePresets
- The above changes are only for the API, the rest of the classes have also had extensive changes

    