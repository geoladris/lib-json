# Change Log
This project adheres to [semantic versioning](http://semver.org/).

## [Unreleased]

## [1.1.2] [2016-12-07]

### Added

- Support new date format in `GeoJSONPGHelper`: `yyyy-MM-ddX`.

## [1.1.1] [2016-12-06]

### Fixed

- `GeoJSONPGHelper` not handling dates. Now it parses strings as dates when having the following format: `yyyy-MM-dd'T'HH:mm:ss.SSSX`.

## [1.1.0] [2016-09-06]
### Added
- `GeoJSONPGHelper` to execute SQL `INSERT`/`UPDATE`/`DELETE` queries from GeoJSON objects.

## [1.0.1] [2016-08-10]
### Changed
- Java 7 instead of 8.

## 1.0.0 [2016-08-09]
### Added
- `JSONUtils` utility class for merging JSON objects.
- `JSONContentProvider` for reading `.json` files in a directory.

[Unreleased]: https://github.com/csgis/lib-json/compare/1.1.0...HEAD
[1.1.0]: https://github.com/csgis/lib-json/compare/1.0.1...1.1.0
[1.0.1]: https://github.com/csgis/lib-json/compare/1.0.0...1.0.1

