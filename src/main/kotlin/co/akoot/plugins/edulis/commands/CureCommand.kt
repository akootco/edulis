package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.edulis.listeners.tasks.endKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CureCommand(plugin: FoxPlugin) : FoxCommand(plugin, "cure") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return getOnlinePlayerSuggestions(exclude = setOf(sender.name))
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {

            if (sender !is Player) {
                return sendError(sender, "Please specify a player")
            }

            if (sender.getPDC<Long>(endKey) != null) {
                sender.removePDC(endKey)
                return true
            }

            return sendError(sender, "You are not sick.")
        }

        val target = plugin.server.getPlayer(args[0]) ?: run {
            return sendError(sender, "Player not found")
        }

        if (target.getPDC<Long>(endKey) != null) {
            target.removePDC(endKey)
            return sendMessage(sender, "${target.name} is no longer sick!")
        }

        return sendError(sender, "${target.name} is not sick.")
    }
}