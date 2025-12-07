package com.example.eventlyapp.model

enum class TaskPriority(val title: String, val order: Int) {
    LOW("Низкий", 0),
    MEDIUM("Средний", 1),
    HIGH("Высокий", 2)
}
