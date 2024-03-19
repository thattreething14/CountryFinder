package site.albaniacraft.countryfinder

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

object Config {
    private val plugin: Plugin = CountryFinderPlugin.instance
    var scale: Double = 3072.0
    var tiles: Double = 3.0
    init {
        loadConfig()
    }

    fun loadConfig() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            try {
                plugin.saveResource("config.yml", false)
                plugin.logger.info("Created config.yml.")
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                plugin.logger.warning("Failed to create config.yml.")
            }
        }

        // Load values from config.yml
        val config = YamlConfiguration.loadConfiguration(configFile)
        scale = config.getDouble("scale", 3072.0)
        tiles = config.getDouble("tiles", 3.0)
    }

}
