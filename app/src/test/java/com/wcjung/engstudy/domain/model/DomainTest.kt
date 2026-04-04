package com.wcjung.engstudy.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DomainTest {

    @Test
    fun `fromKey returns correct domain for valid key`() {
        assertEquals(Domain.DAILY_LIFE, Domain.fromKey("DAILY_LIFE"))
        assertEquals(Domain.BUSINESS, Domain.fromKey("BUSINESS"))
        assertEquals(Domain.TECHNOLOGY, Domain.fromKey("TECHNOLOGY"))
    }

    @Test
    fun `fromKey returns GENERAL for unknown key`() {
        assertEquals(Domain.GENERAL, Domain.fromKey("UNKNOWN"))
        assertEquals(Domain.GENERAL, Domain.fromKey(""))
    }

    @Test
    fun `all domains have unique keys`() {
        val keys = Domain.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `domain count is 12`() {
        assertEquals(12, Domain.entries.size)
    }
}
