import com.android.universe.model.ai.FakeOpenAIService
import com.android.universe.model.ai.OpenAIEventGen
import com.android.universe.model.tag.Tag
import com.android.universe.model.user.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.LocalDate

class OpenAIEventGenTest {

	private val fakeService = FakeOpenAIService()
	private val eventGen = OpenAIEventGen(fakeService)

	private val DummyDate = LocalDate.of(2000, 8, 11)

	val allTags =
		(Tag.getTagsForCategory(Tag.Category.TOPIC) + Tag.getTagsForCategory(Tag.Category.FOOD))
			.toSet()

	val tags = setOf(Tag.ROCK, Tag.POP)
	val allTags_CH_user =
		UserProfile(
			uid = "69",
			username = "ai_69",
			firstName = "AI",
			lastName = "Base",
			country = "CH",
			description = "Has all tags, country = switzerland",
			dateOfBirth = DummyDate,
			tags = allTags)


	@Test
	fun `test parsing of json response`() = runBlocking {
		val events = eventGen.generateEventsForUser(allTags_CH_user)
		assert(events.isNotEmpty())
		println(events)
	}
}
