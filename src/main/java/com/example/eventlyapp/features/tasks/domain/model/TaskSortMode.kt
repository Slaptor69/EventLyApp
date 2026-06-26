package com.example.eventlyapp.features.tasks.domain.model

enum class TaskSortMode(val title: String) {
    NONE("Без сортировки"),
    PRIORITY("По приоритету"),
    DEADLINE("По ближайшему дедлайну")
}
