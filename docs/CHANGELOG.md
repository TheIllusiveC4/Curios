# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

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

    