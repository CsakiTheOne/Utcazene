package com.csakitheone.streetmusic

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.csakitheone.streetmusic.data.EventsProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EventsProviderTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.csakitheone.streetmusic", appContext.packageName)
    }

    @Test
    fun loadEventsTest() {
        val rawEvents = EventsProvider.readEventsFromCsv(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(rawEvents.size, 34 * 4 + 2)
    }
}