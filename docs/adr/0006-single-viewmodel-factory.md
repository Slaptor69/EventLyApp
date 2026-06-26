# ADR 0006: Общая фабрика для одной ViewModel

## Статус

Принято.

## Контекст

Каждой ViewModel нужны зависимости через конструктор из Dagger, но Android API `viewModel()` ожидает `ViewModelProvider.Factory`. Без общего helper-а каждая простая фабрика для одной ViewModel повторяет одну и ту же проверку `modelClass`, cast и обработку ошибки.

## Решение

Использовать `core/presentation/SingleViewModelFactory.kt` как небольшой базовый класс для фабрик, которые создают один тип ViewModel.

Конкретные фабрики только передают зависимости и реализуют `createViewModel()`:

- `AppViewModelFactory`;
- `PlannerViewModelFactory`;
- `NewsViewModelFactory`;
- `SettingsViewModelFactory`.

## Итоги

Boilerplate фабрик централизован, поэтому приложение соблюдает DRY без добавления большой multibinding-схемы для ViewModel. Компромисс в том, что helper намеренно простой: он лучше всего подходит для схемы "одна фабрика - одна ViewModel", а не для большой generic factory map.
