package site.albaniacraft.countryfinder.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import site.albaniacraft.countryfinder.LocationManager
import site.albaniacraft.countryfinder.Messages

class WhereAmICommand : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
            if (sender !is Player) {
                Messages.error(sender, "Only players can use this command.")
                return false
            }
            if (!sender.hasPermission("countryfinder.whereami")) {
                Messages.error(sender, "You don't have permission to use this command!")
                return true
            }

            val playerLocation = sender.location
            val closestLocation = LocationManager.getClosestLocation(playerLocation)

            if (closestLocation != null) {
                Messages.print(sender, "${ChatColor.GOLD}==== ${ChatColor.YELLOW}Closest Town ${ChatColor.GOLD}====")
                Messages.print(sender, "${ChatColor.YELLOW}Latitude: ${ChatColor.WHITE}${closestLocation.first}", false)
                Messages.print(sender, "${ChatColor.YELLOW}Longitude: ${ChatColor.WHITE}${closestLocation.second}", false)
                Messages.print(sender, "${ChatColor.YELLOW}Town: ${ChatColor.WHITE}${closestLocation.third}", false)
                val googleMapsLink = "https://www.google.com/maps?q=${closestLocation.first},${closestLocation.second}"
                val clickableLink = TextComponent("${ChatColor.YELLOW}Google Maps:${ChatColor.WHITE} $googleMapsLink")
                clickableLink.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, googleMapsLink)
                sender.spigot().sendMessage(clickableLink)
            } else {
                Messages.error(sender, "Could not determine the closest town.")
            }

            return true
    }
}
