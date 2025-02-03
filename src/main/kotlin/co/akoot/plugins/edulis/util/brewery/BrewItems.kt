package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.edulis.util.CreateItem.getItemPDC
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack

class BrewItems : PluginItem() {
    override fun matches(item: ItemStack): Boolean {
        return getItemPDC(item) == itemId // lol
    }
}