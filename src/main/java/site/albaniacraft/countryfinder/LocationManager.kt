package site.albaniacraft.countryfinder

import com.google.gson.Gson
import org.bukkit.Location
import site.albaniacraft.countryfinder.objects.City
import site.albaniacraft.countryfinder.objects.Country
import java.io.File
import java.io.FileReader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

public object LocationManager {
    private val plugin = CountryFinderPlugin.instance
    private val file = File(plugin.dataFolder, "countries+cities.json")
    private val gson = Gson()
    val countries = FileReader(file).use { reader ->
        gson.fromJson(reader, Array<Country>::class.java).toList()
    }
    fun getLocation(countryName: String, cityName: String): Pair<String, String>? {
        val country = countries.find { it.name.equals(countryName, ignoreCase = true) }
        return country?.cities?.find { it.name.equals(cityName.replace("_", " "), ignoreCase = true) }
            ?.let { Pair(it.latitude, it.longitude) }
    }
    fun getClosestLocation(location: Location): Triple<String, String, String>? {
        val playerCoords = calculatePlayerCoords(location)

        val (closestCity, closestCountry) = getClosestCity(playerCoords)

        return closestCity?.let {
            Triple( it.latitude, it.longitude, "${it.name}, ${closestCountry?.name ?: "Unknown Country"}")
        }
    }

    private fun calculatePlayerCoords(location: Location): Pair<String, String> {
        val x = location.x.toFloat()
        val z = location.z.toFloat()

        val lon = (x / Config.scale) * Config.tiles
        val lat = -1 * ((z / Config.scale) * Config.tiles)

        return Pair(lat.toString(), lon.toString())
    }

    private fun getClosestCity(playerCoords: Pair<String, String>): Pair<City?, Country?> {
        var closestCity: City? = null
        var closestCountry: Country? = null
        var closestDistance = Double.MAX_VALUE

        for (country in countries) {
            for (city in country.cities) {
                val cityCoords = Pair(city.latitude, city.longitude)
                val distance = calculateDistance(playerCoords, cityCoords)

                if (distance < closestDistance) {
                    closestDistance = distance
                    closestCity = city
                    closestCountry = country
                }
            }
        }

        return Pair(closestCity, closestCountry)
    }
    fun calculateCoordinates(location: Pair<String, String>?): Pair<Float, Float> {
        val x = (location?.second!!.toFloat() * Config.scale / Config.tiles)
        val z = -1 * ((location.first.toFloat() * Config.scale) / Config.tiles)
        return Pair(x.toFloat(), z.toFloat())
    }
    // btw im not a math genius i stole the forumla from here: https://www.movable-type.co.uk/scripts/latlong.html
    private fun calculateDistance(coords1: Pair<String, String>, coords2: Pair<String, String>): Double {
        val lat1 = coords1.first.toDouble()
        val lon1 = coords1.second.toDouble()
        val lat2 = coords2.first.toDouble()
        val lon2 = coords2.second.toDouble()

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // earths radius is approximately 6371 kilometers so uh that math maths!
        val radius = 6371.0

        return radius * c
    }
}
