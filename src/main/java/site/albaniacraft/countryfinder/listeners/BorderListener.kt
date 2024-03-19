

package site.albaniacraft.countryfinder.listeners

import com.google.gson.Gson
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.Plugin
import site.albaniacraft.countryfinder.Config
import site.albaniacraft.countryfinder.CountryFinderPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BorderListener : Listener {
    // very very lazy use of an init it would've looked much better if I put it in main class, but you know don't care!
    private val countryBorders: Map<String, List<List<List<List<Double>>>>>
    private val plugin: Plugin = CountryFinderPlugin.instance
    init {
        val gson = Gson()
        val dataFolder = plugin.dataFolder
        val geoJsonFile = File(dataFolder, "countries.geojson")
        if (!geoJsonFile.exists()) {
            val resourceStream = plugin.getResource("countries.geojson")
            resourceStream?.use { inputStream ->
                Files.copy(inputStream, geoJsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
        val geoJsonString = geoJsonFile.readText()
        val geoJsonObject = gson.fromJson(geoJsonString, GeoJsonObject::class.java)
        countryBorders = geoJsonObject.features
            .associate { it.properties.ADMIN to it.geometry.coordinates }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val currentCoords = calculatePlayerCoords(event.to)

        var enteredCountry: String? = null
        var leftCountry: String? = null

        for ((country, borders) in countryBorders) {
            val isInCountry = isInsideBorders(currentCoords, borders)

            if (isInCountry && !isInCountry(player, country)) {
                enteredCountry = country
            } else if (!isInCountry && isInCountry(player, country)) {
                leftCountry = country
            }
        }

        if (enteredCountry != null) {
            val enterTitle = "${ChatColor.YELLOW}Entering $enteredCountry"
            val enterSubtitle = if (leftCountry != null && leftCountry != enteredCountry) "${ChatColor.YELLOW}Leaving $leftCountry" else ""
            player.sendTitle(enterTitle, enterSubtitle, 10, 70, 20)
        } else if (leftCountry != null) {
            val leaveTitle = "${ChatColor.YELLOW}Leaving $leftCountry"
            player.sendTitle(leaveTitle, "", 10, 70, 20)
        }
    }
    private fun isInCountry(player: Player, country: String): Boolean {
        val playerLocation = player.location
        val playerCoords = calculatePlayerCoords(playerLocation)

        val countryBorders = countryBorders[country]
        if (countryBorders != null) {
            return isInsideBorders(playerCoords, countryBorders)
        }
        return false
    }
    /**
     * Checks if a given point is inside the borders defined by a list of polygons.
     *
     * @param playerCoords The coordinates of the player as a Pair (latitude, longitude).
     * @param borders List of borders represented as a list of polygons, each containing a list of vertices.
     * @return True if the point is inside any of the polygons, false otherwise.
     */
    private fun isInsideBorders(playerCoords: Pair<Double, Double>, borders: List<List<List<List<Double>>>>): Boolean {
        for (border in borders) {
            for (polygon in border) {
                if (isPointInsidePolygon(playerCoords, polygon)) {
                    return true
                }
            }
        }
        return false
    }
    /**
     * Checks if a given point is inside a polygon using the ray-casting algorithm.
     *
     * The ray-casting algorithm works by casting a ray from the given point towards the right and counting
     * how many times it intersects with the edges of the polygon. If the number of intersections is odd,
     * the point is considered inside the polygon otherwise, it is considered outside. A bit of a
     * coding lesson for you casual :).
     *
     * @param point The point coordinates as a Pair (latitude, longitude).
     * @param polygon List of vertices representing the polygon.
     * @return True if the point is inside the polygon, false otherwise.
     */
    private fun isPointInsidePolygon(point: Pair<Double, Double>, polygon: List<List<Double>>): Boolean {
        val x = point.first
        val z = point.second
        var isInside = false

        for (i in 0 until polygon.size - 1) {
            val vertex1 = polygon[i]
            val vertex2 = polygon[i + 1]

            val x1 = vertex1[1]
            val z1 = vertex1[0]
            val x2 = vertex2[1]
            val z2 = vertex2[0]

            // Check if the ray cast from the point intersects with the polygon edges
            // The algorithm counts the number of times the ray intersects with the edges
            // If the count is odd, the point is inside the polygon
            // This took me an unbelievable amount of time to code lol
            val intersect =
                ((z1 > z) != (z2 > z)) && (x < (x2 - x1) * (z - z1) / (z2 - z1) + x1)
            if (intersect) isInside = !isInside
        }

        return isInside
    }

    private fun calculatePlayerCoords(location: Location): Pair<Double, Double> {
        val x = location.x
        val z = location.z

        val lon = (x / Config.scale) * Config.tiles
        val lat = -1 * ((z / Config.scale) * Config.tiles)

        return Pair(lat, lon)
    }
    data class GeoJsonObject(val features: List<Feature>)
    data class Feature(val properties: Properties, val geometry: Geometry)
    data class Properties(val ADMIN: String)
    data class Geometry(val coordinates: List<List<List<List<Double>>>>)
}
