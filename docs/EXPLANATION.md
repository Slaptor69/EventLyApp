# EventLyApp: пояснение архитектуры, технологий и потока данных

## 1. Что это за приложение

`EventLyApp` - Android-приложение на Kotlin и Jetpack Compose. В нём есть:

- лента новостей NYTimes;
- главный экран со статистикой;
- задачи с дедлайнами, приоритетами, флажком и подзадачами;
- записи;
- календарь задач;
- настройки темы;
- локальное сохранение задач/записей через Room;
- кэш новостей через Room и файловый кэш изображений.

Проект сделан в feature-first структуре и использует ELM-style архитектуру: экран отправляет сообщения, reducer чисто пересчитывает состояние, а side effects выполняются через команды во ViewModel-runtime.

## 2. Использованные технологии

| Технология | Где используется | Зачем нужна |
|---|---|---|
| Kotlin | весь проект | основной язык приложения |
| Jetpack Compose | `features/*/presentation` | декларативный UI |
| Material 3 | UI-компоненты | кнопки, карточки, диалоги, темы |
| ViewModel | `features/*/presentation/*ViewModel.kt` | lifecycle-aware runtime для ELM |
| Kotlin Coroutines | ViewModel, repositories, cache services | асинхронная работа с сетью, Room и файлами |
| StateFlow | ViewModel state | реактивная доставка состояния в Compose |
| SharedFlow | one-shot effects | одноразовые события вроде эффекта ошибки/применения темы |
| Retrofit | `core/network`, `NetworkModule` | сетевые запросы к NYTimes и debug endpoint |
| OkHttp | `NetworkModule` | HTTP-клиент и logging interceptor |
| Gson converter | Retrofit DTO | парсинг JSON |
| Coil | `NewsFeedScreen` | загрузка/показ локальных изображений новостей |
| Room | `features/app/data/local` | сохранение задач, подзадач и записей |
| Room | `features/news/data/cache` | локальный кэш метаданных новостей |
| SharedPreferences | `features/settings/data` | сохранение выбранной темы |
| Dagger 2 | `di` | Dependency Injection graph |
| JUnit | `src/test` | unit-тесты reducer/domain/core |
| Compose UI Test | `src/androidTest` | UI happy-path и snapshot-smoke тест |

## 3. Структура проекта

Основной пакет:

```text
com.example.eventlyapp
```

Ключевые директории:

```text
core/
  elm/
  id/
  network/

di/

features/
  app/
  calendar/
  home/
  news/
  notes/
  settings/
  tasks/

ui/theme/
```

Идея разделения:

- `core` - общие примитивы, которые не принадлежат конкретной фиче.
- `di` - сборка зависимостей Dagger.
- `features/<feature>/domain` - состояние, сообщения, команды, reducer, бизнес-модели.
- `features/<feature>/data` - Room/Retrofit/repository/cache.
- `features/<feature>/presentation` - Compose UI и ViewModel-runtime.
- `ui/theme` - цвета, типографика, Compose-тема.

Это соответствует Clean Architecture в упрощённом варианте:

```text
presentation -> domain -> data
```

UI не должен напрямую ходить в сеть или базу. Бизнес-решения вынесены в reducer/domain. Side effects находятся в data/repository и запускаются через команды.

## 4. ELM-архитектура в проекте

### 4.1. Что такое ELM

В классической ELM-архитектуре есть:

```text
View -> Msg -> Update -> Model + Cmd -> Runtime -> Msg
```

В Android-проекте это адаптировано так:

```text
Compose Screen
  -> Msg
  -> ViewModel as ELM runtime
  -> Reducer.update(state, msg)
  -> new State + Commands + Effects
  -> ViewModel executes Commands
  -> repository / Room / network
  -> result returns as Msg
```

ViewModel здесь не бизнес-слой. Она является Android lifecycle-aware runtime:

- хранит `MutableStateFlow`;
- принимает `Msg`;
- вызывает чистый `Reducer`;
- выполняет `Command`;
- публикует `Effect` через `SharedFlow`, если нужно.

### 4.2. Общий ELM-контейнер

`core/elm/ElmUpdate.kt`

Содержит:

```kotlin
data class ElmUpdate<State, Command, Effect>(
    val state: State,
    val commands: List<Command> = emptyList(),
    val effects: List<Effect> = emptyList()
)
```

Это единый формат ответа reducer:

- новое состояние;
- команды для side effects;
- одноразовые эффекты.

Также есть helper `toElmUpdate(...)`, чтобы удобно превращать State в update.

## 5. App/navigation ELM слой

Пакет:

```text
features/app/domain
features/app/presentation
```

### 5.1. AppState

`features/app/domain/AppState.kt`

Хранит навигационное состояние:

- `currentScreen` - какой экран открыт;
- `currentMainTab` - выбранная вкладка: новости, главная, задачи, записи;
- `editingTask` - задача, которая сейчас редактируется;
- `editingNote` - запись, которая сейчас редактируется.

Раньше это было локальным Compose state, теперь это часть ELM state.

### 5.2. AppScreen

`features/app/domain/AppScreen.kt`

Enum экранов:

- `MAIN_TABS`
- `CREATE_TASK`
- `EDIT_TASK`
- `CREATE_NOTE`
- `EDIT_NOTE`
- `TASK_CALENDAR`
- `SETTINGS`

### 5.3. MainTab

`features/app/domain/MainTab.kt`

Enum вкладок:

- `NEWS`
- `HOME`
- `TASKS`
- `NOTES`

### 5.4. AppMsg

`features/app/domain/AppMsg.kt`

Сообщения навигации:

- `MainTabSelected`
- `AddTaskClicked`
- `AddNoteClicked`
- `SettingsClicked`
- `CalendarClicked`
- `EditTaskClicked`
- `EditNoteClicked`
- `MainScreenRequested`

UI не меняет экран напрямую. Он отправляет `AppMsg`.

### 5.5. AppReducer

`features/app/domain/AppReducer.kt`

Чисто пересчитывает `AppState`. Например:

- `AddTaskClicked` открывает экран создания задачи;
- `EditTaskClicked(task)` открывает экран редактирования и сохраняет `editingTask`;
- `MainScreenRequested` возвращает на главный экран и очищает редактируемую сущность.

Команд и эффектов сейчас нет, поэтому reducer использует `Nothing` для этих типов и не держит пустые классы только ради симметрии.

### 5.6. AppViewModel

`features/app/presentation/AppViewModel.kt`

Хранит `StateFlow<AppState>`, принимает `AppMsg`, вызывает `AppReducer`.

## 6. Planner ELM слой: задачи и записи

Planner - это общая логика задач, подзадач и записей.

Пакеты:

```text
features/app/domain
features/app/data
features/app/data/local
features/app/presentation
features/tasks/domain/model
features/notes/domain/model
```

### 6.1. PlannerState

`features/app/domain/PlannerState.kt`

Хранит:

- `tasks: List<TaskData>`;
- `notes: List<NoteData>`;
- `taskSortMode`;
- `showFlaggedOnly`.

Также содержит computed property `visibleTasks`, где применяются:

- фильтр "только с флажком";
- сортировка по приоритету;
- сортировка по ближайшему дедлайну.

### 6.2. PlannerMsg

`features/app/domain/PlannerMsg.kt`

Сообщения planner-фичи:

- `ScreenStarted` - ViewModel создана, нужно загрузить данные из Room;
- `SnapshotLoaded` - данные из Room загружены;
- `SetTaskSortMode`;
- `SetShowFlaggedOnly`;
- `AddTaskRequested`;
- `TaskIdGenerated`;
- `UpdateTaskRequested`;
- `DeleteTaskRequested`;
- `CompleteTaskRequested`;
- `SetTaskDone`;
- `SetSubTaskDone`;
- `AddNoteRequested`;
- `NoteIdGenerated`;
- `UpdateNoteRequested`;
- `DeleteNoteRequested`.

Важно: добавление задачи не создаёт id прямо в reducer. Сначала приходит `AddTaskRequested`, reducer возвращает команду `GenerateTaskId`, ViewModel исполняет команду и отправляет `TaskIdGenerated`.

### 6.3. PlannerCommand

`features/app/domain/PlannerCommand.kt`

Команды side effects:

- `LoadPlannerSnapshot` - загрузить задачи/записи из Room;
- `GenerateTaskId` - создать id задачи;
- `GenerateNoteId` - создать id записи;
- `SaveTask` - сохранить задачу в Room;
- `DeleteTask` - удалить задачу из Room;
- `SaveNote` - сохранить запись в Room;
- `DeleteNote` - удалить запись из Room.

Именно так Room не ломает ELM: reducer не вызывает базу, он только возвращает команду.

### 6.4. PlannerReducer

`features/app/domain/PlannerReducer.kt`

Чистый update-слой. Он:

- принимает старый `PlannerState`;
- принимает `PlannerMsg`;
- возвращает `ElmUpdate<PlannerState, PlannerCommand, Nothing>`.

Примеры:

- `ScreenStarted` -> команда `LoadPlannerSnapshot`;
- `SnapshotLoaded` -> state заполняется задачами и записями;
- `TaskIdGenerated` -> задача добавляется в state + команда `SaveTask`;
- `DeleteTaskRequested` -> задача удаляется из state + команда `DeleteTask`;
- `NoteIdGenerated` -> запись добавляется + команда `SaveNote`;
- `SetSubTaskDone` -> подзадача удаляется из списка + команда `SaveTask` для обновлённой задачи.

### 6.5. PlannerViewModel

`features/app/presentation/PlannerViewModel.kt`

Это ELM runtime для задач/записей:

- создаёт `MutableStateFlow(PlannerState())`;
- в `init` отправляет `PlannerMsg.ScreenStarted`;
- вызывает `PlannerReducer.update`;
- кладёт новый state в `StateFlow`;
- исполняет `PlannerCommand`.

Команды Room исполняются внутри `viewModelScope.launch`, то есть асинхронно и lifecycle-aware.

## 7. Room: локальное сохранение задач и записей

Пакет:

```text
features/app/data
features/app/data/local
```

### 7.1. PlannerDatabase

`features/app/data/local/PlannerDatabase.kt`

Room database:

- таблица `tasks`;
- таблица `subtasks`;
- таблица `notes`.

База называется:

```text
evently_planner.db
```

Создаётся в `AppModule`.

### 7.2. PlannerTaskEntity

`features/app/data/local/PlannerTaskEntity.kt`

Room entity для задачи:

- `id`;
- `title`;
- `description`;
- `priority`;
- `isFlagged`;
- `deadline`;
- `isDone`;
- `position`.

`position` нужен, чтобы сохранять порядок добавления.

### 7.3. PlannerSubTaskEntity

`features/app/data/local/PlannerSubTaskEntity.kt`

Room entity для подзадачи:

- `id`;
- `taskId`;
- `title`;
- `isDone`;
- `position`.

Есть foreign key на `PlannerTaskEntity`. При удалении задачи подзадачи удаляются каскадно.

### 7.4. PlannerNoteEntity

`features/app/data/local/PlannerNoteEntity.kt`

Room entity для записи:

- `id`;
- `title`;
- `text`;
- `position`.

### 7.5. TaskWithSubTasks

`features/app/data/local/TaskWithSubTasks.kt`

Room relation:

```text
Task -> List<SubTask>
```

Нужна, чтобы загрузить задачу сразу с подзадачами.

### 7.6. PlannerDao

`features/app/data/local/PlannerDao.kt`

DAO содержит операции:

- `getTasksWithSubTasks`;
- `getNotes`;
- `nextTaskPosition`;
- `nextNotePosition`;
- `upsertTaskWithSubTasks`;
- `deleteTask`;
- `upsertNote`;
- `deleteNote`.

`upsertTaskWithSubTasks` сделан транзакцией:

1. сохранить задачу;
2. удалить старые подзадачи задачи;
3. вставить актуальный список подзадач.

Так проще поддерживать редактирование и удаление подзадач.

### 7.7. PlannerLocalMapper

`features/app/data/PlannerLocalMapper.kt`

Маппит Room entities в domain models и обратно:

- `TaskData -> PlannerTaskEntity`;
- `TaskData -> List<PlannerSubTaskEntity>`;
- `TaskWithSubTasks -> TaskData`;
- `NoteData -> PlannerNoteEntity`;
- `PlannerNoteEntity -> NoteData`.

Domain слой не знает про Room-аннотации.

### 7.8. PlannerRepository

`features/app/data/PlannerRepository.kt`

Репозиторий для planner persistence:

- `loadSnapshot`;
- `saveTask`;
- `deleteTask`;
- `saveNote`;
- `deleteNote`.

Все операции идут на `Dispatchers.IO`, чтобы не блокировать UI thread.

### 7.9. Что сохраняется после перезапуска

Сейчас через Room сохраняются:

- задачи;
- подзадачи;
- записи;
- приоритеты;
- дедлайны;
- флажки;
- порядок добавления.

После переворота экрана данные сохраняются во ViewModel. После убийства процесса или перезапуска приложения данные восстанавливаются из Room.

## 8. Tasks feature

Пакеты:

```text
features/tasks/domain/model
features/tasks/presentation
```

### 8.1. TaskData

`features/tasks/domain/model/TaskData.kt`

Domain model задачи:

- id;
- title;
- description;
- priority;
- isFlagged;
- deadline;
- isDone;
- subtasks.

### 8.2. SubTaskData

`features/tasks/domain/model/SubTaskData.kt`

Domain model подзадачи:

- id;
- title;
- isDone.

Сейчас при выполнении подзадача удаляется из списка после подтверждения.

### 8.3. TaskPriority

`features/tasks/domain/model/TaskPriority.kt`

Приоритеты:

- `LOW`;
- `MEDIUM`;
- `HIGH`.

У каждого есть `title` для UI и `order` для сортировки.

### 8.4. TaskSortMode

`features/tasks/domain/model/TaskSortMode.kt`

Режимы сортировки:

- без сортировки;
- по приоритету;
- по ближайшему дедлайну.

### 8.5. TaskDeadline

`features/tasks/domain/model/TaskDeadline.kt`

Функции для дедлайнов:

- `deadlineEpochMillis`;
- `deadlineDateKey`;
- `isDeadlineOverdue`;
- `dateKeyFromMillis`;
- `dateKey`.

Если дата битая, `deadlineEpochMillis` возвращает `null`, а приложение не падает.

### 8.6. TaskListScreen

`features/tasks/presentation/TaskListScreen.kt`

Показывает список задач. Отвечает за UI:

- кнопка сортировки;
- тумблер "только с флажком";
- карточка задачи;
- дедлайн;
- красная плашка "Дедлайн просрочен";
- кнопки редактирования/удаления;
- checkbox выполнения основной задачи;
- checkbox выполнения подзадачи;
- confirmation dialogs.

Важный момент: экран не меняет данные сам. Он вызывает callbacks, которые в `EventlyAppRoot` превращаются в `PlannerMsg`.

### 8.7. TaskCreateScreen

`features/tasks/presentation/TaskCreateScreen.kt`

Экран создания/редактирования задачи:

- поля title/description;
- выбор приоритета;
- флажок;
- выбор дедлайна через `DatePickerDialog` и `TimePickerDialog`;
- список подзадач;
- удаление подзадачи при создании/редактировании;
- кнопки `Отменить` и `Сохранить`.

Форма хранит временное local UI state через `remember`, потому что это черновик ввода. Постоянное состояние задачи появляется только после `onSave`.

## 9. Notes feature

Пакеты:

```text
features/notes/domain/model
features/notes/presentation
```

### 9.1. NoteData

`features/notes/domain/model/NoteData.kt`

Domain model записи:

- id;
- title;
- text.

### 9.2. NotesListScreen

`features/notes/presentation/NotesListScreen.kt`

Показывает записи. Есть:

- редактирование;
- удаление;
- confirmation dialog удаления.

Удаление подтверждается через диалог: красная кнопка `Удалить`, серая/обычная `Отмена`.

### 9.3. NoteCreateScreen

`features/notes/presentation/NoteCreateScreen.kt`

Экран создания/редактирования записи:

- заголовок;
- текст;
- кнопки `Отменить` и `Сохранить`;
- scroll/ime/navigation padding, чтобы кнопки не перекрывались системной навигацией.

## 10. News feature: как полностью работает лента новостей

Пакеты:

```text
features/news/domain
features/news/domain/model
features/news/data
features/news/data/remote
features/news/data/cache
features/news/presentation
core/network
core/network/dto
```

### 10.1. Внешний API

Используется NYTimes Top Stories API:

```text
https://api.nytimes.com/svc/topstories/v2/home.json
```

Ключ берётся из `local.properties`:

```text
nyt.api.key=...
```

Gradle читает его в `build.gradle.kts` и кладёт в:

```kotlin
BuildConfig.NYT_API_KEY
```

Потом Dagger отдаёт ключ через `@Named(AppModule.NYT_API_KEY)`.

### 10.2. NytTopStoriesService

`core/network/NytTopStoriesService.kt`

Retrofit interface:

```kotlin
@GET("home.json")
suspend fun getTopStories(@Query("api-key") apiKey: String): NytNewsResponse
```

Это suspend-функция. Retrofit сам выполняет HTTP-запрос асинхронно через coroutines.

### 10.3. DTO

`core/network/dto/NytNewsResponse.kt`

DTO описывает JSON NYTimes:

- `NytNewsResponse`;
- `NytNewsItemDto`;
- `NytMultimediaDto`.

DTO не используется напрямую в UI. Он маппится в domain/data model.

### 10.4. NewsRemoteDataSource

`features/news/data/remote/NewsRemoteDataSource.kt`

Задачи:

- вызвать `NytTopStoriesService.getTopStories`;
- превратить DTO в `RemoteNewsArticle`;
- выбрать preview image URL;
- отправить debug request в JSONPlaceholder.

Debug request не критичен: он обёрнут в `runCatching`, поэтому если debug endpoint упадёт, новости не сломаются.

### 10.5. NewsRepository

`features/news/data/NewsRepository.kt`

Главный data-класс фичи новостей.

Методы:

- `loadCachedNews`;
- `refreshNews`;
- `formatPublishedAt`;
- `formatCacheTimestamp`.

`loadCachedNews`:

1. читает Room-кэш метаданных;
2. проверяет TTL 24 часа;
3. если кэш старый, чистит metadata cache и image cache;
4. возвращает `CachedNewsSnapshot`.

`refreshNews`:

1. проверяет API key;
2. идёт в сеть через `NewsRemoteDataSource`;
3. маппит remote articles в `NewsArticleData`;
4. кеширует изображения через `NewsImageCacheService`;
5. сохраняет metadata snapshot через `NewsMetadataCacheService`;
6. чистит неиспользуемые изображения;
7. возвращает `Result<CachedNewsSnapshot>`.

Ошибки превращаются в понятные сообщения через `NewsErrorMessageMapper`.

### 10.6. NewsErrorMessageMapper

`features/news/data/NewsErrorMessageMapper.kt`

Превращает технические ошибки в пользовательские:

- нет ключа API;
- NYT отклонил ключ;
- rate limit;
- сервер NYT недоступен;
- нет интернета;
- таймаут;
- generic IO error.

Так пользователь не видит `HttpException`, stack trace или непонятный код.

### 10.7. News cache

Пакет:

```text
features/news/data/cache
```

`NewsCacheDatabase` - Room database для таблицы `news_cache`.

`NewsCacheDao`:

- читает статьи из Room;
- заменяет весь набор статей;
- очищает таблицу metadata cache.

`NewsCacheEntity` - Room entity для одной новости в кэше. Хранит metadata и путь к картинке, но не сам файл изображения.

`NewsMetadataCacheService`:

- читает список новостей из Room;
- заменяет snapshot новыми новостями;
- чистит metadata cache.

`NewsImageCacheService`:

- скачивает картинку по URL;
- сохраняет файл в cache directory приложения;
- возвращает локальный путь;
- чистит неиспользуемые файлы;
- очищает весь файловый кэш изображений при команде из настроек.

`CachedNewsSnapshot`:

- список `NewsArticleData`;
- время обновления кэша.

### 10.8. News ELM domain

Файлы:

- `NewsFeedState`;
- `NewsSnapshotData`;
- `NewsMsg`;
- `NewsCommand`;
- `NewsEffect`;
- `NewsReducer`.

`NewsFeedState` хранит:

- `isLoading`;
- `isRefreshing`;
- `articles`;
- `errorMessage`;
- `lastUpdatedLabel`;
- `sourceLabel`.

`NewsMsg`:

- `ScreenStarted`;
- `CachedSnapshotLoaded`;
- `RefreshRequested`;
- `RefreshStarted`;
- `RefreshSucceeded`;
- `RefreshFailed`.

`NewsCommand`:

- `LoadCachedNews`;
- `RefreshNews`;
- `StartAutoRefresh`.

`NewsEffect`:

- `ShowRefreshError`.

`NewsReducer` чисто решает:

- что показать;
- когда включить loader;
- когда показать cached source;
- какие команды запустить дальше.

### 10.9. NewsViewModel

`features/news/presentation/NewsViewModel.kt`

ELM runtime для новостей:

1. в `init` отправляет `NewsMsg.ScreenStarted`;
2. reducer возвращает `LoadCachedNews`;
3. ViewModel читает кэш;
4. отправляет `CachedSnapshotLoaded`;
5. reducer решает refresh;
6. ViewModel вызывает `repository.refreshNews`;
7. успех превращается в `RefreshSucceeded`;
8. ошибка превращается в `RefreshFailed`;
9. auto-refresh каждые 120 секунд отправляет `RefreshRequested(false)`.

Используется:

- `MutableStateFlow<NewsFeedState>` для состояния;
- `MutableSharedFlow<NewsEffect>` для одноразовых эффектов;
- `viewModelScope.launch` для асинхронных команд.

### 10.10. NewsFeedScreen

`features/news/presentation/NewsFeedScreen.kt`

Compose UI:

- собирает state через `collectAsState`;
- показывает loader;
- показывает error state;
- показывает cached/network labels;
- показывает cards новостей;
- показывает кнопку `Обновить`;
- для изображений использует Coil `SubcomposeAsyncImage`.

Экран не делает сеть сам. Он отправляет `NewsMsg.RefreshRequested`.

## 11. Settings feature

Пакеты:

```text
features/settings/domain
features/settings/domain/model
features/settings/data
features/settings/presentation
```

### 11.1. SettingsState

Хранит выбранную тему и UI-состояние confirmation dialog для очистки кэша новостей:

```kotlin
val themePreference: ThemePreference
val showClearNewsCacheDialog: Boolean
val isClearingNewsCache: Boolean
```

### 11.2. ThemePreference

Enum:

- `LIGHT`;
- `DARK`.

Есть `storageKey` для SharedPreferences и `title` для UI.

### 11.3. SettingsMsg / Command / Effect / Reducer

`SettingsMsg.ThemeSelected` - пользователь выбрал тему.

`SettingsMsg.ClearNewsCacheClicked` - пользователь нажал пункт очистки кэша.

`SettingsMsg.ClearNewsCacheConfirmed` - пользователь подтвердил удаление.

`SettingsMsg.NewsCacheCleared` / `NewsCacheClearFailed` - результат выполнения команды.

`SettingsCommand.SaveThemePreference` - сохранить тему.

`SettingsCommand.ClearNewsCache` - очистить Room-кэш metadata новостей и файловый кэш изображений.

`SettingsEffect.ThemeApplied` - одноразовый эффект, что тема применена.

`SettingsEffect.NewsCacheCleared` / `NewsCacheClearFailed` - одноразовый эффект для snackbar.

`SettingsReducer`:

- обновляет `SettingsState`;
- возвращает команду сохранения темы;
- показывает и скрывает confirmation dialog очистки кэша;
- возвращает command очистки кэша и effect результата.

### 11.4. SettingsRepository

`features/settings/data/SettingsRepository.kt`

Использует `SharedPreferences`:

```text
evently_settings
```

Ключ:

```text
theme_preference
```

Кроме темы, repository очищает кэш новостей через `NewsMetadataCacheService.clearAll()` и `NewsImageCacheService.clearAllImages()`.

### 11.5. SettingsViewModel

ELM runtime для настроек:

- читает тему из repository при создании;
- принимает `SettingsMsg`;
- вызывает `SettingsReducer`;
- исполняет `SaveThemePreference`;
- исполняет `ClearNewsCache` и возвращает результат в reducer как `SettingsMsg.NewsCacheCleared` или `SettingsMsg.NewsCacheClearFailed`.

### 11.6. SettingsScreen

Показывает настройки. Сейчас есть пункты:

- `Тема`.
- `Кэш новостей`.

При нажатии открывается `AlertDialog` выбора темы. Выбранная тема отмечается галочкой.

Для кэша новостей открывается confirmation dialog `Очистить кэш новостей?`. Подтверждение выполняется красной кнопкой `Удалить`, отмена - серой кнопкой `Отмена`. После выполнения показывается snackbar.

## 12. Home feature

`features/home/presentation/HomeScreen.kt`

Главный экран показывает:

- количество задач;
- количество записей;
- количество подзадач;
- количество просроченных дедлайнов;
- кнопку перехода в календарь задач.

Данные приходят сверху из `PlannerState`, сам экран ничего не хранит в базе и не меняет бизнес-состояние.

## 13. Calendar feature

`features/calendar/presentation/TaskCalendarScreen.kt`

Экран календаря:

- показывает месяц;
- даёт переключать месяц;
- подсвечивает даты, где есть задачи с дедлайнами;
- при выборе даты показывает задачи на эту дату.

Для ключей даты используются helper-функции из `TaskDeadline.kt`.

Локальное UI-state календаря:

- `monthOffset`;
- `selectedDateKey`.

Это именно UI-state календаря, а не бизнес-данные. Поэтому допустимо хранить через `rememberSaveable`.

## 14. Навигация и backstack

В проекте нет Navigation Component. Навигация реализована через ELM state:

- `AppState.currentScreen`;
- `AppState.currentMainTab`;
- `AppReducer`;
- `AppViewModel`.

Почему это работает:

- при переходе на создание/редактирование state меняет `currentScreen`;
- при возврате вызывается `AppMsg.MainScreenRequested`;
- выбранная вкладка хранится отдельно в `currentMainTab`, поэтому после выхода из задачи пользователь возвращается на ту же вкладку, а не на новости;
- редактируемые объекты хранятся в `editingTask`/`editingNote`.

Это не системный Android back stack, а внутренний ELM backstack/state navigation. Для текущего масштаба приложения это проще и тестируемо.

## 15. Всплывающие окна и подтверждения

Диалоги сделаны через Compose `AlertDialog`.

Где используются:

- `TaskListScreen`:
  - подтверждение выполнения задачи;
  - подтверждение удаления задачи;
  - подтверждение выполнения подзадачи;
  - диалог сортировки.
- `NotesListScreen`:
  - подтверждение удаления записи.
- `SettingsScreen`:
  - выбор темы.

Важный принцип:

- сам диалог хранит только временное UI-состояние: открыт/закрыт;
- после подтверждения отправляется callback;
- callback превращается в `PlannerMsg` или `SettingsMsg`;
- бизнес-изменение происходит через ELM reducer.

## 16. Dependency Injection: Dagger graph

Пакет:

```text
di/
```

### 16.1. EventlyApplication

Создаёт `AppComponent` один раз на уровне приложения.

### 16.2. AppComponent

`di/AppComponent.kt`

Dagger component:

- подключает `AppModule`;
- подключает `NetworkModule`;
- отдаёт фабрики ViewModel:
  - `AppViewModelFactory`;
  - `PlannerViewModelFactory`;
  - `NewsViewModelFactory`;
  - `SettingsViewModelFactory`.

### 16.3. AppModule

`di/AppModule.kt`

Предоставляет:

- `Context`;
- `SharedPreferences`;
- `PlannerDatabase`;
- `PlannerDao`;
- `NYT_API_KEY`.

Room создаётся здесь:

```kotlin
Room.databaseBuilder(
    context,
    PlannerDatabase::class.java,
    "evently_planner.db"
).build()
```

### 16.4. NetworkModule

`di/NetworkModule.kt`

Предоставляет:

- `OkHttpClient`;
- Retrofit для NYT;
- Retrofit для debug endpoint;
- `NytTopStoriesService`;
- `DebugApiService`.

NYT base URL:

```text
https://api.nytimes.com/svc/topstories/v2/
```

Debug base URL:

```text
https://jsonplaceholder.typicode.com/
```

### 16.5. Почему DI полезен

DI убирает создание зависимостей из UI. Compose-экраны не создают Retrofit, Room, repository или SharedPreferences.

Это лучше для SOLID:

- Single Responsibility: каждый класс делает свою задачу;
- Dependency Inversion: ViewModel зависит от abstractions/repository, а не создаёт базу сама;
- Open/Closed: можно заменить data layer без переписывания UI;
- тестируемость: reducer тестируется без Android, repository можно тестировать отдельно.

## 17. Асинхронность и реактивность

### 17.1. StateFlow

StateFlow используется в ViewModel:

- `AppViewModel.state`;
- `PlannerViewModel.state`;
- `NewsViewModel.state`;
- `SettingsViewModel.state`.

Compose вызывает `collectAsState()`, поэтому экран автоматически перерисовывается при изменении state.

Это и есть основная "реактивщина" в приложении.

### 17.2. SharedFlow

SharedFlow используется для one-shot effects:

- `NewsViewModel.effects`;
- `SettingsViewModel.effects`.

Сейчас effects подготовлены архитектурно. Основной UI пока в основном читает `StateFlow`, но effects уже есть как место для snackbar/toast/navigation event.

### 17.3. Coroutines

Coroutines используются:

- во ViewModel через `viewModelScope.launch`;
- в repositories через `suspend fun`;
- для Room DAO suspend-функций;
- для Retrofit suspend-функций;
- для `Dispatchers.IO` в repository/cache.

### 17.4. Retrofit suspend

Сетевой запрос:

```kotlin
nytTopStoriesService.getTopStories(apiKey)
```

Это suspend-функция. Она не блокирует главный поток.

### 17.5. Room suspend

DAO функции тоже suspend:

```kotlin
suspend fun getTasksWithSubTasks()
suspend fun upsertTask(...)
```

Они вызываются из `PlannerRepository` на `Dispatchers.IO`.

## 18. Сетевые запросы: полный путь

Пример: пользователь открыл вкладку "Новости".

1. `MainActivity` создаёт `NewsViewModel` через Dagger factory.
2. `NewsViewModel.init` отправляет `NewsMsg.ScreenStarted`.
3. `NewsReducer` возвращает `NewsCommand.LoadCachedNews`.
4. `NewsViewModel` вызывает `repository.loadCachedNews()`.
5. Если кэш есть, отправляется `NewsMsg.CachedSnapshotLoaded`.
6. `NewsReducer` показывает кэш и возвращает `NewsCommand.RefreshNews(false)`.
7. `NewsViewModel` вызывает `repository.refreshNews()`.
8. `NewsRepository` вызывает `NewsRemoteDataSource.fetchArticles`.
9. `NewsRemoteDataSource` вызывает Retrofit service `NytTopStoriesService`.
10. Retrofit делает HTTP GET на NYTimes.
11. Ответ JSON маппится в DTO.
12. DTO маппится в `RemoteNewsArticle`.
13. `NewsRepository` маппит это в `NewsArticleData`.
14. Изображения сохраняются в file cache.
15. Метаданные сохраняются в Room cache.
16. Успех возвращается как `NewsMsg.RefreshSucceeded`.
17. `NewsReducer` обновляет `NewsFeedState`.
18. `NewsFeedScreen` получает новый state через `collectAsState` и перерисовывается.

Если ошибка:

1. exception попадает в `NewsRepository`;
2. `NewsErrorMessageMapper` делает понятный текст;
3. `NewsViewModel` отправляет `NewsMsg.RefreshFailed`;
4. reducer кладёт `errorMessage` в state и создаёт `NewsEffect.ShowRefreshError`;
5. UI показывает ошибку и кнопку `Повторить`.

## 19. Пример полного ELM-пути: создание задачи

Пользователь нажимает `Добавить задачу`, заполняет форму и жмёт `Сохранить`.

Путь:

1. `MainTabsScreen` вызывает `onAddTaskClick`.
2. `EventlyAppRoot` отправляет:

```kotlin
AppMsg.AddTaskClicked
```

3. `AppReducer` меняет:

```text
currentScreen = CREATE_TASK
```

4. Показывается `TaskCreateScreen`.
5. Пользователь жмёт `Сохранить`.
6. `EventlyAppRoot` отправляет:

```kotlin
PlannerMsg.AddTaskRequested(...)
```

7. `PlannerReducer` не создаёт задачу сразу, а возвращает:

```kotlin
PlannerCommand.GenerateTaskId(...)
```

8. `PlannerViewModel` исполняет команду через `IdGenerator`.
9. ViewModel отправляет:

```kotlin
PlannerMsg.TaskIdGenerated(...)
```

10. `PlannerReducer` добавляет задачу в `PlannerState`.
11. Reducer возвращает команду:

```kotlin
PlannerCommand.SaveTask(task)
```

12. `PlannerViewModel` исполняет команду через `PlannerRepository.saveTask`.
13. `PlannerRepository` сохраняет задачу и подзадачи в Room.
14. `EventlyAppRoot` отправляет `AppMsg.MainScreenRequested`.
15. Пользователь возвращается на главный tabs screen.
16. `TaskListScreen` получает обновлённый список через `StateFlow`.

Так один пользовательский action проходит через View -> Msg -> Reducer -> State + Command -> Runtime -> Room.

## 20. Пример полного ELM-пути: выполнение подзадачи

1. Пользователь нажимает checkbox подзадачи.
2. `TaskListScreen` открывает confirmation dialog.
3. Пользователь подтверждает.
4. callback идёт в `EventlyAppRoot`.
5. Отправляется:

```kotlin
PlannerMsg.SetSubTaskDone(task, subTask, true)
```

6. `PlannerReducer` удаляет подзадачу из списка.
7. Возвращает:

```kotlin
PlannerCommand.SaveTask(updatedTask)
```

8. `PlannerViewModel` вызывает `PlannerRepository.saveTask`.
9. `PlannerDao.upsertTaskWithSubTasks` транзакционно заменяет список подзадач в Room.
10. UI перерисовывается без выполненной подзадачи.

## 21. Сохранение состояния

Есть несколько уровней сохранения:

### 21.1. UI local state

Используется `remember` / `rememberSaveable`.

Примеры:

- открытость диалога;
- введённый текст в форме до сохранения;
- выбранная дата календаря;
- offset месяца в календаре.

Это временное UI-состояние.

### 21.2. ViewModel state

`StateFlow` во ViewModel переживает поворот экрана.

Примеры:

- `PlannerState`;
- `AppState`;
- `NewsFeedState`;
- `SettingsState`.

### 21.3. Persistent local storage

Сохраняется после полного перезапуска:

- задачи/записи - Room `evently_planner.db`;
- тема - SharedPreferences `evently_settings`;
- кэш metadata новостей - Room database `evently_news_cache.db`;
- изображения новостей - app cache directory.

### 21.4. Remote storage

Удалённого пользовательского хранилища задач/записей нет. NYTimes используется только как внешний источник новостей.

## 22. Тесты

### 22.1. Unit-тесты

Папка:

```text
src/test/java
```

Покрыты:

- `PlannerReducerTest` - ELM planner flow, команды сохранения, сортировки, подзадачи, дедлайны;
- `AppReducerTest` - навигационный state и возврат на вкладку;
- `NewsReducerTest` - команды загрузки кэша, refresh, error effects;
- `SettingsReducerTest` - выбор темы, команда сохранения, dialog и команда очистки кэша новостей, effect;
- `TaskDeadlineTest` - парсинг дедлайнов и защита от битой даты;
- `NewsErrorMessageMapperTest` - понятные ошибки для пользователя;
- `ThemePreferenceTest` - чтение темы из storage key;
- `IdGeneratorTest` - генерация id;
- `PlannerLocalMapperTest` - маппинг Room entities <-> domain models.

Последний прогон:

```text
31 unit-тест, failures=0, errors=0
```

### 22.2. Android UI tests

Папка:

```text
src/androidTest/java
```

Тесты:

- `TaskFlowTest.createTask_happyPath_showsTaskInList` - happy-path создания задачи;
- `TaskFlowTest.taskList_snapshotSmoke_rendersNonEmptyImage` - snapshot-smoke списка задач;
- `SettingsFlowTest.selectDarkTheme_updatesSettingsRow` - выбор тёмной темы.

Компиляция UI-тестов проверена командой:

```text
.\gradlew.bat compileDebugAndroidTestKotlin
```

Для реального запуска нужен подключённый телефон или эмулятор.

## 23. SOLID, KISS, DRY, Clean Architecture

### 23.1. Single Responsibility

Примеры:

- `PlannerReducer` только пересчитывает state;
- `PlannerRepository` только работает с persistence;
- `PlannerDao` только SQL/Room операции;
- `TaskListScreen` только UI;
- `NewsRemoteDataSource` только remote data;
- `NewsErrorMessageMapper` только user-readable errors.

### 23.2. Open/Closed

Новые команды можно добавлять без переписывания UI. Например, если позже появится синхронизация задач с сервером, можно добавить `PlannerCommand.SyncTasks`.

### 23.3. Liskov / Interface Segregation

В проекте мало inheritance, в основном data classes/sealed interfaces. Это снижает риск неправильного наследования.

### 23.4. Dependency Inversion

ViewModel не создаёт Room/Retrofit вручную. Зависимости приходят через Dagger factories.

### 23.5. KISS

Навигация сделана через простой ELM state, без тяжёлого Navigation Component. Для текущего числа экранов это проще и тестируемо.

### 23.6. DRY

Общие вещи вынесены:

- `ElmUpdate`;
- `Id`, `IdGenerator`;
- date helpers в `TaskDeadline`;
- Room mapping в `PlannerLocalMapper`;
- error mapping в `NewsErrorMessageMapper`.

## 24. ADR документы

Папка:

```text
docs/adr
```

Документы:

- `0001-feature-first-elm-architecture.md` - почему выбрана feature-first ELM architecture;
- `0002-news-cache-policy.md` - политика кэша новостей;
- `0003-planner-room-persistence.md` - почему задачи/записи сохраняются через Room и как это не ломает ELM.

ADR нужны для критерия "все решения обоснованы".

## 25. Как коротко объяснить архитектуру на защите

Можно сказать так:

> Проект разделён feature-first. Внутри фич есть domain, data и presentation. Presentation на Compose только отображает State и отправляет Msg. ViewModel используется не как место бизнес-логики, а как Android lifecycle-aware ELM runtime: принимает сообщения, вызывает чистый Reducer, обновляет StateFlow и исполняет Command. Reducer не знает про Room, Retrofit или SharedPreferences. Все side effects вынесены в команды и репозитории.

Про новости:

> Новости работают через Retrofit с NYTimes Top Stories API. При старте сначала читается локальный Room-кэш, затем запускается сетевое обновление. Метаданные новостей хранятся в Room, изображения сохраняются в файловый кэш. Ошибки сети маппятся в понятные пользовательские сообщения.

Про настройки:

> Настройки работают как отдельная ELM-фича. Выбор темы сохраняется в SharedPreferences. Очистка кэша новостей проходит через SettingsMsg, SettingsReducer, SettingsCommand.ClearNewsCache и SettingsRepository, который удаляет metadata cache и image cache. UI только показывает пункт настроек, confirmation dialog и snackbar результата.

Про задачи/записи:

> Задачи, подзадачи и записи сохраняются в Room. При старте PlannerViewModel отправляет ScreenStarted, reducer возвращает команду LoadPlannerSnapshot, ViewModel загружает snapshot через PlannerRepository и возвращает его в reducer как SnapshotLoaded. При изменениях reducer возвращает SaveTask/DeleteTask/SaveNote/DeleteNote, а ViewModel исполняет эти команды.

Про реактивность:

> State доставляется в Compose через StateFlow и collectAsState. Когда reducer создаёт новый state, Compose автоматически перерисовывает экран. Для одноразовых событий предусмотрен SharedFlow effects.

Про тесты:

> Основная бизнес-логика покрыта unit-тестами reducer/domain/core. UI покрыт Compose-тестами: happy-path создания задачи, snapshot-smoke списка задач и выбор темы.

## 26. Команды проверки

Unit-тесты:

```powershell
.\gradlew.bat testDebugUnitTest
```

Компиляция Android UI-тестов:

```powershell
.\gradlew.bat compileDebugAndroidTestKotlin
```

Debug-сборка:

```powershell
.\gradlew.bat assembleDebug
```

Последняя проверка:

- `testDebugUnitTest` - успешно;
- `compileDebugAndroidTestKotlin` - успешно;
- `assembleDebug` - успешно.
