package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import org.bukkit.command.CommandSender

class HungryCommand(plugin: FoxPlugin) : FoxCommand(plugin, "hungry") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> =
        mutableListOf()

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        playerCheck(sender)?.apply {
            foodLevel = 2
            saturation = 2.0f
        }

        return true
    }
}