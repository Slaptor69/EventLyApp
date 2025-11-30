package com.example.eventlyapp.id

object IdGenerator {
    private var counter = 0
    fun nextId(): Id = Id((++counter).toString())
}
