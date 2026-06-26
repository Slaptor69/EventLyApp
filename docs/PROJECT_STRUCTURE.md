# EventLyApp: роли пакетов и классов

Этот файл нужен как удобная карта проекта для защиты: где что лежит, какую роль выполняет каждый пакет и за что отвечает каждый класс.

## 1. Общая идея структуры

Проект разделен по feature-first подходу:

```text
com.example.eventlyapp
  core/        - общие примитивы проекта
  di/          - Dagger graph и создание зависимостей
  features/    - пользовательские фичи приложения
  ui/theme/    - Compose-тема приложения
```

Внутри фич обычно используются слои:

```text
domain       - состояние, сообщения, команды, reducer, бизнес-модели
data         - repository, Room, network/cache mapping
presentation - Compose UI и ViewModel
```

Главная архитектурная схема:

```text
Compose UI -> Msg -> ViewModel -> Reducer -> State + Command + Effect
                          |                         |
                          |                         v
                          +------ Repository / Room / Network
```

ViewModel в этом проекте - не место основной бизнес-логики, а runtime для ELM-потока: она хранит `StateFlow`, принимает `Msg`, вызывает reducer и исполняет команды.

## 2. Корневой пакет

Пакет:

```text
com.example.eventlyapp
```

### `EventlyApplication`

Application-класс. Создает `AppComponent` один раз на уровне приложения и хранит Dagger graph, чтобы Activity могла получать фабрики ViewModel.

### `MainActivity`

Главная Android Activity. Поднимает Compose UI, получает ViewModel через Dagger factories, собирает тему и показывает корневой экран `EventlyAppRoot`.

## 3. `core`

Пакет `core` содержит общие классы, которые не принадлежат конкретной фиче.

### `core/elm/ElmUpdate`

Общий контейнер результата reducer:

- `state` - новое состояние;
- `commands` - команды для side effects;
- `effects` - одноразовые UI/effect события.

Также содержит helper `toElmUpdate`, чтобы reducer мог удобно возвращать новое состояние.

### `core/id/Id`

Value object для идентификатора сущности. Используется задачами, подзадачами и заметками, чтобы не таскать по проекту голые строки без смысла.

### `core/id/HasId`

Общий интерфейс для моделей, у которых есть `id`. Сейчас его реализуют domain-модели вроде `TaskData`, `SubTaskData`, `NoteData`.

### `core/id/IdGenerator`

Генератор новых `Id`. Используется в `PlannerViewModel`, когда reducer возвращает команду `GenerateTaskId` или `GenerateNoteId`.

### `core/presentation/SingleViewModelFactory`

Общий базовый класс для простых `ViewModelProvider.Factory`. Убирает повторение одного и того же кода из `AppViewModelFactory`, `PlannerViewModelFactory`, `NewsViewModelFactory`, `SettingsViewModelFactory`.

### `core/network/NytTopStoriesService`

Retrofit interface для NYTimes Top Stories API. Отвечает только за описание HTTP-запроса к новостям.

### `core/network/DebugApiService`

Retrofit interface для debug-запроса на тестовый endpoint. Используется news data layer как дополнительная проверка/демонстрация сетевого запроса.

### `core/network/dto/NytNewsResponse`

DTO-структуры ответа NYTimes. Это не domain-модель, а форма внешнего JSON, которую Retrofit/Gson парсит после сетевого запроса.

### `core/network/dto/DebugPostRequest`

DTO для debug POST-запроса. Нужен только data/network слою, UI о нем не знает.

## 4. `di`

Пакет `di` отвечает за Dependency Injection через Dagger.

### `AppComponent`

Главный Dagger component. Собирает `AppModule` и `NetworkModule`, а наружу отдает фабрики ViewModel:

- `AppViewModelFactory`;
- `PlannerViewModelFactory`;
- `NewsViewModelFactory`;
- `SettingsViewModelFactory`.

### `AppModule`

Предоставляет зависимости уровня приложения:

- `Context`;
- `SharedPreferences`;
- `PlannerDatabase`;
- `PlannerDao`;
- NYTimes API key из `BuildConfig.NYT_API_KEY`.

### `NetworkModule`

Создает сетевой слой:

- `OkHttpClient`;
- Retrofit для NYTimes;
- Retrofit для debug endpoint;
- `NytTopStoriesService`;
- `DebugApiService`.

## 5. `features/app`

Это центральная фича приложения. Она отвечает за навигационное состояние и общий planner-поток задач/заметок.

### `features/app/domain/AppState`

Навигационное состояние приложения:

- текущий экран;
- выбранная главная вкладка;
- редактируемая задача;
- редактируемая заметка.

### `features/app/domain/AppScreen`

Enum экранов приложения:

- главный экран с вкладками;
- создание/редактирование задачи;
- создание/редактирование заметки;
- календарь задач;
- настройки.

### `features/app/domain/MainTab`

Enum вкладок нижней навигации:

- новости;
- главная;
- задачи;
- заметки.

### `features/app/domain/AppMsg`

Сообщения навигационного ELM-слоя. UI отправляет эти сообщения вместо прямого изменения экрана: выбрать вкладку, открыть создание задачи, открыть настройки, вернуться на главный экран.

### `features/app/domain/AppReducer`

Чистый reducer навигации. Принимает `AppState` и `AppMsg`, возвращает новое состояние. Команд и эффектов у этого reducer сейчас нет, поэтому в типах используется `Nothing`.

### `features/app/presentation/AppViewModel`

ViewModel для навигационного состояния. Хранит `StateFlow<AppState>`, принимает `AppMsg`, вызывает `AppReducer`.

### `features/app/presentation/AppViewModelFactory`

Фабрика для создания `AppViewModel`. Наследуется от общего `SingleViewModelFactory`.

### `features/app/presentation/EventlyAppRoot`

Корневой Compose-компонент приложения. Соединяет `AppViewModel`, `PlannerViewModel`, `NewsViewModel`, `SettingsViewModel` и решает, какой экран показать по `AppState.currentScreen`.

Также именно здесь callbacks из UI превращаются в `AppMsg` и `PlannerMsg`.

### `features/app/presentation/MainTabsScreen`

Экран с основными вкладками. Показывает news/home/tasks/notes и верхние действия: добавить задачу, добавить заметку, открыть календарь, открыть настройки.

## 6. Planner: общий слой задач и заметок

Planner сейчас находится внутри `features/app`, потому что задачи и заметки сохраняются в одном локальном planner-хранилище.

### `features/app/domain/PlannerState`

Состояние planner-части:

- список задач;
- список заметок;
- текущий режим сортировки задач;
- фильтр "только с флажком".

Содержит computed property `visibleTasks`, где применяются фильтр и сортировка.

### `features/app/domain/PlannerMsg`

Сообщения для задач и заметок:

- загрузить snapshot;
- добавить/обновить/удалить задачу;
- отметить задачу или подзадачу;
- добавить/обновить/удалить заметку;
- поменять сортировку и фильтр.

### `features/app/domain/PlannerCommand`

Команды side effects:

- загрузка snapshot из Room;
- генерация id;
- сохранение/удаление задачи;
- сохранение/удаление заметки.

Reducer не ходит в Room сам, он только возвращает эти команды.

### `features/app/domain/PlannerReducer`

Главная бизнес-логика planner-части. Чисто пересчитывает `PlannerState`:

- добавляет задачу после генерации id;
- обновляет задачу;
- удаляет задачу;
- удаляет выполненную подзадачу;
- добавляет/обновляет/удаляет заметку;
- возвращает команды сохранения в Room.

Эффектов у planner reducer сейчас нет, поэтому effect-тип - `Nothing`.

### `features/app/presentation/PlannerViewModel`

ELM runtime для задач и заметок. При старте отправляет `ScreenStarted`, загружает данные из Room, исполняет команды `SaveTask`, `DeleteTask`, `SaveNote`, `DeleteNote`.

### `features/app/presentation/PlannerViewModelFactory`

Фабрика для `PlannerViewModel`. Подставляет `PlannerReducer` и `PlannerRepository`.

### `features/app/data/PlannerSnapshot`

Снимок локальных данных planner: список задач и список заметок. Используется при загрузке из Room.

### `features/app/data/PlannerRepository`

Репозиторий для локального сохранения задач и заметок. Работает через `PlannerDao` и переводит операции ViewModel в Room-вызовы.

### `features/app/data/PlannerLocalMapper`

Маппинг между domain-моделями и Room entities:

- `TaskData` <-> `PlannerTaskEntity`;
- `SubTaskData` <-> `PlannerSubTaskEntity`;
- `NoteData` <-> `PlannerNoteEntity`.

### `features/app/data/local/PlannerDatabase`

Room database для planner-данных. Содержит таблицы задач, подзадач и заметок.

### `features/app/data/local/PlannerDao`

DAO для Room. Загружает задачи с подзадачами, заметки, считает следующую позицию, сохраняет и удаляет сущности.

### `features/app/data/local/PlannerTaskEntity`

Room entity для задачи. Это data-слой, не domain-модель.

### `features/app/data/local/PlannerSubTaskEntity`

Room entity для подзадачи. Связана с задачей через `taskId` и foreign key.

### `features/app/data/local/PlannerNoteEntity`

Room entity для заметки.

### `features/app/data/local/TaskWithSubTasks`

Room relation, которая позволяет загрузить задачу вместе со списком подзадач.

## 7. `features/tasks`

Фича задач. Domain-модели лежат отдельно, а общий reducer/persistence сейчас находятся в planner-слое.

### `features/tasks/domain/model/TaskData`

Domain-модель задачи:

- id;
- заголовок;
- описание;
- приоритет;
- флажок;
- дедлайн;
- статус выполнения;
- подзадачи.

### `features/tasks/domain/model/SubTaskData`

Domain-модель подзадачи:

- id;
- заголовок;
- статус выполнения.

### `features/tasks/domain/model/TaskPriority`

Enum приоритетов задачи. Хранит UI-название и порядок сортировки.

### `features/tasks/domain/model/TaskSortMode`

Enum режимов сортировки списка задач.

### `features/tasks/domain/model/TaskDeadline`

Набор helper-функций для дедлайнов: парсинг строки в millis, получение ключа даты, проверка просрочки.

### `features/tasks/presentation/TaskListScreen`

Compose-экран списка задач. Показывает задачи, сортировку, фильтр, карточки, подзадачи, диалоги подтверждения удаления/выполнения.

### `features/tasks/presentation/TaskCreateScreen`

Compose-экран создания и редактирования задачи. Хранит черновик формы через `remember`, а при сохранении отдает данные наверх через callback.

## 8. `features/notes`

Фича заметок. Сейчас у нее есть UI и domain-модель, а бизнес-операции добавления/обновления/удаления проходят через общий `PlannerReducer`.

### `features/notes/domain/model/NoteData`

Domain-модель заметки:

- id;
- заголовок;
- текст.

### `features/notes/presentation/NotesListScreen`

Compose-экран списка заметок. Показывает карточки заметок, кнопки редактирования/удаления и диалог подтверждения удаления.

### `features/notes/presentation/NoteCreateScreen`

Compose-экран создания и редактирования заметки. Хранит локальный черновик заголовка и текста, а при сохранении отправляет данные наверх.

## 9. `features/news`

Фича новостей: сеть, кэш, reducer и экран ленты.

### `features/news/domain/model/NewsArticleData`

Domain-модель новости, которую понимает UI: заголовок, описание, ссылка, дата публикации, путь к картинке.

### `features/news/domain/model/NewsFeedState`

Состояние ленты новостей:

- статьи;
- загрузка;
- refresh;
- ошибка;
- подписи источника и времени обновления.

### `features/news/domain/model/NewsSnapshotData`

Domain-снимок новостей для передачи из кэша/репозитория в reducer.

### `features/news/domain/NewsMsg`

Сообщения news ELM-слоя: старт экрана, кэш загружен, пользователь запросил refresh, refresh начался/успешен/упал.

### `features/news/domain/NewsCommand`

Команды news side effects:

- загрузить кэш;
- обновить новости из сети;
- запустить автообновление.

### `features/news/domain/NewsEffect`

Одноразовые эффекты news-фичи. Сейчас используется `ShowRefreshError`, чтобы можно было показать ошибку пользователю отдельно от постоянного state.

### `features/news/domain/NewsReducer`

Чистая логика состояния ленты. Решает, когда показывать кэш, когда запускать сетевое обновление, как менять loading/refreshing/error state.

### `features/news/presentation/NewsViewModel`

ELM runtime для новостей. Исполняет команды reducer: читает кэш, обновляет новости, запускает автообновление, отправляет результат обратно как `NewsMsg`.

### `features/news/presentation/NewsViewModelFactory`

Фабрика `NewsViewModel`.

### `features/news/presentation/NewsFeedScreen`

Compose-экран ленты новостей. Показывает loading, error, список карточек, кнопку retry и изображения через Coil.

### `features/news/data/NewsRepository`

Репозиторий новостей. Объединяет remote data source, metadata cache, image cache и formatting дат. Возвращает результат в удобном для ViewModel виде.

### `features/news/data/NewsErrorMessageMapper`

Маппер технических ошибок в понятные пользовательские сообщения.

### `features/news/data/remote/NewsRemoteDataSource`

Remote data source. Делает запрос к NYTimes через `NytTopStoriesService`, маппит DTO в `RemoteNewsArticle`, отправляет debug request.

### `features/news/data/remote/RemoteNewsArticle`

Промежуточная data-модель новости после сети, но до domain/cache слоя.

### `features/news/data/cache/CachedNewsSnapshot`

Data-модель снимка новостей в локальном кэше.

### `features/news/data/cache/NewsCacheDatabase`

Room database для кэша метаданных новостей.

### `features/news/data/cache/NewsCacheDao`

DAO для чтения, замены и очистки метаданных новостей.

### `features/news/data/cache/NewsCacheEntity`

Room entity таблицы `news_cache`. Хранит metadata статьи и `imagePath`, но не сами изображения.

### `features/news/data/cache/NewsMetadataCacheService`

Сервис чтения/записи метаданных новостей через Room.

### `features/news/data/cache/NewsImageCacheService`

Сервис файлового кэша изображений. Скачивает картинки, сохраняет их локально, чистит неиспользуемые файлы.

## 10. `features/settings`

Фича настроек. Сейчас основная настройка - тема приложения.

### `features/settings/domain/model/ThemePreference`

Enum выбранной темы:

- системная;
- светлая;
- темная.

Содержит storage key и helper восстановления из `SharedPreferences`.

### `features/settings/domain/model/SettingsState`

Состояние настроек. Хранит выбранную тему, флаг диалога очистки кэша новостей и признак выполняющейся очистки.

### `features/settings/domain/SettingsMsg`

Сообщения настроек. Есть выбор темы и поток очистки кэша новостей: клик, отмена, подтверждение, успешный или неуспешный результат.

### `features/settings/domain/SettingsCommand`

Команды настроек. Есть сохранение темы в persistent storage и очистка кэша новостей.

### `features/settings/domain/SettingsEffect`

Одноразовые эффекты настроек: применение темы, успешная очистка кэша новостей и ошибка очистки.

### `features/settings/domain/SettingsReducer`

Чистая логика настроек. При выборе темы меняет state, возвращает команду сохранения и effect применения. При очистке кэша показывает confirmation dialog, после подтверждения возвращает command очистки и effect результата.

### `features/settings/data/SettingsRepository`

Работает с `SharedPreferences`: читает и сохраняет выбранную тему. Также через news cache services очищает Room-кэш метаданных новостей и файловый кэш изображений.

### `features/settings/presentation/SettingsViewModel`

ELM runtime настроек. При создании читает тему из repository, принимает `SettingsMsg`, вызывает reducer, сохраняет тему и очищает кэш новостей через commands.

### `features/settings/presentation/SettingsViewModelFactory`

Фабрика `SettingsViewModel`.

### `features/settings/presentation/SettingsScreen`

Compose-экран настроек. Показывает пункт выбора темы, пункт очистки кэша новостей, confirmation dialog и snackbar результата.

## 11. `features/home`

### `features/home/presentation/HomeScreen`

Главный обзорный экран. Показывает статистику по задачам, заметкам, подзадачам и просроченным дедлайнам. Сам не меняет данные, а только отображает состояние, переданное сверху.

## 12. `features/calendar`

### `features/calendar/presentation/TaskCalendarScreen`

Compose-экран календаря задач. Группирует задачи по дедлайнам, показывает месяц, переключение месяцев и список задач выбранного дня.

## 13. `ui/theme`

Пакет темы приложения.

### `Color`

Цветовая палитра Compose-темы.

### `Type`

Typography-настройки Compose-темы.

### `Theme`

Главная Compose-тема приложения. Применяет светлую/темную/системную тему с учетом `ThemePreference`.

## 14. Тестовые пакеты

### `src/test/java/.../core/id/IdGeneratorTest`

Проверяет генерацию id.

### `src/test/java/.../features/app/domain/AppReducerTest`

Проверяет навигационный reducer: открытие редактирования и возврат на главный экран.

### `src/test/java/.../features/app/domain/PlannerReducerTest`

Проверяет planner reducer: команды загрузки/сохранения, сортировку, фильтрацию, обновление заметок, удаление задач и подзадач.

### `src/test/java/.../features/app/data/PlannerLocalMapperTest`

Проверяет маппинг Room entities в domain-модели и обратно.

### `src/test/java/.../features/news/domain/NewsReducerTest`

Проверяет ELM-логику новостей: старт, кэш, refresh, ошибки.

### `src/test/java/.../features/news/data/NewsErrorMessageMapperTest`

Проверяет, что технические ошибки превращаются в понятные сообщения.

### `src/test/java/.../features/settings/domain/SettingsReducerTest`

Проверяет выбор темы: state, command, effect.

### `src/test/java/.../features/settings/domain/model/ThemePreferenceTest`

Проверяет восстановление темы из storage key.

### `src/test/java/.../features/tasks/domain/model/TaskDeadlineTest`

Проверяет парсинг дедлайнов, date key и определение просрочки.

### `src/androidTest/java/.../ExampleInstrumentedTest`

Базовый instrumentation test Android-пакета.

### `src/androidTest/java/.../features/tasks/presentation/TaskFlowTest`

UI-тесты задачи: happy path создания и snapshot-smoke списка задач.

### `src/androidTest/java/.../features/settings/presentation/SettingsFlowTest`

UI-тест выбора темы в настройках.

## 15. Документация `docs/adr`

ADR - это короткие записи архитектурных решений.

### `docs/NAVIGATION_AND_PRINCIPLES.md`

Отдельный документ для защиты: где находится навигация, как работает `AppState`/`AppReducer`/`EventlyAppRoot`, и где в коде видны SOLID, KISS и DRY.

### `0001-feature-first-elm-architecture.md`

Объясняет, почему проект использует feature-first структуру и ELM-подход.

### `0002-news-cache-policy.md`

Объясняет политику кэша новостей.

### `0003-planner-room-persistence.md`

Объясняет, почему задачи и заметки сохраняются через Room и как это сочетается с ELM.

### `0004-app-state-navigation.md`

Объясняет, почему навигация сделана через `AppState`, `AppMsg`, `AppReducer` и `EventlyAppRoot`, а не через отдельный navigation graph.

### `0005-settings-news-cache-cleanup.md`

Объясняет ручную очистку кэша новостей из настроек через ELM command и confirmation dialog.

### `0006-single-viewmodel-factory.md`

Объясняет общий `SingleViewModelFactory` как DRY-решение для простых ViewModel factories.

## 16. Самые важные потоки для защиты

### Создание задачи

```text
TaskCreateScreen
  -> PlannerMsg.AddTaskRequested
  -> PlannerReducer
  -> PlannerCommand.GenerateTaskId
  -> PlannerViewModel + IdGenerator
  -> PlannerMsg.TaskIdGenerated
  -> PlannerReducer adds TaskData
  -> PlannerCommand.SaveTask
  -> PlannerRepository
  -> PlannerDao / Room
```

### Создание заметки

```text
NoteCreateScreen
  -> PlannerMsg.AddNoteRequested
  -> PlannerReducer
  -> PlannerCommand.GenerateNoteId
  -> PlannerViewModel + IdGenerator
  -> PlannerMsg.NoteIdGenerated
  -> PlannerReducer adds NoteData
  -> PlannerCommand.SaveNote
  -> PlannerRepository
  -> PlannerDao / Room
```

### Загрузка новостей

```text
NewsViewModel init
  -> NewsMsg.ScreenStarted
  -> NewsReducer
  -> NewsCommand.LoadCachedNews
  -> NewsRepository reads cache
  -> NewsMsg.CachedSnapshotLoaded
  -> NewsCommand.RefreshNews
  -> NewsRemoteDataSource / Retrofit / NYTimes
  -> NewsRepository updates cache
  -> NewsMsg.RefreshSucceeded
  -> NewsReducer updates NewsFeedState
  -> NewsFeedScreen redraws
```

### Выбор темы

```text
SettingsScreen
  -> SettingsMsg.ThemeSelected
  -> SettingsReducer
  -> SettingsCommand.SaveThemePreference
  -> SettingsRepository / SharedPreferences
  -> SettingsEffect.ThemeApplied
  -> Theme redraws
```

### Очистка кэша новостей

```text
SettingsScreen
  -> SettingsMsg.ClearNewsCacheClicked
  -> SettingsReducer shows confirmation dialog
  -> SettingsMsg.ClearNewsCacheConfirmed
  -> SettingsCommand.ClearNewsCache
  -> SettingsRepository clears Room metadata and image files
  -> SettingsMsg.NewsCacheCleared
  -> SettingsEffect.NewsCacheCleared
  -> snackbar
```

## 17. Что можно сказать коротко

Проект построен так: `presentation` показывает UI и отправляет сообщения, `domain` через reducer принимает решения и меняет state, `data` выполняет сохранение, сеть и кэш. `core` хранит общие примитивы, а `di` собирает зависимости. За счет этого UI не знает про Room/Retrofit, reducer не знает про Android, а side effects вынесены в команды и repositories.
