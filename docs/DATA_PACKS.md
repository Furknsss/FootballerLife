# Football Life World DataPacks

## Storage and compatibility

DataPacks use a separate Room file (`football_life_world_data.db`) and the `WorldDataDatabase` schema. Career snapshots remain in `football_life_career.db`; importing a pack never migrates, rewrites, deletes, or automatically refreshes an existing career. A newly started career captures the selected `packId@version` key in `selectedDataPackKey`, so the source version is auditable and does not silently drift. DataPack records are retained in the local world database by `(packId@version, module, internalId)`; multiple versions of one logical pack can be installed side by side.

The first implementation stores normalized pack metadata, typed module names, stable IDs, parent IDs, JSON payloads, per-record source/date/missing fields, external ID mappings, media paths/checksums, and import issues. Supported module names are `Leagues`, `Clubs`, `Players`, `Squads`, `Coaches`, `Contracts`, `Transfers`, `PlayerAttributes`, `PlayerValuations`, `ClubLogos`, `PlayerPortraits`, and `Kits`.

## JSON format

The import screen accepts a document shaped like:

```json
{
  "metadata": {
    "packId": "example-country-2026-27",
    "season": "2026/27",
    "snapshotDate": "2026-11-14",
    "source": "Named source and URL",
    "version": "1.0.0",
    "checksum": "64 lowercase hexadecimal SHA-256 characters"
  },
  "records": [
    {
      "module": "Leagues",
      "internalId": "country-league-id",
      "parentInternalId": null,
      "source": "Source for this row",
      "validatedAt": "2026-11-14",
      "missingValues": [],
      "data": { "name": "League name", "country": "Country" }
    }
  ],
  "mediaAssets": [],
  "externalIds": []
}
```

The checksum is SHA-256 of compact canonical JSON for `{ "records": [...], "mediaAssets": [...] }`: object keys sort recursively in ascending Unicode order, arrays keep order, strings use JSON escaping, numbers use plain decimal without insignificant zeroes. Metadata itself is excluded so the checksum can be generated before inserting it. This detects record/asset manifest changes; ZIP media bytes additionally have an individual SHA-256 in each `mediaAssets` entry.

CSV uses a short metadata preamble (`#packId=...`, `#season=...`, `#snapshotDate=...`, `#source=...`, `#version=...`, `#checksum=...`) followed by RFC-4180 CSV with these columns: `module,internalId,parentInternalId,source,validatedAt,missingValues,dataJson`. `missingValues` is a `|`-separated list; `dataJson` contains a JSON object. The parsed record content uses the same canonical checksum rule as JSON.

ZIP files contain `datapack.json` (or `manifest.json`) at the archive root, plus optional files under `media/`. A media descriptor names the exact safe relative path and owning `module`/`internalId`/kind. ZIP traversal, duplicate paths, oversized inputs/expanded archives, missing assets, and asset checksum mismatches are rejected before writing.

## Validation and reports

The parser rejects duplicate IDs, unknown modules, missing module-required values, broken internal-ID relationships, malformed source/verification dates, invalid metadata, checksum mismatches, and malformed or untrusted media references. It returns one report row for each of the 12 modules with row count, missing-value rate, unmatched IDs and errors, plus package issues. A failed import is recorded in the import issue table but installs no pack records. Re-importing an identical `(packId, version, checksum)` is idempotent; changed content under the same `(packId, version)` is rejected rather than replacing installed data. A new version can be installed beside the older one.

Clubs and players must have stable source-independent internal IDs. External identifiers (for example a provider-specific ID) go in `externalIds` and are mapped to these IDs; they never replace primary keys. Media paths are stored with the owning IDs. `DataPackMediaImage` resolves and decodes only the requested visible image, verifies its bytes, and uses the same person/football icon fallback if it is missing or invalid.

## Verified example

`data-packs/examples/tff-super-lig-2026-27-sample.json` and the ZIP counterpart form a deliberately small real-data fixture. It contains the 2026/27 TFF competition plus the two clubs named in a TFF first-week fixture. It has no player records, valuations, kits, logos, or portrait images. The rows preserve the TFF source and 2026-11-14 verification date. This fixture validates import mechanics, not broad league coverage. The official source is the [TFF 2026-2027 Süper Lig fixture list](https://www.tff.org/Default.aspx?macId=283594&pageId=198).

## Current scope boundary

Packs install and are selectable as an immutable new-career source version. The existing career setup's playable league/club generator still reads the legacy `WorldDataCatalog`; this first data foundation does not synthesize game attributes, staff, budgets, squads, transfers, assets, or players from sparse imported rows. Full data-driven career creation requires a verified source pack with the complete gameplay fields and a follow-up adapter/selection UI. No player or image data was invented to fill this gap. Public world-wide source acquisition, rights review, update/delta workflow, and bulk provider reconciliation remain separate verification steps.

