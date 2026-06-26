# ADR 0002: News cache policy

## Status

Accepted.

## Context

The news feed must load remote NYT articles, refresh periodically, and use cached data at least once. The UI should not block while the network request is in progress, and the cache should have a clear cleanup strategy.

## Decision

The app keeps the latest successful news snapshot in SQLite for 24 hours. On screen start, the ViewModel reads the cached snapshot first and renders it immediately when available. Then it refreshes from the network and replaces the cached metadata.

Images are cached separately in the app cache directory. File names are derived from the image URL hash. Reused images are kept while fresh; unused files are removed after a successful metadata refresh. If the metadata cache expires, both metadata and cached images are cleared.

## Consequences

The feed opens quickly when a previous successful snapshot exists, survives temporary network failures, and avoids unbounded cache growth. The policy is intentionally simple for coursework review: one TTL, one SQLite table, one image cache directory.
