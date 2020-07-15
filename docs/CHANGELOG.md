# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project does not adhere to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
This project uses MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH.

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

    