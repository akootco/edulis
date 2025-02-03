package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.util.CreateItem.getItemPDC
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Fox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.persistence.PersistentDataType

class BlockEvent : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val block = event.blockPlaced
        val id = getItemPDC(event.itemInHand)?: return


        if (block.type == Material.CAKE) {
            createDisplay(block.location, 0, id)
            return
        }

        setPDC(block, id)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        dropItems(event.block)
    }

    @EventHandler
    fun blockBreakBlock(event: BlockBreakEvent) {
        dropItems(event.block)
    }

    @EventHandler
    fun onDestroy(event: BlockDestroyEvent) {
        dropItems(event.block)
    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        for (block in event.blockList()) {
            dropItems(block)
        }
    }

    @EventHandler
    fun onFoxHarvest(event: EntityChangeBlockEvent) {
        val entity = event.entity
        val block = event.block

        if (entity is Fox && block.blockData is Ageable && block.type == Material.SWEET_BERRY_BUSH) {
            dropItems(block)

            // set the berry bush age
            val ageableBlockData = block.blockData as Ageable
            ageableBlockData.age = 1

            block.blockData = ageableBlockData
            event.isCancelled = true
        }
    }

    private fun setPDC(block: Block, id: String) {
        val pdc = block.chunk.persistentDataContainer
        pdc.set(getBlockPDC(block.location), PersistentDataType.STRING, id)
    }

    private fun getBlockPDC(location: Location): NamespacedKey {
        val key = "${location.world.name}.${location.blockX}.${location.blockY}.${location.blockZ}"
        return NamespacedKey("edulis", key)
    }

    // TODO(i haven't tested this yet!)
    private fun dropItems(block: Block) {
        val pdc = block.chunk.persistentDataContainer.get(getBlockPDC(block.location), PersistentDataType.STRING)
            ?: return

        block.drops.apply {
            clear()
            add(resolvedResults[pdc])
        }
    }
}