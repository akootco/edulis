package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.util.loaders.ItemLoader.Companion.foodKey
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType

class Block : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val item = event.itemInHand

        if (block.type == Material.CAKE) {
            val meta = item.itemMeta
            val value = meta.persistentDataContainer.get(foodKey, PersistentDataType.STRING)?: return
            createDisplay(block.location, 0, value)
        }
    }
}