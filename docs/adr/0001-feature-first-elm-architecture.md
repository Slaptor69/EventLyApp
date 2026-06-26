# ADR 0001: Feature-first ELM architecture

## Status

Accepted.

## Context

The app contains several user-facing areas: app navigation, news, tasks, notes, settings, calendar, and the home summary. Keeping all screens, state changes, models, and networking in a flat `ui`, `model`, and `data` structure makes the project harder to verify and extend. The final coursework criteria also require separated modules/components, clear responsibility boundaries, and testable business logic.

The UI must stay simple: it should render state and send user intent upward. Business state transitions should be deterministic and testable without Android UI.

## Decision

Use a feature-first package structure:

- `core`: reusable primitives such as ids and network interfaces.
- `features/<feature>/domain`: pure feature models and business rules.
- `features/<feature>/data`: repositories, cache, remote data sources.
- `features/<feature>/presentation`: Compose screens and ViewModels.
- `di`: Dagger graph assembly.

The presentation layer follows an Elm-style architecture:

- `State` / `Model`: immutable data classes that fully describe the current feature state.
- `Msg`: sealed user/system messages that describe what happened.
- `Reducer` / `Update`: pure functions that transform `State + Msg` into a new `State`.
- `Command`: side-effect requests created by reducers and executed by ViewModels.
- `Effect`: one-shot outputs for UI reactions such as refresh errors or applied settings.
- `ViewModel`: thin Elm runtime that owns `StateFlow`, accepts messages, executes commands, and publishes effects.

The current Elm slices are:

- `features/app/domain`: app navigation state, selected tab, edited task/note, planner messages, planner commands, and reducers.
- `features/news/domain`: news feed state transitions, cache loading commands, refresh commands, auto-refresh command, and refresh-error effects.
- `features/settings/domain`: theme selection message, persistence command, and theme-applied effect.

Compose screens keep only local form/dialog state. Persistent screen state, navigation state, and business actions live in Elm reducers and ViewModels. Reducers are covered by local JVM tests.

## Consequences

The app is easier to grow feature by feature. Dependencies are assembled in one Dagger graph instead of being created inside composables. UI code no longer owns task, note, settings, news, or navigation mutations, so those rules can be tested without Android UI.

Side effects are visible in the architecture. For example, adding a task first emits `PlannerCommand.GenerateTaskId`; loading news emits `NewsCommand.LoadCachedNews` and `NewsCommand.RefreshNews`; selecting a theme emits `SettingsCommand.SaveThemePreference`.
