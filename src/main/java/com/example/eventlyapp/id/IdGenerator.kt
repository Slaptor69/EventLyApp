package com.example.eventlyapp.id

object IdGenerator {
    private var counter = 0
    fun nextId(): Id = Id((counter + 1).toString()).also { counter += 1 }
}
