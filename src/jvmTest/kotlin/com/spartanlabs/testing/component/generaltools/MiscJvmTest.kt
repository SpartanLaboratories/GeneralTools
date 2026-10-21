package com.spartanlabs.testing.component.generaltools

import com.spartanlabs.generaltools.capitalizeEveryWord
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 2 (Isolated Component Behaviour) coverage for the helpers whose implementations are still JVM-only
 * (the deprecated capitalize()). Interim class: each test moves into the common MiscTest when its
 * subject becomes common, and this class is then deleted.
 */
class MiscJvmTest {
    @Test
    fun `capitalizeEveryWord capitalizes each word`() {
        assertEquals("Hello World Foo", "hello world foo".capitalizeEveryWord())
    }
}
