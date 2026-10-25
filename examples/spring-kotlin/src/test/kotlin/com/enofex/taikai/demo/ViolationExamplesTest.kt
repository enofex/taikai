package com.enofex.taikai.demo

import com.enofex.taikai.Taikai
import com.enofex.taikai.kotlin.taikai
import com.enofex.taikai.demo.violations.MutableResponse
import com.enofex.taikai.demo.violations.WronglyNamed
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ViolationExamplesTest {
    @Test
    fun `mutable response example reports the offending property`() {
        val failure = assertThrows<AssertionError> {
            taikai {
                classes(MutableResponse::class)
                kotlin { dataClassesShouldBeImmutable() }
            }.checkAll()
        }
        assertTrue(failure.message!!.contains("mutable property title"))
    }

    @Test
    fun `Spring service naming example reports the offending class`() {
        val failure = assertThrows<AssertionError> {
            Taikai.builder().classes(WronglyNamed::class.java)
                .spring { it.services { service -> service.namesShouldEndWithService() } }
                .build().check()
        }
        assertTrue(failure.message!!.contains("WronglyNamed"))
    }
}
