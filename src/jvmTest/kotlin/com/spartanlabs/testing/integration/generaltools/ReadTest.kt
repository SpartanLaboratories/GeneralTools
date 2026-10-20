package com.spartanlabs.testing.integration.generaltools

import com.spartanlabs.generaltools.read
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Level 3 (Integration & External Interfaces) coverage for `read`. It reads a real file from the file system.
 */
class ReadTest {
    @Test
    fun `read returns each line of the file`() {
        val tempFile = File.createTempFile("misc-test", ".txt")
        tempFile.deleteOnExit()
        tempFile.writeText("line one\nline two\nline three")

        val lines = read(tempFile.absolutePath)

        assertEquals(listOf("line one", "line two", "line three"), lines)
    }
}
