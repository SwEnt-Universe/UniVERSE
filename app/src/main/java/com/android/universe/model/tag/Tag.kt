package com.android.universe.model.tag

/**
 * Enumeration representing various tags that can be associated with user profiles. Each tag has a
 * [displayName] for user-friendly representation and belongs to a [Category].
 */
enum class Tag(val displayName: String, val category: Category) {
  // Music
  JAZZ(displayName = "Jazz", category = Category.MUSIC),
  POP(displayName = "Pop", category = Category.MUSIC),
  ROCK(displayName = "Rock", category = Category.MUSIC),
  RAP(displayName = "Rap", category = Category.MUSIC),
  CLASSICAL(displayName = "Classical", category = Category.MUSIC),
  BLUES(displayName = "Blues", category = Category.MUSIC),
  METAL(displayName = "Metal", category = Category.MUSIC),
  RNB(displayName = "R&B", category = Category.MUSIC),
  FUNK(displayName = "Funk", category = Category.MUSIC),
  REGGAE(displayName = "Reggae", category = Category.MUSIC),
  ELECTRONIC(displayName = "Electronic", category = Category.MUSIC),
  COUNTRY(displayName = "Country", category = Category.MUSIC),
  INDIE(displayName = "Indie", category = Category.MUSIC),
  PUNK(displayName = "Punk", category = Category.MUSIC),
  K_POP(displayName = "K-pop", category = Category.MUSIC),
  LIVE_MUSIC(displayName = "Live music", category = Category.MUSIC),
  CONCERT(displayName = "Concert", category = Category.MUSIC),
  DJ_SET(displayName = "DJ set", category = Category.MUSIC),
  OPEN_MIC(displayName = "Open mic", category = Category.MUSIC),
  KARAOKE(displayName = "Karaoke", category = Category.MUSIC),

  // Sport
  RUNNING(displayName = "Running", category = Category.SPORT),
  FITNESS(displayName = "Fitness", category = Category.SPORT),
  SWIMMING(displayName = "Swimming", category = Category.SPORT),
  CYCLING(displayName = "Cycling", category = Category.SPORT),
  MOUNTAIN_BIKING(displayName = "Mountain biking", category = Category.SPORT),
  HIKING(displayName = "Hiking", category = Category.SPORT),
  YOGA(displayName = "Yoga", category = Category.SPORT),
  MEDITATION(displayName = "Meditation", category = Category.SPORT),
  PILATES(displayName = "Pilates", category = Category.SPORT),
  JUDO(displayName = "Judo", category = Category.SPORT),
  KARATE(displayName = "Karate", category = Category.SPORT),
  BOXING(displayName = "Boxing", category = Category.SPORT),
  FOOTBALL(displayName = "Football", category = Category.SPORT),
  BASKETBALL(displayName = "Basketball", category = Category.SPORT),
  VOLLEYBALL(displayName = "Volleyball", category = Category.SPORT),
  RUGBY(displayName = "Rugby", category = Category.SPORT),
  HANDBALL(displayName = "Handball", category = Category.SPORT),
  TENNIS(displayName = "Tennis", category = Category.SPORT),
  BADMINTON(displayName = "Badminton", category = Category.SPORT),
  TABLE_TENNIS(displayName = "Table tennis", category = Category.SPORT),
  SKIING(displayName = "Skiing", category = Category.SPORT),
  SNOWBOARDING(displayName = "Snowboarding", category = Category.SPORT),
  SKATING(displayName = "Skating", category = Category.SPORT),
  SURFING(displayName = "Surfing", category = Category.SPORT),
  GOLF(displayName = "Golf", category = Category.SPORT),
  KAYAKING(displayName = "Kayaking", category = Category.SPORT),
  DANCING(displayName = "Dancing", category = Category.SPORT),
  HORSEBACK_RIDING(displayName = "Horseback riding", category = Category.SPORT),

  // Food
  VEGAN(displayName = "Vegan", category = Category.FOOD),
  VEGETARIAN(displayName = "Vegetarian", category = Category.FOOD),
  HALAL(displayName = "Halal", category = Category.FOOD),
  ITALIAN(displayName = "Italian", category = Category.FOOD),
  ASIAN(displayName = "Asian", category = Category.FOOD),
  INDIAN(displayName = "Indian", category = Category.FOOD),
  MEXICAN(displayName = "Mexican", category = Category.FOOD),
  LEBANESE(displayName = "Lebanese", category = Category.FOOD),
  MEDITERRANEAN(displayName = "Mediterranean", category = Category.FOOD),
  FAST_FOOD(displayName = "Fast food", category = Category.FOOD),
  DESSERTS(displayName = "Desserts", category = Category.FOOD),
  GRILLING(displayName = "Grilling", category = Category.FOOD),
  HOME_COOKING(displayName = "Home cooking", category = Category.FOOD),
  STREET_FOOD(displayName = "Street food", category = Category.FOOD),
  CAFES(displayName = "Cafés", category = Category.FOOD),
  WINE_TASTING(displayName = "Wine tasting", category = Category.FOOD),
  BEER_TASTING(displayName = "Beer tasting", category = Category.FOOD),
  COCKTAILS(displayName = "Cocktails", category = Category.FOOD),
  BARS(displayName = "Bars", category = Category.FOOD),
  BRUNCH(displayName = "Brunch", category = Category.FOOD),
  BAKING(displayName = "Baking", category = Category.FOOD),
  COOKING_CLASS(displayName = "Cooking class", category = Category.FOOD),
  FOOD_TRUCKS(displayName = "Food trucks", category = Category.FOOD),
  FARMERS_MARKET(displayName = "Farmers market", category = Category.FOOD),
  FINE_DINING(displayName = "Fine dining", category = Category.FOOD),
  POTLUCK(displayName = "Potluck", category = Category.FOOD),

  // Art
  DRAWING(displayName = "Drawing", category = Category.ART),
  PAINTING(displayName = "Painting", category = Category.ART),
  GRAFFITI(displayName = "Graffiti", category = Category.ART),
  PHOTOGRAPHY(displayName = "Photography", category = Category.ART),
  SCULPTURE(displayName = "Sculpture", category = Category.ART),
  MUSIC(displayName = "Music", category = Category.ART),
  THEATER(displayName = "Theater", category = Category.ART),
  CINEMA(displayName = "Cinema", category = Category.ART),
  DOCUMENTARIES(displayName = "Documentaries", category = Category.ART),
  ANIMATION(displayName = "Animation", category = Category.ART),
  POETRY(displayName = "Poetry", category = Category.ART),
  LITERATURE(displayName = "Literature", category = Category.ART),
  FASHION(displayName = "Fashion", category = Category.ART),
  ARCHITECTURE(displayName = "Architecture", category = Category.ART),
  DESIGN(displayName = "Design", category = Category.ART),
  UI_UX(displayName = "UI/UX", category = Category.ART),
  DIGITAL_ART(displayName = "Digital art", category = Category.ART),
  COMEDY(displayName = "Comedy", category = Category.ART),
  STAND_UP(displayName = "Stand up", category = Category.ART),
  CRAFTS(displayName = "Crafts", category = Category.ART),
  POTTERY(displayName = "Pottery", category = Category.ART),
  KNITTING(displayName = "Knitting", category = Category.ART),
  WOODWORKING(displayName = "Woodworking", category = Category.ART),
  MUSEUMS(displayName = "Museums", category = Category.ART),
  GALLERIES(displayName = "Galleries", category = Category.ART),
  WRITING(displayName = "Writing", category = Category.ART),

  // Travel
  FESTIVALS(displayName = "Festivals", category = Category.TRAVEL),
  CAMPING(displayName = "Camping", category = Category.TRAVEL),
  BEACH(displayName = "Beach", category = Category.TRAVEL),
  CITY_TRIPS(displayName = "City trips", category = Category.TRAVEL),
  ROAD_TRIPS(displayName = "Road trips", category = Category.TRAVEL),
  SAFARI(displayName = "Safari", category = Category.TRAVEL),
  BACKPACKING(displayName = "Backpacking", category = Category.TRAVEL),
  ADVENTURE_TRAVEL(displayName = "Adventure travel", category = Category.TRAVEL),
  WELLNESS_RETREATS(displayName = "Wellness retreats", category = Category.TRAVEL),
  CULTURAL_TRIPS(displayName = "Cultural trips", category = Category.TRAVEL),
  SOLO_TRAVEL(displayName = "Solo travel", category = Category.TRAVEL),
  GROUP_TRAVEL(displayName = "Group travel", category = Category.TRAVEL),
  BUDGET_TRAVEL(displayName = "Budget travel", category = Category.TRAVEL),
  LUXURY_TRAVEL(displayName = "Luxury travel", category = Category.TRAVEL),
  VOLUNTEER_TRAVEL(displayName = "Volunteer travel", category = Category.TRAVEL),
  CRUISES(displayName = "Cruises", category = Category.TRAVEL),
  NATIONAL_PARKS(displayName = "National parks", category = Category.TRAVEL),
  STAYCATION(displayName = "Staycation", category = Category.TRAVEL),
  WEEKEND_TRIPS(displayName = "Weekend trips", category = Category.TRAVEL),

  // Games
  VIDEO_GAMES(displayName = "Video games", category = Category.GAMES),
  BOARD_GAMES(displayName = "Board games", category = Category.GAMES),
  CARD_GAMES(displayName = "Card games", category = Category.GAMES),
  DND(displayName = "DnD", category = Category.GAMES),
  PUZZLE(displayName = "Puzzle", category = Category.GAMES),
  BRAIN_GAMES(displayName = "Brain games", category = Category.GAMES),
  ONLINE_GAMES(displayName = "Online games", category = Category.GAMES),
  CO_OP_GAMES(displayName = "Co-op games", category = Category.GAMES),
  CHESS(displayName = "Chess", category = Category.GAMES),

  // Technology
  PROGRAMMING(displayName = "Programming", category = Category.TECHNOLOGY),
  AI(displayName = "AI", category = Category.TECHNOLOGY),
  MACHINE_LEARNING(displayName = "Machine learning", category = Category.TECHNOLOGY),
  DATA_SCIENCE(displayName = "Data science", category = Category.TECHNOLOGY),
  CONSOLES(displayName = "Consoles", category = Category.TECHNOLOGY),
  CRYPTOCURRENCY(displayName = "Cryptocurrency", category = Category.TECHNOLOGY),
  CYBERSECURITY(displayName = "Cybersecurity", category = Category.TECHNOLOGY),
  VR(displayName = "VR", category = Category.TECHNOLOGY),
  ROBOTICS(displayName = "Robotics", category = Category.TECHNOLOGY),
  CLOUD_COMPUTING(displayName = "Cloud computing", category = Category.TECHNOLOGY),
  TECH_NEWS(displayName = "Tech news", category = Category.TECHNOLOGY),
  STARTUP(displayName = "Startup", category = Category.TECHNOLOGY),

  // Topics
  PHYSICS(displayName = "Physics", category = Category.TOPIC),
  MATHEMATICS(displayName = "Mathematics", category = Category.TOPIC),
  CHEMISTRY(displayName = "Chemistry", category = Category.TOPIC),
  ASTRONOMY(displayName = "Astronomy", category = Category.TOPIC),
  BIOLOGY(displayName = "Biology", category = Category.TOPIC),
  HISTORY(displayName = "History", category = Category.TOPIC),
  PHILOSOPHY(displayName = "Philosophy", category = Category.TOPIC),
  COMPUTER_SCIENCE(displayName = "Computer science", category = Category.TOPIC),
  ECOLOGY(displayName = "Ecology", category = Category.TOPIC),
  POLITICS(displayName = "Politics", category = Category.TOPIC),
  ECONOMICS(displayName = "Economics", category = Category.TOPIC),
  SOCIOLOGY(displayName = "Sociology", category = Category.TOPIC);

  /** Defines the categories to which tags can belong. */
  enum class Category(val fieldName: String, val displayName: String) {
    MUSIC("music_tags", "Music"),
    SPORT("sport_tags", "Sport"),
    FOOD("food_tags", "Food"),
    ART("art_tags", "Art"),
    TRAVEL("travel_tags", "Travel"),
    GAMES("games_tags", "Games"),
    TECHNOLOGY("technology_tags", "Technology"),
    TOPIC("topic_tags", "Topic")
  }

  companion object {
    // Map of displayName to Tag for efficient lookup
    private val displayNameToTag = entries.associateBy { it.displayName }

    // Map of category to list of Tags
    private val tagsByCategory = entries.groupBy { it.category }.mapValues { it.value.toList() }

    /**
     * Converts a displayName to a Tag enum entry.
     *
     * @param displayName The user-friendly name of the tag.
     * @return The corresponding Tag or null if not found.
     */
    fun fromDisplayName(displayName: String): Tag? {
      return displayNameToTag[displayName]
    }

    /**
     * Retrieves all Tags for a given category.
     *
     * @param category The category to filter by.
     * @return List of Tags in the specified category.
     */
    fun getTagsForCategory(category: Category): List<Tag> {
      return tagsByCategory[category] ?: emptyList()
    }

    /**
     * Retrieves all Tags for a given category field name.
     *
     * @param fieldName The field name (e.g., "interest_tags").
     * @return List of Tags in the specified category.
     */
    fun getTagsForFieldName(fieldName: String): List<Tag> {
      val category = Category.entries.find { it.fieldName == fieldName }
      return category?.let { getTagsForCategory(it) } ?: emptyList()
    }

    /**
     * Retrieves the display names for all Tags in a given category.
     *
     * @param category The category to filter by.
     * @return List of display names for Tags in the category.
     */
    fun getDisplayNamesForCategory(category: Category): List<String> {
      return getTagsForCategory(category).map { it.displayName }
    }

    val tagFromEachCategory =
        setOf(JAZZ, RUNNING, VEGAN, DRAWING, FESTIVALS, VIDEO_GAMES, PROGRAMMING, PHYSICS)

    /**
     * Filters a list of Tags to include only those in a specific category.
     *
     * @param tags The list of Tags to filter.
     * @param category The category to filter by.
     * @return List of Tags in the specified category.
     */
    fun filterByCategory(tags: List<Tag>, category: Category): List<Tag> {
      return tags.filter { it.category == category }
    }

    /**
     * Converts a list of Tags to their display names, filtered by category.
     *
     * @param tags The list of Tags to convert.
     * @param category The category to filter by.
     * @return List of display names for Tags in the category.
     */
    fun toDisplayNamesByCategory(tags: List<Tag>, category: Category): List<String> {
      return filterByCategory(tags, category).map { it.displayName }
    }
  }
}
