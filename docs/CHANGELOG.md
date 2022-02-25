# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

## [1.16.5-4.0.8.1] - 2022.02.25
### Changed
- Updated `ko_kr.json` localization (thanks PixVoxel!) [#223](https://github.com/TheIllusiveC4/Curios/pull/223)
### Fixed
- Fixed curios being unequipped when used with additional slots and relogging [#218](https://github.com/TheIllusiveC4/Curios/issues/218)
- Fixed NPE crash with certain mods that implement their own Curios providers [#225](https://github.com/TheIllusiveC4/Curios/issues/225)

## [1.16.5-4.0.8.0] - 2021.01.18
### Added
- Added `uk_ua.json` localization (thanks Sushomeister!)
- Added new entity selector option, `curios=`. More information at the [wiki](https://github.com/TheIllusiveC4/Curios/wiki/Commands#entity-selector-options).
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
- Fixed crash when running data generation in development environments [#188](https://github.com/TheIllusiveC4/Curios/issues/188)

## [1.16.5-4.0.6.3] - 2021.12.01
### Fixed
- Fixed slots not being loaded correctly in some situations
- Fixed slot size desyncs when players have more than one of any slot type [#185](https://github.com/TheIllusiveC4/Curios/issues/185)
- Fixed potential crash on dedicated servers [#184](https://github.com/TheIllusiveC4/Curios/issues/184)

## [1.16.5-4.0.6.2] - 2021.11.30
### Fixed
- Fixed resource loading crashing when invalid texture files are found [#183](https://github.com/TheIllusiveC4/Curios/issues/183)

## [1.16.5-4.0.6.1] - 2021.11.30
### Fixed
- Fixed syncing slot shrinking client-side, actually this time

## [1.16.5-4.0.6.0] - 2021.11.30
Please note that this update is more experimental than most and is marked as a beta. Be cautious about adding this to
stable worlds.
### Added
- [API] Added slot modifier system, see [the wiki page](https://github.com/TheIllusiveC4/Curios/wiki/Slot-Modifiers) for
more info. This is the main reason the update is experimental. Although this system was designed to be backwards
compatible, be wary of issues with older slot modification methods. [#178](https://github.com/TheIllusiveC4/Curios/issues/178)
- [API] Added `CurioEquipEvent` and `CurioUnequipEvent` to allow modders the ability to intercept and change
equip/unequip results [#174](https://github.com/TheIllusiveC4/Curios/issues/174)
- [API] Added new slot texture registration method: textures located in the `assets/curios/textures/slot` directory in
any mod or resource pack will be automatically stitched to the texture atlas and usable by slots [#145](https://github.com/TheIllusiveC4/Curios/issues/145)
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
- Fixed class-loading errors by annotating curio render methods with `OnlyIn(Dist.CLIENT)` [#121](https://github.com/TheIllusiveC4/Curios/issues/121)

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
behavior may cause lingering items but fixes issues with invalidating curios from various mods [#124](https://github.com/TheIllusiveC4/Curios/issues/124)
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

    