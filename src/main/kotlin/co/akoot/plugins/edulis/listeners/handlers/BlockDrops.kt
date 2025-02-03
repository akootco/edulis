package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType

object BlockDrops {
    fun setBlockPDC(block: Block, id: String) {
        val pdc = block.chunk.persistentDataContainer
        pdc.set(getBlockPDC(block.location), PersistentDataType.STRING, id)
    }

    private fun getBlockPDC(location: Location): NamespacedKey {
        val key = "${location.world.name}.${location.blockX}.${location.blockY}.${location.blockZ}"
        return NamespacedKey("edulis", key)
    }

    fun dropItems(block: Block): Boolean {
        val pdc = block.chunk.persistentDataContainer
        val id = pdc.get(getBlockPDC(block.location), PersistentDataType.STRING)
            ?: return false

        val loc = block.location

        resolvedResults[id]?.let { loc.world.dropItemNaturally(loc.add(0.5, 0.5, 0.5), it) }
        pdc.remove(getBlockPDC(loc))
        return true
    }
}