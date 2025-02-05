package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.giveCovid
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
                return sendError(sender, "Please specify a player")
            }
        }

        val target = plugin.server.getPlayer(args[0]) ?: run {
            return sendError(sender, "Player not found")
        }

        giveCovid(target, plugin)
        return sendMessage(sender,"${target.name} has been infected!")
    }
}