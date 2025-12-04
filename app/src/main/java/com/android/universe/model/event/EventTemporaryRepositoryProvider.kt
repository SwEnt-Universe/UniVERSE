package com.android.universe.model.event

object EventTemporaryRepositoryProvider {
  /** Private repository instance */
  private val _repository: EventTemporaryRepository = EventLocalTemporaryRepository()

  /** Public repository instance (read-only) */
  var repository: EventTemporaryRepository = _repository
}
