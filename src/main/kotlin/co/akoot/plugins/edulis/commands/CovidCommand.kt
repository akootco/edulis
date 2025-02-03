package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.giveCovid
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CovidCommand(plugin: FoxPlugin) : FoxCommand(plugin, "covid") {

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return getOnlinePlayerSuggestions(exclude = setOf(sender.name))
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender is Player) {
                giveCovid(sender, plugin)
                return true
            } else {
                sender.sendMessage("Please specify a player")
                return false
            }
        }

        val target = Bukkit.getPlayer(args[0]) ?: run {
            sender.sendMessage("Player not found!")
            return false
        }

        giveCovid(target, plugin)
        sender.sendMessage("${target.name} has been infected!")
        return true
    }
}