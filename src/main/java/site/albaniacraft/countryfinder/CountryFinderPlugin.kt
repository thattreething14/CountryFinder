package site.albaniacraft.countryfinder

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import site.albaniacraft.countryfinder.commands.GetLocationCommand
import site.albaniacraft.countryfinder.commands.WhereAmICommand
import site.albaniacraft.countryfinder.listeners.BorderListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CountryFinderPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: Plugin
            private set
    }

    override fun onEnable() {
        instance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        Config.loadConfig()
        val countriesCitiesFile = File(dataFolder, "countries+cities.json")
        if (!countriesCitiesFile.exists()) { try { val inputStream = getResource("countries+cities.json")
            if (inputStream != null) { val outputStream = FileOutputStream(countriesCitiesFile)
                inputStream.use { input -> outputStream.use { output -> input.copyTo(output) } }
                logger.info("Copied countries+cities.json from resources to the data folder.") } else {
                logger.warning("Failed to copy countries+cities.json from resources. File not found in resources.") } } catch (e: IOException) { e.printStackTrace()
            logger.warning("Failed to copy countries+cities.json from resources.") } }
        getCommand("getlocation")?.setExecutor(GetLocationCommand())
        getCommand("getlocation")?.tabCompleter = GetLocationCommand()
        getCommand("whereami")?.setExecutor(WhereAmICommand())
        server.pluginManager.registerEvents(BorderListener(), this)
        logger.info("CountryFinder has been enabled!")
    }
    override fun onDisable() {
    }
}
