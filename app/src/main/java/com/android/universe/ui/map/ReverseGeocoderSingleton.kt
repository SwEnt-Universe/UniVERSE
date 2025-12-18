package com.android.universe.ui.map

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import com.android.universe.BuildConfig
import com.android.universe.di.DefaultDP
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Result
import com.tomtom.sdk.common.annotations.BetaServiceUriApi
import com.tomtom.sdk.common.flatMap
import com.tomtom.sdk.common.fold
import com.tomtom.sdk.common.uri.ServiceUri
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.CategoryId
import com.tomtom.sdk.location.poi.StandardCategoryId
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.geometry.CircleGeometry
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.search.poicategories.PoiCategoryOptions
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoder
import com.tomtom.sdk.search.reversegeocoder.ReverseGeocoderOptions
import com.tomtom.sdk.search.reversegeocoder.online.OnlineReverseGeocoder
import kotlinx.coroutines.withContext

@OptIn(BetaServiceUriApi::class)
object ReverseGeocoderSingleton {

  private val studentEventCategory =
      setOf(
          // 1. The Campus Hub: Classes, club meetings, protests
          StandardCategoryId.CollegeUniversity, // 7377

          // 2. The "Third Place": Study sessions (coffee) & Socials (pubs)
          StandardCategoryId.CafePub, // 9376

          // 3. Weekends: Parties, mixers, celebrations
          StandardCategoryId.Nightlife, // 9379

          // 4. Academics: Study groups, tutoring, quiet work
          StandardCategoryId.Library, // 9913

          // 5. Budget/Outdoor: Picnics, frisbee, outdoor clubs
          StandardCategoryId.ParkRecreationArea, // 9362

          // 6. Active Lifestyle: Intramural sports, gym buddies
          StandardCategoryId.SportsCenter, // 7320

          // 7. Dining: Team dinners, "cheap eats" outings
          StandardCategoryId.Restaurant, // 7315

          // 8. Educational Trips: Art history assignments or free student days
          StandardCategoryId.Museum, // 7317

          // 9. Arts/Diversity: Cultural exchange, art exhibitions
          StandardCategoryId.CulturalCenter, // 7319

          // 10. Organization: Volunteer groups, large hall rentals
          StandardCategoryId.CommunityCenter // 7363
          )

  private val additionalStudentEventCategories =
      setOf(
          // 1. Retail Therapy & Supplies: Bookstores, thrift shops, fashion
          StandardCategoryId.Shop, // 9361

          // 2. Group Fun: Bowling alleys, arcades, dance studios
          StandardCategoryId.LeisureCenter, // 9378

          // 3. Varsity Spirit: Watching big college games or concerts
          StandardCategoryId.Stadium, // 7374

          // 4. Spiritual Life: Student religious organizations and gatherings
          StandardCategoryId.PlaceofWorship, // 7339

          // 5. Cheap Eats & Local Vibe: Farmers markets and flea markets
          StandardCategoryId.Market, // 7332

          // 6. Entertainment: Film clubs, social outings
          StandardCategoryId.MovieTheater, // 7342

          // 7. Performing Arts: Drama club outings (plays, operas, not just movies)
          StandardCategoryId.Theater, // 7318

          // 8. City Exploration: "Discover your city" events for freshmen
          StandardCategoryId.TouristAttraction, // 7376

          // 9. Weekend Escapes: Beach days (if applicable geography)
          StandardCategoryId.Beach, // 9357

          // 10. The Meeting Point: Start location for group trips/excursions
          StandardCategoryId.PublicTransportationStop // 9942
          )
  @SuppressLint("StaticFieldLeak") private var reverseInstance: ReverseGeocoder? = null
  private val reverseGeocoder: ReverseGeocoder
    get() =
        reverseInstance
            ?: throw IllegalStateException(
                "ReverseGeocoder not initialized. Call ReverseGeocoderSingleton.init(context) in your Application class.")

  @SuppressLint("StaticFieldLeak") private var searchInstance: Search? = null
  private val search: Search
    get() =
        searchInstance
            ?: throw IllegalStateException(
                "ReverseGeocoder not initialized. Call ReverseGeocoderSingleton.init(context) in your Application class.")

  private val tomtomReverseServiceUri =
      ServiceUri.TomTomOrbisMapService("https://api.tomtom.com/maps/orbis/places/".toUri())

  private val tomtomSearchServiceUri =
      ServiceUri.TomTomOrbisMapService("https://api.tomtom.com/maps/orbis/places/".toUri())

  fun init(context: Context) {
    if (reverseInstance == null) {
      reverseInstance =
          OnlineReverseGeocoder.create(
              context = context,
              apiKey = BuildConfig.TOMTOM_API_KEY,
              serviceUri = tomtomReverseServiceUri)
    }
    if (searchInstance == null) {
      searchInstance =
          OnlineSearch.create(
              context = context,
              apiKey = BuildConfig.TOMTOM_API_KEY,
              serviceUri = tomtomSearchServiceUri)
    }
  }

  private suspend fun getClosestPoi(location: GeoPoint, categories: Set<CategoryId>) =
      withContext(DefaultDP.io) {
        val poiOptions = PoiCategoryOptions()
        val poiCategories =
            search.requestPoiCategories(poiOptions).flatMap {
              Result.success(
                  it.poiCategories.filter { it.id.standard.value < 10000 }.map { it.id }.toSet())
            }
        if (poiCategories.isFailure()) {
          return@withContext null
        }
        val searchOptions =
            SearchOptions(
                categoryIds = categories,
                searchAreas = setOf(CircleGeometry(location, Distance.meters(50))),
                resultTypes = setOf(SearchResultType.Poi),
                limit = 1)
        val bestResult: SearchResult? =
            search
                .search(searchOptions)
                .fold(
                    ifSuccess = { if (it.results.isNotEmpty()) it.results.first() else null },
                    ifFailure = { null })
        var poiName: String? = null
        bestResult?.let { poiName = it.poi?.names?.first() }
        return@withContext poiName
      }

  suspend fun getSmartAddress(location: GeoPoint): String {
    val address = getAddressFromLocation(location)
    var poi = getClosestPoi(location, studentEventCategory.map { CategoryId(it) }.toSet())
    if (poi == null)
        poi =
            getClosestPoi(location, additionalStudentEventCategories.map { CategoryId(it) }.toSet())

    return if (poi != null) "$address;$poi" else address
  }

  private suspend fun getAddressFromLocation(location: GeoPoint) =
      withContext(DefaultDP.io) {
        val options =
            ReverseGeocoderOptions(
                position = location,
                radius = Distance.Companion.meters(1000),
                preferClosestAccurateAddress = true)
        val processedResult: Place? =
            reverseGeocoder
                .reverseGeocode(options)
                .fold(ifSuccess = { it.places.first().place }, ifFailure = { null })
        var address: String? = null
        processedResult?.let { address = it.address?.freeformAddress?.substringBefore('(') }
        return@withContext address ?: "Unknown Address"
      }
}
