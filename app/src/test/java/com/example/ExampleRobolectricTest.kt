package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.zoro.engine.ZoroBenchmarkEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZORO LLM", appName)
  }

  @Test
  fun `verify hardware profiler extracts device specs`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = ZoroBenchmarkEngine(context)
    val hw = engine.getDeviceHardwareProfile()
    assertNotNull("Hardware profile should not be null", hw)
    assertTrue("CPU cores should be greater than 0", hw.cpuCores > 0)
    assertTrue("Total RAM should be greater than 0", hw.totalRamMb >= 0)
  }
}
