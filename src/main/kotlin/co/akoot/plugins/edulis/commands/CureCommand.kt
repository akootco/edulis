package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.endKey
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

            val pdc = sender.persistentDataContainer

            if (pdc.has(endKey)) {
                pdc.remove(endKey)
                return true
            }

            return sendError(sender, "You are not sick.")
        }

        val target = plugin.server.getPlayer(args[0]) ?: run {
            return sendError(sender, "Player not found")
        }

        val targPdc = target.persistentDataContainer
        if (targPdc.has(endKey)) {
            targPdc.remove(endKey)
            return sendMessage(sender, "${target.name} is no longer sick!")
        }

        return sendError(sender, "${target.name} is not sick.")
    }
}