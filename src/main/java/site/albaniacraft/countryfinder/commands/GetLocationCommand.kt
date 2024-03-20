package site.albaniacraft.countryfinder.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import site.albaniacraft.countryfinder.Config
import site.albaniacraft.countryfinder.LocationManager
import site.albaniacraft.countryfinder.Messages

class GetLocationCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (command.name.equals("getlocation", ignoreCase = true)) {
            if(sender !is Player) {
                Messages.error(sender, "Only players can use this command.")
                return false
            }
            if(!sender.hasPermission("countryfinder.getlocation")) {
                Messages.error(sender, "You don't have permission to use this command!")
                return false
            }
            if (LocationManager.cooldowns.containsKey(sender.name) && LocationManager.cooldowns[sender.name]!! > System.currentTimeMillis()) {
                val remainingTime = (LocationManager.cooldowns[sender.name]!! - System.currentTimeMillis()) / 1000
                Messages.error(sender, "You must wait $remainingTime seconds before using this command again.")
                return false
            }
            if (args.isNotEmpty()) {
                val input = args.joinToString(" ")
                val parts = input.split(",").map { it.trim() }

                if (parts.isNotEmpty()) {
                    val countryName = parts[0]
                    val cityName = if (parts.size > 1) parts[1] else ""
                    val location = LocationManager.getLocation(countryName, cityName)

                    if (location != null) {
                        val coordinates = LocationManager.calculateCoordinates(location)
                        Messages.print(sender, "${ChatColor.GOLD}==== ${ChatColor.YELLOW}Location ${ChatColor.GOLD}====")
                        Messages.print(sender, "${ChatColor.YELLOW}Country: ${ChatColor.WHITE}$countryName", false)
                        Messages.print(sender, "${ChatColor.YELLOW}City: ${ChatColor.WHITE}$cityName", false)
                        Messages.print(sender, "${ChatColor.YELLOW}Latitude: ${ChatColor.WHITE}${location.first}", false)
                        Messages.print(sender, "${ChatColor.YELLOW}Longitude: ${ChatColor.WHITE}${location.second}", false)
                        val googleMapsLink = "https://www.google.com/maps?q=${location.first},${location.second}"
                        val clickableLink = TextComponent("${ChatColor.YELLOW}Google Maps:${ChatColor.WHITE} $googleMapsLink")
                        clickableLink.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, googleMapsLink)
                        sender.spigot().sendMessage(clickableLink)
                        val teleportCommand = "/tp ${coordinates.first} ~ ${coordinates.second}"
                        val teleportLink = TextComponent("${ChatColor.YELLOW}Coordinates:${ChatColor.WHITE} x=${coordinates.first}, z=${coordinates.second}")
                        teleportLink.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand)
                        sender.spigot().sendMessage(teleportLink)
                    } else {
                        Messages.error(sender, "City not found in the specified country.")
                    }
                    LocationManager.cooldowns[sender.name] = System.currentTimeMillis() + Config.cooldown * 1000
                } else {
                    Messages.error(sender, "Usage: /getlocation <country>, <city>")
                }
            } else {
                Messages.error(sender, "Usage: /getlocation <country>, <city>")
            }
            return true
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (args.isNotEmpty()) {
            if (LocationManager.cooldowns.containsKey(sender.name) && LocationManager.cooldowns[sender.name]!! > System.currentTimeMillis()) {
                return emptyList()
            }
            val input = args.joinToString(" ")
            val parts = input.split(",").map { it.trim() }
            when {
                parts.size == 1 -> {
                    return LocationManager.countries!!.map { it.name + "," }
                        .filter { it.startsWith(parts[0], ignoreCase = true) }
                }
                parts.size >= 2 -> {
                    val countryName = parts[0]
                    val country = LocationManager.countries!!.find { it.name.equals(countryName, ignoreCase = true) }
                    return country?.cities?.map { it.name }?.filter {
                        it.startsWith(parts[1], ignoreCase = true)
                    } ?: emptyList()
                }
            }
        }
        return emptyList()
    }
}
