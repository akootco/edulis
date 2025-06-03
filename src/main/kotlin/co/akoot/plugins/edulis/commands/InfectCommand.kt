package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.listeners.tasks.giveCovid
import org.bukkit.command.CommandSender

class InfectCommand(plugin: FoxPlugin) : FoxCommand(plugin, "infect") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> =
        if (args.size == 1) getOnlinePlayerSuggestions(exclude = setOf(sender.name)) else mutableListOf()

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val target = if (args.isEmpty()) {
            playerCheck(sender, Text("Please specify a player"))
        } else {
            plugin.server.getPlayer(args[0]) ?: return sendError(sender, "Player not found")
        } ?: return false

        giveCovid(target, plugin)
        return sendMessage(sender, "${target.name} has been infected!")
    }
}