package br.com.conseng.myfirstgooglemapsapp

import android.content.Context
import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL

/**
 * Search for specific places around the current location.
 */
class GooglePlaceApis {

    /**
     * Informs the places on [radius] of the specific place [type] on Json format.
     * @param [context] to access the application resources.
     * @param [latitude] the current location latitude.
     * @param [longitude] the current location longitude.
     * @param [radius] the radius distance (meters) to search for the specific place [type].
     * @param [type] identify the type of the place to search around.
     * @return The list of places around the current location on Json format.
     *         On error, the return will be an empty string.
     * @see [https://developers.google.com/places/web-service/search?hl=pt-br]
     */
    fun getPlacesJson(context: Context, latitude: Double, longitude: Double,
                      radius: Int, type: String): String {
        var result = ""
        var connection: HttpURLConnection? = null

        try {
            val urlString = uriGoogleApisSearchNearby(context, latitude, longitude, radius, type)
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Content-Type", "application/json")
            connection.requestMethod = "GET"
            connection.doInput = true
            val br = connection.inputStream.bufferedReader()
            result = br.use { br.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (null != connection) {
                connection.disconnect()
            }
        }
        return result
    }

    /**
     * Search options.
     * @see [https://developers.google.com/places/web-service/search?hl=pt-br]
     */
    enum class GoogleSearchOptions {
        nearbysearch,
        textsearch,
        radarsearch
    }

    /**
     * Returns the base URL of the Google Maps Api search.
     * @param [option] identifies the desired search option.
     * @param [json] defines if the return string format: true=json (default), false=xml.
     * @return The URI for "https://maps.googleapis.com/maps/api/place/[option]/[json]"
     * @see [https://developers.google.com/places/web-service/search?hl=pt-br]
     */
    private fun uriGoogleApisSearchBase(option: GoogleSearchOptions, json: Boolean = true): Uri.Builder {
        val builder = Uri.Builder()
        builder.scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("place")
                .appendPath(option.toString())
                .appendPath(if (json) "json" else "xml")
        return builder
    }

    /**
     * Returns the base URL of the Google Maps Api Nearby search with result on Json format.
     * @param [context] to access the application resources.
     * @param [latitude] the current location latitude.
     * @param [longitude] the current location longitude
     * @param [radius] the radius distance (meters) to search for the specific place [type].
     * @param [type] identify the type of the place to search around.
     * @return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=[latitude],[longitude]&radius=[radius]&type=[type]&key=xx...xx"
     * @see [https://developers.google.com/places/web-service/search?hl=pt-br]
     * @see [https://developers.google.com/places/web-service/supported_types?hl=pt-br]
     */
    private fun uriGoogleApisSearchNearby(context: Context, latitude: Double, longitude: Double,
                                          radius: Int, type: String): String {
        val builder = uriGoogleApisSearchBase(GoogleSearchOptions.nearbysearch)
        builder.appendQueryParameter("location", "$latitude,$longitude")
        builder.appendQueryParameter("radius", "$radius")
        builder.appendQueryParameter("type", "$type")
        builder.appendQueryParameter("key", context.resources.getString(R.string.google_maps_key))
        return builder.toString()
    }
}