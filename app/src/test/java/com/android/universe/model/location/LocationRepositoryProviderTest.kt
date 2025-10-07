package com.android.universe.model.location

import android.content.Context
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.junit.runner.RunWith

@RunWith(RobolectricTestRunner::class)
class LocationRepositoryProviderTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = org.robolectric.RuntimeEnvironment.getApplication()
        resetRepositoryProvider()
    }

    @After
    fun tearDown() {
        resetRepositoryProvider()
    }

    @Test
    fun init_withFakeRepository_setsRepositoryCorrectly() {
        LocationRepositoryProvider.init(useFake = true)
        val repo = LocationRepositoryProvider.repository
        assertTrue(repo is FakeLocationRepository)
    }

    @Test
    fun init_withRealRepository_setsRepositoryCorrectly() {
        LocationRepositoryProvider.init(useFake = false, context = context)
        val repo = LocationRepositoryProvider.repository
        assertTrue(repo is TomTomLocationRepository)
    }

    @Test(expected = IllegalArgumentException::class)
    fun init_withoutContext_throwsException() {
        LocationRepositoryProvider.init(useFake = false, context = null)
    }

    private fun resetRepositoryProvider() {
        val field = LocationRepositoryProvider::class.java.getDeclaredField("_repository")
        field.isAccessible = true
        field.set(null, null)
    }
}
