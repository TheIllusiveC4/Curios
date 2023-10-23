The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).
Prior to version 5.2.0, this project used [Forge Recommended Versioning](https://mcforge.readthedocs.io/en/latest/conventions/versioning/).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/TheIllusiveC4/Curios/blob/1.20.x/docs/CHANGELOG.md).

## [5.4.0+1.20.1] - 2023.10.23
### Added
- Added `replace` fields to entity files to clear previously assigned slots to entities
### Changed
- Using set operations in slot files with `replace` set to true will now reset previous add and remove operations
### Fixed
- Fixed `replace` fields not working properly for slot loading, this may cause current slot configurations to change when
  updating
