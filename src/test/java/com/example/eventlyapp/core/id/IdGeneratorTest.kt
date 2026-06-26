package com.example.eventlyapp.core.id

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IdGeneratorTest {
    @Test
    fun nextId_returnsUniqueSequentialValues() {
        val firstId = IdGenerator.nextId()
        val secondId = IdGenerator.nextId()

        assertNotEquals(firstId, secondId)
        assertEquals(firstId.value.toInt() + 1, secondId.value.toInt())
    }
}
