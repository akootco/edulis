package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.edulis.util.CreateItem.foodKey
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack

class BrewItems : PluginItem() {
    override fun matches(item: ItemStack): Boolean {
        return item.itemMeta.getPDC<String>(foodKey) == itemId // lol
    }
}