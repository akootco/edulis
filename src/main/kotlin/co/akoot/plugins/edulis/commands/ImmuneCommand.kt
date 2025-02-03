package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.immuneKey
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataType

class ImmuneCommand(plugin: FoxPlugin) : FoxCommand(plugin, "immune") {

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false
        val pdc = p.persistentDataContainer
        val isImmune = pdc.has(immuneKey)

        // i DONT even want to add immunity but i know people will cry!
        p.sendMessage("You are ${if (isImmune) "no longer" else "now"} immune to covid!")
        if (isImmune) pdc.remove(immuneKey) else pdc.set(immuneKey, PersistentDataType.BOOLEAN, true)

        return true
    }
}