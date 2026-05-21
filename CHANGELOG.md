# Changelog
## [1.1.0] - 

### Added
- Tile Entity render range configuration (Default 128)
- Unsubscribe button in the EM Rail Ejector and Ray Receiver GUIs, allowing players to return to their personal Dyson Sphere
- FML Update Check through CurseUpdate API.

### Changed
- [Align with original project simplified chinese translation#5 by ZHAY10086](https://github.com/Refitbench/Dyson-Cube-Project/pull/5)
- Beam and current sail counts in the GUI now show full numbers instead of abbreviated values

### Fixed
- A number of hardcoded english strings, now uses langkeys.
- Deduplicated WritableEnergyStorage method, moved to util.
- Unify loggers to use mod logger methods.
- Number Util to use static final.
- Off-by-one: the last batch of solar sails could never be submitted due to `>=` instead of `>` in the max-sails check
- MaxSolarPanels no longer exceeds Config.MAX_SOLAR_PANELS when many beams are present
- MaxBeams now uses ceiling division so the beam cap is never under-counted
- "Needs more beams" message no longer appears when the sphere has actually reached its final sail cap.

## [1.0.3] - 2026-04-22
    
    Initial Release


- Follows the [KeepAChangelog Convention](https://keepachangelog.com/en/1.1.0/)