package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.edulis.util.loaders.ItemLoader.Companion.foodKey
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class BrewItems : PluginItem() {
    override fun matches(item: ItemStack): Boolean {
        val itemMeta = item.itemMeta ?: return false
        // match pdc
        val itemId = itemMeta.persistentDataContainer.get(foodKey, PersistentDataType.STRING)?: return false
        return itemId == getItemId()
    }
}