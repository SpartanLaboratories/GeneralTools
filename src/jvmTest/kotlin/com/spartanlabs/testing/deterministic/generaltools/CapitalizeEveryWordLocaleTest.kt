package com.spartanlabs.testing.deterministic.generaltools

import com.spartanlabs.generaltools.capitalizeEveryWord
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 4a (Deterministic Logic) coverage that `capitalizeEveryWord` maps input to output independently of the JVM
 * default locale. JVM only because `java.util.Locale` is JVM API; the other targets have no default locale to vary.
 * JUnit runs the JVM tests sequentially, so changing the global default for the test's duration is safe.
 */
class CapitalizeEveryWordLocaleTest {
    @Test
    fun `capitalizeEveryWord gives the same result under a Turkish default locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("Istanbul Izmir", "istanbul izmir".capitalizeEveryWord())
        } finally {
            Locale.setDefault(previous)
        }
    }
}
