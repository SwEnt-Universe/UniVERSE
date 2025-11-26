package com.android.universe.model.ai.prompt

import com.android.universe.model.user.UserProfile

data class EventQuery(
    val user: UserProfile,
    val task: TaskConfig = TaskConfig.Default,
    val context: ContextConfig = ContextConfig.Default
)
