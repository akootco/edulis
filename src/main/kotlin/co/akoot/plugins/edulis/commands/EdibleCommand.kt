package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.util.builders.FoodBuilder
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.command.CommandSender

class EdibleCommand(plugin: FoxPlugin) : FoxCommand(plugin, "edible") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> =
        mutableListOf()

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val player = playerCheck(sender) ?: return false
        val item = player.inventory.itemInMainHand
        val edibleKey = key("edible")

        if (item.isFood) return false

        if (item.itemMeta.hasPDC(edibleKey)) {
            ItemBuilder.builder(item)
                .removepdc(edibleKey)
                .resetData(DataComponentTypes.CONSUMABLE)
                .resetData(DataComponentTypes.FOOD)
                .build()
            return true
        } else {
            val edibleItem = ItemBuilder.builder(item)
                .pdc(edibleKey)
                .build()

            FoodBuilder.builder(edibleItem)
                .hunger(2, 2.0f)
                .isSnack().build()
            return true
        }
    }
}