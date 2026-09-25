# Branch Index

`main` = baseline only (never receives feature merges).
Slice branches merge into feature integration branches with `--no-ff`.

| # | Slice branch | Layer | Scope | Merged into |
|---|--------------|-------|-------|-------------|
| 001 | `001-project-scaffold` | both | Setup | `001-support-tickets` |
| 002 | `002-backend-foundation` | backend | Foundation | `001-support-tickets` |
| 003 | `003-backend-us1-auth` | backend | US1 | `001-support-tickets` |
| 004 | `004-frontend-foundation` | frontend | Foundation | `001-support-tickets` |
| 005 | `005-frontend-us1-auth` | frontend | US1 | `001-support-tickets` |
| 006 | `006-backend-us2-ticket-list` | backend | US2 | `001-support-tickets` |
| 007 | `007-frontend-us2-ticket-list` | frontend | US2 | `001-support-tickets` |
| 008 | `008-backend-us3-ticket-detail` | backend | US3 | `001-support-tickets` |
| 009 | `009-frontend-us3-ticket-detail` | frontend | US3 | `001-support-tickets` |
| 010 | `010-backend-us4-transitions` | backend | US4 | `001-support-tickets` |
| 011 | `011-frontend-us4-transitions` | frontend | US4 | `001-support-tickets` |
| 012 | `012-backend-us5-comments` | backend | US5 | `001-support-tickets` |
| 013 | `013-frontend-us5-comments` | frontend | US5 | `001-support-tickets` |
| 014 | `014-backend-us6-search-filter` | backend | US6 | `001-support-tickets` |
| 015 | `015-frontend-us6-search-filter` | frontend | US6 | `001-support-tickets` |
| 016 | `016-polish-integration` | both | Polish | `001-support-tickets` |
| 017 | `017-backend-ticket-attachments` | backend | 002 | `002-ticket-attachments` |
| 018 | `018-frontend-ticket-attachments` | frontend | 002 | `002-ticket-attachments` |

**`001-support-tickets`** — integration branch for support tickets (16 slices).  **`002-ticket-attachments`** — integration branch for attachments (2 slices).  **`main`** — baseline scaffold only.
