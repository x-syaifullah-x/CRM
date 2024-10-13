package com.lizpostudio.kgoptometrycrm

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test

class ExampleInstrumentTest {

    @Test
    fun exampleInstrumentTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertNotNull(context)
    }
}