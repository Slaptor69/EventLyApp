# Navigation and Architecture Principles

Этот документ нужен для защиты проекта: где находится навигация, как она устроена, и где в коде видно соблюдение SOLID, KISS и DRY.

## 1. Где находится навигация

В проекте нет отдельного `NavHost` из Navigation Compose. Навигация сделана через ELM-состояние фичи `app`.

Главные файлы:

- `src/main/java/com/example/eventlyapp/features/app/domain/AppScreen.kt` - enum всех полноэкранных экранов.
- `src/main/java/com/example/eventlyapp/features/app/domain/MainTab.kt` - enum вкладок внутри главного экрана.
- `src/main/java/com/example/eventlyapp/features/app/domain/AppState.kt` - текущее навигационное состояние.
- `src/main/java/com/example/eventlyapp/features/app/domain/AppMsg.kt` - события навигации.
- `src/main/java/com/example/eventlyapp/features/app/domain/AppReducer.kt` - чистая логика переходов.
- `src/main/java/com/example/eventlyapp/features/app/presentation/AppViewModel.kt` - принимает `AppMsg` и обновляет `AppState`.
- `src/main/java/com/example/eventlyapp/features/app/presentation/EventlyAppRoot.kt` - выбирает, какой экран показать.
- `src/main/java/com/example/eventlyapp/features/app/presentation/MainTabsScreen.kt` - рисует верхние вкладки.

## 2. Как устроена навигация

`AppScreen` описывает крупные экраны:

```kotlin
enum class AppScreen {
    MAIN_TABS,
    CREATE_TASK,
    EDIT_TASK,
    CREATE_NOTE,
    EDIT_NOTE,
    TASK_CALENDAR,
    SETTINGS
}
```

`MainTab` описывает вкладки внутри `MAIN_TABS`:

```kotlin
enum class MainTab(val title: String) {
    NEWS("Новости"),
    HOME("Главная"),
    TASKS("Задачи"),
    NOTES("Записи")
}
```

`AppState` хранит:

- `currentScreen` - какой экран сейчас открыт;
- `currentMainTab` - какая вкладка выбрана;
- `editingTask` - какую задачу редактируем;
- `editingNote` - какую запись редактируем.

`AppMsg` описывает действия пользователя:

- `MainTabSelected`;
- `AddTaskClicked`;
- `AddNoteClicked`;
- `SettingsClicked`;
- `CalendarClicked`;
- `EditTaskClicked`;
- `EditNoteClicked`;
- `MainScreenRequested`.

`AppReducer` получает старый `AppState` и `AppMsg`, возвращает новый `AppState`. Например:

```text
AppMsg.SettingsClicked
  -> currentScreen = AppScreen.SETTINGS
```

```text
AppMsg.AddTaskClicked
  -> currentScreen = AppScreen.CREATE_TASK
  -> editingTask = null
```

```text
AppMsg.EditTaskClicked(task)
  -> currentScreen = AppScreen.EDIT_TASK
  -> editingTask = task
```

В `EventlyAppRoot` есть `when (appState.currentScreen)`. Именно там выбранное состояние превращается в конкретный Compose-экран:

```text
AppScreen.MAIN_TABS -> MainTabsScreen(...)
AppScreen.CREATE_TASK -> TaskCreateScreen(...)
AppScreen.EDIT_TASK -> TaskCreateScreen(...)
AppScreen.CREATE_NOTE -> NoteCreateScreen(...)
AppScreen.EDIT_NOTE -> NoteCreateScreen(...)
AppScreen.TASK_CALENDAR -> TaskCalendarScreen(...)
AppScreen.SETTINGS -> SettingsScreen(...)
```

## 3. Пример перехода в настройки

```text
MainTabsScreen
  -> onSettingsClick
  -> appViewModel.accept(AppMsg.SettingsClicked)
  -> AppReducer.update(...)
  -> AppState(currentScreen = SETTINGS)
  -> EventlyAppRoot
  -> SettingsScreen
```

## 4. Пример создания задачи

```text
MainTabsScreen
  -> onAddTaskClick
  -> AppMsg.AddTaskClicked
  -> AppState(currentScreen = CREATE_TASK)
  -> EventlyAppRoot shows TaskCreateScreen
  -> user presses save
  -> PlannerMsg.AddTaskRequested
  -> AppMsg.MainScreenRequested
  -> AppState(currentScreen = MAIN_TABS)
```

Важно: `App` отвечает только за переходы. Реальное добавление задачи относится к `PlannerMsg`, `PlannerReducer`, `PlannerCommand` и `PlannerRepository`.

## 5. SOLID в проекте

### S - Single Responsibility Principle

Принцип в целом соблюден: классы разделены по ролям.

Примеры:

- `AppReducer` отвечает только за навигационное состояние.
- `PlannerReducer` отвечает за правила изменения задач и записей.
- `PlannerRepository` отвечает за сценарии работы с Room.
- `PlannerLocalMapper` отвечает только за преобразование Room entity в domain model и обратно.
- `NewsRepository` объединяет remote data source, metadata cache, image cache и форматирование дат.
- `SettingsRepository` отвечает за настройки и очистку news cache.

Хороший пример: `PlannerReducer` не знает про Room, а `PlannerRepository` не решает UI-навигацию.

### O - Open/Closed Principle

Соблюден частично и практично для ELM-проекта.

Проект легко расширять новой фичей: можно добавить отдельные `State`, `Msg`, `Command`, `Effect`, `Reducer`, `ViewModel`, `Repository`, не переписывая существующие фичи.

Пример: news, planner и settings имеют отдельные ELM-срезы. Добавление очистки кэша новостей расширило settings-срез через новые `SettingsMsg`, `SettingsCommand`, `SettingsEffect`, не затрагивая `NewsReducer`.

Ограничение: sealed-классы и `when` всё равно требуют добавить новую ветку в reducer. Это нормально для ELM, потому что reducer должен явно знать все события своей фичи.

### L - Liskov Substitution Principle

В проекте этот принцип виден на уровне Android-контрактов.

`SingleViewModelFactory<T>` реализует `ViewModelProvider.Factory`, а конкретные фабрики:

- `AppViewModelFactory`;
- `PlannerViewModelFactory`;
- `NewsViewModelFactory`;
- `SettingsViewModelFactory`;

могут использоваться Android API как обычные `ViewModelProvider.Factory`. То есть код, который ожидает фабрику ViewModel, получает любую из этих реализаций без изменения поведения контракта.

### I - Interface Segregation Principle

Соблюден частично через маленькие специализированные API.

Примеры:

- `NytTopStoriesService` описывает только NYT-запрос.
- `DebugApiService` описывает только debug POST.
- `PlannerDao` содержит только операции локального планера.
- `SettingsCommand` не смешан с командами planner или news.
- `MainTabsScreen` принимает конкретные callbacks, а не один большой объект управления всем приложением.

Ограничение: repository в проекте в основном concrete classes, а не интерфейсы. Для учебного проекта это нормально, но если бы требовалась более строгая ISP/DIP-архитектура, можно было бы вынести интерфейсы `PlannerRepository`, `NewsRepository`, `SettingsRepository`.

### D - Dependency Inversion Principle

Принцип хорошо виден через constructor injection и Dagger.

Примеры:

- `PlannerViewModel` получает `PlannerReducer` и `PlannerRepository` через конструктор.
- `NewsViewModel` получает `NewsRepository` и `NewsReducer` через конструктор.
- `SettingsViewModel` получает `SettingsRepository` и `SettingsReducer` через конструктор.
- `AppComponent` собирает зависимости и отдаёт `ViewModelFactory`.
- `AppModule` и `NetworkModule` создают инфраструктурные зависимости: `SharedPreferences`, Room database, DAO, Retrofit, OkHttp.

Важный плюс: ViewModel не создаёт Room, Retrofit или repository вручную. Это делает код проще тестировать и менять.

## 6. KISS в проекте

KISS означает: решение должно быть настолько простым, насколько это возможно для задачи.

Где это видно:

- Навигация сделана через `AppScreen`, `AppState`, `AppReducer` и `when`, без сложного navigation graph.
- `ElmUpdate` - маленький общий контейнер: `state`, `commands`, `effects`.
- `AppReducer` возвращает `ElmUpdate<AppState, Nothing, Nothing>`, потому что navigation app-срезу не нужны side effects.
- `PlannerSnapshot` содержит только `tasks` и `notes`.
- Настройки темы хранятся в `SharedPreferences`, а не в Room, потому что это одно простое значение.

Пример для защиты:

> Навигация в проекте простая: состояние `AppState` говорит, какой экран открыт, reducer меняет это состояние, а `EventlyAppRoot` по нему выбирает Compose-экран. Для текущего масштаба приложения этого достаточно и понятнее, чем отдельный сложный navigation graph.

## 7. DRY в проекте

DRY означает: не повторять одну и ту же логику в разных местах.

Примеры:

- `SingleViewModelFactory` убирает повторяющийся код `ViewModelProvider.Factory` из всех ViewModel-фабрик.
- `ElmUpdate` и `toElmUpdate()` дают единый формат результата reducer-а.
- `PlannerLocalMapper` хранит преобразования `TaskData <-> PlannerTaskEntity`, `NoteData <-> PlannerNoteEntity` в одном месте.
- `MainTab.entries.forEach` строит вкладки из enum, а не из вручную продублированного списка.
- `TaskCreateScreen` используется и для создания, и для редактирования задачи через `initialTask`.
- `NoteCreateScreen` используется и для создания, и для редактирования записи через `initialNote`.

## 8. Что можно сказать коротко на защите

> Навигация в проекте построена как часть ELM-архитектуры. `AppState` хранит текущий экран и вкладку, `AppMsg` описывает действия пользователя, `AppReducer` меняет состояние, а `EventlyAppRoot` по этому состоянию выбирает нужный Compose-экран. Бизнес-действия вроде создания задач не лежат в navigation-срезе: они передаются в `PlannerMsg`.

> SOLID соблюден в практическом виде: reducer, ViewModel, repository, mapper и UI имеют разные ответственности; зависимости передаются через конструкторы и Dagger; маленькие API не смешивают разные фичи. KISS виден в простой навигации через state и enum, а DRY - в `SingleViewModelFactory`, `ElmUpdate`, mapper-ах и переиспользовании create/edit экранов.
