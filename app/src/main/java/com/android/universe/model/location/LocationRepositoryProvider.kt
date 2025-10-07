package com.android.universe.model.location

import android.content.Context

/**
 * Provides a singleton instance of [LocationRepository].
 */
object LocationRepositoryProvider {

    private lateinit var _repository: LocationRepository

    /**
     * Initializes the repository provider.
     *
     * @param useFake true to use [FakeLocationRepository] for testing.
     * @param context required if using a real repository.
     */
    fun init(useFake: Boolean = false, context: Context? = null) {
        _repository = if (useFake) {
            FakeLocationRepository()
        } else {
            requireNotNull(context) { "Context is required for real repository" }
            TomTomLocationRepository(context)
        }
    }

    /** Access the repository instance */
    val repository: LocationRepository
        get() = _repository
}
