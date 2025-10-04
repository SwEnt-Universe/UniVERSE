package com.android.universe.model

enum class TagType {
    PROFILE,
    EVENT
}

data class Tag(
    val Name: String,
    val uid: String,
    val ownerId: String,
    val type: TagType
)

val tagsInterest = listOf<String>(
    "Reading",
    "Writing",
    "Music",
    "Cinema",
    "Photography",
    "Video games",
    "Programming",
    "Artificial intelligence",
    "Astronomy",
    "Electronics",
    "Traveling",
    "Cooking",
    "Politics",
    "Philosophy",
    "Drawing",
    "Sculpture",
    "Poetry",
    "Fashion",
    "Board games",
    "Role-playing games",
    "Car"
)

val tagsSport = listOf<String>(
    "Running",
    "Fitness",
    "Swimming",
    "Cycling",
    "Mountain biking",
    "Hiking",
    "Yoga",
    "Meditation",
    "Pilates",
    "Judo",
    "Karate",
    "Boxing",
    "Football",
    "Basketball",
    "Volleyball",
    "Rugby",
    "Handball",
    "Tennis",
    "Badminton",
    "Table tennis",
    "Skiing",
    "Snowboarding",
    "Skating",
    "Surfing",
    "Golf",
    "Kayaking",
    "Dancing",
    "Horseback riding"
)

val tagsMusic = listOf<String>(
    "Jazz",
    "Pop",
    "Rock",
    "Rap",
    "Classical",
    "Blues",
    "Metal",
    "R&B",
    "Funk",
    "Reggae",
    "Electronic",
    "Country",
    "Indie",
    "Punk",
    "K-pop"
)

val tagsTransport = listOf<String>(
    "Car",
    "Train",
    "Boat",
    "Bus",
    "Bicycle",
    "Foot",
    "Plane"
)

val tagsCanton = listOf<String>(
    "Aargau",
    "Appenzell Ausserrhoden",
    "Appenzell Innerrhoden",
    "Basel-Landschaft",
    "Basel-Stadt",
    "Bern",
    "Fribourg",
    "Geneva",
    "Glarus",
    "Graubünden",
    "Jura",
    "Lucerne",
    "Neuchâtel",
    "Nidwalden",
    "Obwalden",
    "Schaffhausen",
    "Schwyz",
    "Solothurn",
    "St. Gallen",
    "Thurgau",
    "Ticino",
    "Uri",
    "Valais",
    "Vaud",
    "Zug",
    "Zürich"
)