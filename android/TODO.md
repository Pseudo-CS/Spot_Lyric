# TODO

Keep this file updated as tasks are added, started, or completed.

## Lyrics extraction & translation workflow improvements
- [x] Improve source search ranking (dual queries, dedupe, domain reliability scoring).
- [x] Implement a multi-stage extraction pipeline (domain parsers → heuristics → Gemini fallback) with auto-try-next-source.
- [x] Adjust translation workflow (skip when translation exists or language is English; on-demand AI; chunk long lyrics).
- [x] Persist extraction metadata (domain, stage, confidence) and surface it in Player/Lyrics UI with retry.

## Data management
- [x] Add backup creation and import feature to export and import all user data.
