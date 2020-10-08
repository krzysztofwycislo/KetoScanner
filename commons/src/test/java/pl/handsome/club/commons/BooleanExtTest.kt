package pl.handsome.club.commons

import org.junit.Test

import org.junit.Assert.*


class BooleanExtensionsTest {

    @Test
    fun `when we want to execute something when value is true only on true condition should be executed`() {
        true.ifTrue { assertTrue(true) }
            .ifFalse { fail() }
    }

    @Test
    fun `when we want to execute something when value is false only on false condition should be executed`() {
        false.ifTrue { fail() }
            .ifFalse { assertTrue(true) }
    }

}