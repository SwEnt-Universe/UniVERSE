package com.android.universe.model

/**
 * Enumeration representing various tags that can be associated with user profiles. Each tag has a
 * [displayName] for user-friendly representation and belongs to a [Category].
 */
enum class Tag(val displayName: String, val category: Category) {
  READING("Reading", Category.INTEREST),
  WRITING("Writing", Category.INTEREST),
  MUSIC("Music", Category.INTEREST),
  CINEMA("Cinema", Category.INTEREST),
  PHOTOGRAPHY("Photography", Category.INTEREST),
  VIDEO_GAMES("Video_Games", Category.INTEREST),
  PROGRAMMING("Programming", Category.INTEREST),
  ARTIFICIAL_INTELLIGENCE("Artificial intelligence", Category.INTEREST),
  ASTRONOMY("Astronomy", Category.INTEREST),
  ELECTRONICS("Electronics", Category.INTEREST),
  TRAVELING("Traveling", Category.INTEREST),
  COOKING("Cooking", Category.INTEREST),
  POLITICS("Politics", Category.INTEREST),
  PHILOSOPHY("Philosophy", Category.INTEREST),
  DRAWING("Drawing", Category.INTEREST),
  SCULPTURE("Sculpture", Category.INTEREST),
  POETRY("Poetry", Category.INTEREST),
  FASHION("Fashion", Category.INTEREST),
  BOARD_GAMES("Board games", Category.INTEREST),
  ROLE_PLAYING_GAMES("Role-playing games", Category.INTEREST),
  CAR_RACE("Car race", Category.INTEREST),
  RUNNING("Running", Category.SPORT),
  FITNESS("Fitness", Category.SPORT),
  SWIMMING("Swimming", Category.SPORT),
  CYCLING("Cycling", Category.SPORT),
  MOUNTAIN_BIKING("Mountain biking", Category.SPORT),
  HIKING("Hiking", Category.SPORT),
  YOGA("Yoga", Category.SPORT),
  MEDITATION("Meditation", Category.SPORT),
  PILATES("Pilates", Category.SPORT),
  JUDO("Judo", Category.SPORT),
  KARATE("Karate", Category.SPORT),
  BOXING("Boxing", Category.SPORT),
  FOOTBALL("Football", Category.SPORT),
  BASKETBALL("Basketball", Category.SPORT),
  VOLLEYBALL("Volleyball", Category.SPORT),
  RUGBY("Rugby", Category.SPORT),
  HANDBALL("Handball", Category.SPORT),
  TENNIS("Tennis", Category.SPORT),
  BADMINTON("Badminton", Category.SPORT),
  TABLE_TENNIS("Table tennis", Category.SPORT),
  SKIING("Skiing", Category.SPORT),
  SNOWBOARDING("Snowboarding", Category.SPORT),
  SKATING("Skating", Category.SPORT),
  SURFING("Surfing", Category.SPORT),
  GOLF("Golf", Category.SPORT),
  KAYAKING("Kayaking", Category.SPORT),
  DANCING("Dancing", Category.SPORT),
  HORSEBACK_RIDING("Horseback riding", Category.SPORT),
  JAZZ("Jazz", Category.MUSIC),
  POP("Pop", Category.MUSIC),
  ROCK("Rock", Category.MUSIC),
  RAP("Rap", Category.MUSIC),
  CLASSICAL("Classical", Category.MUSIC),
  BLUES("Blues", Category.MUSIC),
  METAL("Metal", Category.MUSIC),
  RNB("R&B", Category.MUSIC),
  FUNK("Funk", Category.MUSIC),
  REGGAE("Reggae", Category.MUSIC),
  ELECTRONIC("Electronic", Category.MUSIC),
  COUNTRY("Country", Category.MUSIC),
  INDIE("Indie", Category.MUSIC),
  PUNK("Punk", Category.MUSIC),
  K_POP("K_pop", Category.MUSIC),
  CAR("Car", Category.TRANSPORT),
  TRAIN("Train", Category.TRANSPORT),
  BOAT("Boat", Category.TRANSPORT),
  BUS("Bus", Category.TRANSPORT),
  BICYCLE("Bicycle", Category.TRANSPORT),
  FOOT("Foot", Category.TRANSPORT),
  PLANE("Plane", Category.TRANSPORT),
  AARGAU("Aargau", Category.CANTON),
  APPENZELL_AUSSERRHODEN("Appenzell Ausserrhoden", Category.CANTON),
  APPENZELL_INNERRHODEN("Appenzell Innerrhoden", Category.CANTON),
  BASEL_LANDSCHAFT("Basel-Landschaft", Category.CANTON),
  BASEL_STADT("Basel-Stadt", Category.CANTON),
  BERN("Bern", Category.CANTON),
  FRIBOURG("Fribourg", Category.CANTON),
  GENEVA("Geneva", Category.CANTON),
  GLARUS("Glarus", Category.CANTON),
  GRAUBUNDEN("Graubünden", Category.CANTON),
  JURA("Jura", Category.CANTON),
  LUCERNE("Lucerne", Category.CANTON),
  NEUCHATEL("Neuchâtel", Category.CANTON),
  NIDWALDEN("Nidwalden", Category.CANTON),
  OBWALDEN("Obwalden", Category.CANTON),
  SCHAFFHAUSEN("Schaffhausen", Category.CANTON),
  SCHWYZ("Schwyz", Category.CANTON),
  SOLOTHURN("Solothurn", Category.CANTON),
  ST_GALLEN("St. Gallen", Category.CANTON),
  THURGAU("Thurgau", Category.CANTON),
  TICINO("Ticino", Category.CANTON),
  URI("Uri", Category.CANTON),
  VALAIS("Valais", Category.CANTON),
  VAUD("Vaud", Category.CANTON),
  ZUG("Zug", Category.CANTON),
  ZURICH("Zürich", Category.CANTON);

  /** Defines the categories to which tags can belong. */
  enum class Category(val fieldName: String, val displayName: String) {
    INTEREST("interest_tags", "Interests"),
    SPORT("sport_tags", "Sport"),
    MUSIC("music_tags", "Music"),
    TRANSPORT("transport_tags", "Transport"),
    CANTON("canton_tags", "Canton")
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
