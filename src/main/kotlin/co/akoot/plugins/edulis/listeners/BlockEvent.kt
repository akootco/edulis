package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.setBlockPDC
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.util.CreateItem.getItemPDC
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Fox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent

class BlockEvent : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        val block = event.blockPlaced
        val id = getItemPDC(event.itemInHand) ?: return

        if (block.type == Material.CAKE) {
            createDisplay(block.location, 0, id)
        }
        setBlockPDC(block, id)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return
        event.isDropItems = !dropItems(event.block)

    }

    @EventHandler
    fun blockBreakBlock(event: BlockBreakBlockEvent) {
        if (dropItems(event.block)) event.drops.clear()
    }

    @EventHandler
    fun onDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return
        event.setWillDrop(!dropItems(event.block))

    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        if (event.isCancelled) return
        for (block in event.blockList()) {
            if (dropItems(block)) block.type = Material.AIR
        }
    }

    @EventHandler
    fun onFoxHarvest(event: EntityChangeBlockEvent) {
        if (event.isCancelled) return
        val block = event.block

        if (event.entity is Fox && block.blockData is Ageable && block.type == Material.SWEET_BERRY_BUSH) {
            event.isCancelled = dropItems(block)

            // set the berry bush age
            val ageableBlockData = block.blockData as Ageable
            ageableBlockData.age = 1

            block.blockData = ageableBlockData
        }
    }
}