package com.wcjung.engstudy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AgeGroupTest {

    @Test
    fun `fromKey returns correct age group for valid key`() {
        assertEquals(AgeGroup.ELEMENTARY, AgeGroup.fromKey("ELEMENTARY"))
        assertEquals(AgeGroup.HIGH_SCHOOL, AgeGroup.fromKey("HIGH_SCHOOL"))
        assertEquals(AgeGroup.PROFESSIONAL, AgeGroup.fromKey("PROFESSIONAL"))
    }

    @Test
    fun `fromKey returns COLLEGE for unknown key`() {
        assertEquals(AgeGroup.COLLEGE, AgeGroup.fromKey("UNKNOWN"))
    }

    @Test
    fun `all age groups have unique keys`() {
        val keys = AgeGroup.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `age groups are ordered correctly`() {
        val sorted = AgeGroup.entries.sortedBy { it.order }
        assertEquals(AgeGroup.ELEMENTARY, sorted.first())
        assertEquals(AgeGroup.PROFESSIONAL, sorted.last())
    }

    @Test
    fun `age group count is 5`() {
        assertEquals(5, AgeGroup.entries.size)
    }
}
