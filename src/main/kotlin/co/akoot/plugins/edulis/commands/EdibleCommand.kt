package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.util.builders.FoodBuilder
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

class EdibleCommand(plugin: FoxPlugin) : FoxCommand(plugin, "edible", aliases = arrayOf("drinkable")) {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> =
        mutableListOf()

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val player = playerCheck(sender) ?: return false
        val item = player.inventory.itemInMainHand
        val edibleKey = key("edible")
        val bucketKey = key("bkt")
        if (item.isEmpty || (item.isFood || item.type.isEdible)) return false

        if (item.itemMeta.hasPDC(edibleKey)) {
            val bucketType = item.itemMeta.getPDC<String>(bucketKey)
            val material = bucketType?.let { Material.getMaterial(it) } ?: item.type
            player.inventory.setItemInMainHand(ItemStack(material, item.amount))
            return true
        } else {
            val isBucket = item.type.name.endsWith("_BUCKET")
            val edibleItem = ItemBuilder.builder(if (isBucket) ItemStack(Material.MILK_BUCKET) else item)
                .pdc(edibleKey, true)
                .copyOf(item)
                .apply { if (isBucket) {
                    itemModel(item.type.name.lowercase())
                    pdc(bucketKey, item.type.name)
                } }
                .build()

            player.inventory.setItemInMainHand(FoodBuilder.builder(edibleItem)
                .hunger(2, 2.0f)
                .apply { if (alias == "drinkable") eatSound("entity.generic.drink") }
                .isSnack().build())
            return true
        }
    }
}