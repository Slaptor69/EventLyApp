# ADR 0003: Planner Room Persistence

## Status

Accepted.

## Context

Tasks and notes were previously stored only in `PlannerViewModel` memory. This survives many configuration changes, but does not survive a full process death, app restart, or device reboot. The app needs local persistence while preserving the Elm-style architecture.

## Decision

Use Room as the local persistence layer for planner data:

- `PlannerDatabase`: Room database for tasks, subtasks, and notes.
- `PlannerDao`: local database access.
- `PlannerRepository`: maps Room entities to domain models and exposes planner persistence operations.
- `PlannerCommand`: side-effect requests such as `LoadPlannerSnapshot`, `SaveTask`, `DeleteTask`, `SaveNote`, and `DeleteNote`.

Reducers stay pure. They never call Room directly. They only return commands. `PlannerViewModel` acts as the Elm runtime: it executes persistence commands through `PlannerRepository` and sends loaded data back into the reducer with `PlannerMsg.SnapshotLoaded`.

## Consequences

Tasks and notes are restored when the app starts again after process death or a normal app restart. ELM boundaries stay intact because database work remains outside reducers and UI composables.
