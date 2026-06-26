# ADR 0005: Очистка кэша новостей из настроек

## Статус

Принято.

## Контекст

Фича новостей хранит метаданные в Room, а изображения - в cache directory приложения. Автоматическая политика кэша очищает устаревшие данные, но пользователю также нужен видимый способ вручную удалить кэшированные новости.

Экран настроек уже отвечает за пользовательские предпочтения и одноразовую UI-обратную связь через settings Elm-срез.

## Решение

Добавить ручную очистку кэша новостей в настройки:

- `SettingsMsg.ClearNewsCacheClicked` открывает confirmation dialog.
- `SettingsMsg.ClearNewsCacheConfirmed` запрашивает очистку.
- `SettingsCommand.ClearNewsCache` представляет side effect.
- `SettingsRepository.clearNewsCache()` очищает метаданные через `NewsMetadataCacheService` и изображения через `NewsImageCacheService`.
- `SettingsEffect.NewsCacheCleared` или `SettingsEffect.NewsCacheClearFailed` управляет snackbar-сообщением.

UI только показывает строку настройки, confirmation dialog и snackbar. Он не удаляет файлы и не вызывает Room напрямую.

## Итоги

Действие очистки кэша видно пользователю и при этом следует ELM-границам. Reducer-ы остаются чистыми, а разрушительная операция защищена подтверждением. Settings теперь зависит от news cache services, что допустимо, потому что действие явно является cross-feature maintenance setting.
