package com.android.universe.model.tag

object TagTemporaryRepositoryProvider {
  /** Private repository instance */
  private val _repository: TagTemporaryRepository = TagLocalTemporaryRepository()

  /** Public repository instance (read-only) */
  var repository: TagTemporaryRepository = _repository
}
