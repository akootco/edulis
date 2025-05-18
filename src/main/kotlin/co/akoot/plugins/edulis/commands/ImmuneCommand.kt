package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.edulis.listeners.tasks.immuneKey
import co.akoot.plugins.edulis.listeners.tasks.isImmune
import org.bukkit.command.CommandSender

class ImmuneCommand(plugin: FoxPlugin) : FoxCommand(plugin, "immune") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false

        // i DONT even want to add immunity but i know people will cry!
        if (p.isImmune) p.removePDC(immuneKey) else p.setPDC<Byte>(immuneKey, 1)
        return sendMessage(p, "You are ${if (p.isImmune) "no longer" else "now"} immune to covid!")
    }
}