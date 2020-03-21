# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project does not adhere to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
This project uses MCVERSION-MAJORMOD.MAJORAPI.MINOR.PATCH.

## [2.0.2] - 2020.03.21
### Added
- Added support for curio attribute modifiers using NBT tags, simply replace "AttributeModifiers" with "CurioAttributeModifiers"

## [2.0.1] - 2020.03.08
### Added
- [API] ICurio#onCurioAnimate - Called every tick client-side only while curio is equipped

## [2.0] - 2020.02.24
### Fixed
- Fixed compatibility issue with Ensorcellation's Soulbound enchantment [#41](https://github.com/TheIllusiveC4/Curios/issues/41)

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