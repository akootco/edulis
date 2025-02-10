package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.getBlockPDC
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.isLeaf
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.leafDrops
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.setBlockPDC
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.util.CreateItem.getItemPDC
import co.akoot.plugins.edulis.util.Schematics.paste
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.TreeType
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Fox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.world.StructureGrowEvent
import org.bukkit.persistence.PersistentDataType

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

        if (isLeaf(event.block)) return leafDrops(event.block)
        event.isDropItems = !dropItems(event.block)

    }

    @EventHandler
    fun blockBreakBlock(event: BlockBreakBlockEvent) {
        if (isLeaf(event.block)) return leafDrops(event.block)
        if (dropItems(event.block)) event.drops.clear()
    }

    @EventHandler
    fun onDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return

        if (isLeaf(event.block)) return leafDrops(event.block)
        event.setWillDrop(!dropItems(event.block))

    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        if (event.isCancelled) return
        for (block in event.blockList()) {
            if (isLeaf(block)) leafDrops(block)
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

    @EventHandler
    fun onLeafDecay(event: LeavesDecayEvent) {
        leafDrops(event.block)
    }

    @EventHandler
    fun treeGrow(event: StructureGrowEvent) {
        val location = event.location
        val pdc = location.chunk.persistentDataContainer
        val key = getBlockPDC(location)
        val value = pdc.get(key, PersistentDataType.STRING) ?: return

        if (event.species in listOf(TreeType.TREE, TreeType.BIG_TREE)) {
            event.isCancelled = paste(value, location)
            pdc.remove(key)
        }
    }
}