# Changelog

## 2026-05-30 (stage 1 parsers update)
- **New Stage 1 Domain Parsers**: Implemented native on-device domain-specific parsers for `lyricsdecoder.com` and `bollymeaning.com` (also supporting `bollywoodmeaning.com`).
  - **LyricsDecoder Parser**: Extracts original lyrics from the `.md:text-center.text-lg` container and dynamically retrieves English translations by scanning paragraphs following the "Meaning in English" or "Translation" `h2` headings until a terminating section is encountered.
  - **BollyMeaning Parser**: Traverses the Blogger post body elements to separate original verses (bolded `<b>` tags) and English translations (sequential sibling text nodes/paragraphs) on a stanza-by-stanza basis.
- **Unit Tests**: Added robust HTML fixture tests verifying full accuracy of these parsers under `testLyricsDecoderExtraction` and `testBollyMeaningExtraction`.
- **Sources Overview Screen**: Added a new metrics screen accessible from Settings showing overall and domain-specific extraction statistics (bookmark count, success rate, stage breakdown, translation coverage, and average confidence) for all imported song sources.

## 2026-05-30 (bugfix, extraction update & docs)
- **Strategy B AI Fallback**: Simplified the Gemini extraction text cleaning pipeline. Instead of relying on fragile CSS class keyword matching (which caused false positives on utility classes like `text-gray-800` and stripped actual lyrics), the cleaner now strips structural noise and directly sends the full text body fallback to Gemini.
- **Backup/Import Stream Lifecycle Fix**: Fixed a bug in `SettingsScreen` where the file Input/Output streams were closed prematurely via Kotlin's `.use` block before the asynchronous coroutines could read/write backup data. The streams are now safely closed inside the async callbacks after execution completes.
- **Extraction Behavior**: Removed automatic fallback/shifting to subsequent candidate sources during extraction in `PlayerViewModel`. The app now runs the multi-stage extraction pipeline (Stage 1 -> Stage 2 -> Stage 3) strictly on the user-selected source.
- **Monorepo Documentation**: Created a detailed, comprehensive [README.md](file:///home/pseudo/work/README.md) at the root of the project.

## 2026-05-30 (backup & import implementation)
- **Backup & Import Service**: Introduced [BackupRepository](file:///home/pseudo/work/spotlyric-android/app/src/main/java/com/spotlyric/app/domain/repository/BackupRepository.kt) interface and [BackupRepositoryImpl](file:///home/pseudo/work/spotlyric-android/app/src/main/java/com/spotlyric/app/data/repository/BackupRepositoryImpl.kt) implementation to handle complete JSON-based serialization/deserialization of user data.
- **Natural-Key Mapping**: JSON schema structures backup details (bookmarks, lyrics, preferred sources, and settings) cleanly and maps lyrics back to bookmarks using natural keys (`songName`, `artistName`) to prevent database primary key clashes on different devices.
- **Data Management UI**: Created a new **DATA MANAGEMENT (BACKUP & IMPORT)** card inside the [SettingsScreen](file:///home/pseudo/work/spotlyric-android/app/src/main/java/com/spotlyric/app/presentation/settings/SettingsScreen.kt) with "Export Backup" and "Import Backup" actions, loading indicators, and Toast feedback, using Android Storage Access Framework (SAF) so that no storage permissions are requested.
- **Automatic Migration Backups**: Integrated an interceptor in [DatabaseModule.kt](file:///home/pseudo/work/spotlyric-android/app/src/main/java/com/spotlyric/app/di/DatabaseModule.kt) that checks the database version prior to initialization. If a schema version mismatch is found, it automatically serializes the database contents to a JSON backup file in internal storage (`context.filesDir`). Upon destructive migration, Room's `onDestructiveMigration` callback automatically parses this file and restores all bookmarks, cached lyrics, and preferred sources, guaranteeing zero user data loss on any future schema updates.

## 2026-05-30 (lyrics workflow improvements)
- **Search ranking**: `LyricsSearchService` now fires two concurrent SerpAPI queries per search (e.g. "lyrics translation" + "lyrics english translation"), merges and deduplicates results by normalised URL, and ranks them by a composite score (preferred domain +2.0, Tier 1 reliable domains +1.0, Tier 2 directories +0.5, unreliable domains −0.5, plus token-overlap relevance).
- **Multi-stage extraction pipeline**: `LyricsExtractorService` gained `ExtractionResult` (originalLyrics, translatedLyrics, stage, confidence), Stage 1 domain parsers for Genius (`[data-lyrics-container]`) and LyricsRaag (`span.original`/`span.translated`), and Stage 2 CSS-class heuristics (`.lyrics`, `.lyric-content`, `.song-lyrics`, `.entry-content`). `PlayerRepositoryImpl.extractAndTranslateLyrics` now tries Stage 1 → Stage 2 → Stage 3 (Gemini AI) in order.
- **Auto-fallback across sources**: `PlayerViewModel.extractAndTranslate` automatically tries subsequent ranked sources when one fails, showing which URL is currently being attempted.
- **Conditional AI translation**: AI translation is now skipped when (a) the extracted content already contains a non-blank translation (e.g. LyricsRaag bilingual pages) or (b) the original language is detected as English.
- **Chunked translation**: `GeminiLyricsService.generateAiTranslationChunked` splits songs longer than 80 lines into ~40-line chunks (honouring stanza boundaries) and calls Gemini sequentially to avoid response truncation.
- **Extraction metadata**: `SongLyricsEntity` and `Lyrics` now carry `sourceUrl`, `extractionStage`, `confidenceScore`, and `originalLanguage`. `SongLyricsDao` gained `deleteByBookmarkId` to replace stale lyrics on re-extraction. Database version bumped to 3 (with `fallbackToDestructiveMigration`).
- **Lyrics UI source badge**: `LyricsScreen` shows a subtle `"Source: domain  •  Stage"` label beneath the TopAppBar.
- **Switch Source dialog**: A new "Switch Source" (⇕) button in the Lyrics TopAppBar opens a dialog listing the ranked search results; selecting one re-extracts lyrics via the full pipeline and updates the source badge.

## 2026-05-30
- Consolidated documentation to `README.md`, `CHANGELOG.md`, and `TODO.md`.
- Added the lyrics extraction/translation improvement plan and backup/export-import task to `TODO.md`.
- Added `AI_AGENT_INSTRUCTIONS.md` and moved AI requirements out of `README.md`.
- Fixed HTML noise stripping in `LyricsExtractorService` to support both layouts of `lyricsraag.com` and avoid extracting related song lists.
- Added a custom Gemini Model text input field in Settings and wired it to `GeminiLyricsService`.
- Adjusted lyrics autoscroll speeds to `1.5x` (37.5 px/s) and `2.5x` (62.5 px/s) in `LyricsScreen`.
- Created and revised the implementation plan artifact for the lyrics extraction & translation workflow improvements (detailing user custom/preferred sources integration).
- Created a `plans/` folder and saved the implementation plan as `lyrics_workflow_improvements.md`.
- Updated `AI_AGENT_INSTRUCTIONS.md` to instruct agents to delete plans from `plans/` once all associated tasks are executed and marked off in `TODO.md`.
