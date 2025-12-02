package com.android.universe.model.ai

import com.android.universe.model.user.UserProfile
import java.time.LocalDate

object AIUserProfile {
  val OPENAI_USER =
      UserProfile(
          uid = "OpenAI",
          username = "OpenAI",
          firstName = "Open",
          lastName = "AI",
          country = "US",
          description = "AI-generated event creator",
          dateOfBirth = LocalDate.of(2002, 1, 1),
          tags = emptySet())
}
