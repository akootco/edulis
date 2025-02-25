package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.getBlockPDC
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.isLeaf
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.leafDrops
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.removeDisplay
import co.akoot.plugins.edulis.listeners.tasks.CropDisplay
import co.akoot.plugins.edulis.util.CreateItem.foodKey
import co.akoot.plugins.edulis.util.Schematics.paste
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.TreeType
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Cake
import org.bukkit.entity.Fox
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerHarvestBlockEvent
import org.bukkit.event.world.StructureGrowEvent

class BlockEvent : Listener {

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        val block = event.blockPlaced

        val id = event.itemInHand.itemMeta.getPDC<String>(foodKey) ?: return

        if (block.type == Material.CAKE) {
            createDisplay(block.location, 0, id)
        }

        block.chunk.setPDC(getBlockPDC(block.location), id)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return

        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        event.isDropItems = !dropItems(event.block)

    }

    @EventHandler
    fun blockBreakBlock(event: BlockBreakBlockEvent) {
        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        if (dropItems(event.block)) event.drops.clear()
    }

    @EventHandler
    fun onDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return

        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        event.setWillDrop(!dropItems(event.block))

    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        if (event.isCancelled) return
        for (block in event.blockList()) {
            if (isLeaf(block)) leafDrops(block)

            removeDisplay(block.location)
            if (dropItems(block)) block.type = Material.AIR
        }
    }

    @EventHandler
    fun entityHarvest(event: EntityChangeBlockEvent) {
        if (event.isCancelled) return
        val block = event.block

        when (event.entity) {
            is Fox -> {
                if (dropItems(block, 3, false)) {
                    block.location.world.playSound(block.location, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1f, 1f)
                    val ageableBlockData = block.blockData as Ageable
                    ageableBlockData.age = 1
                    block.blockData = ageableBlockData
                }
            }

            is Villager -> {
                if (dropItems(block, 3, false)) {
                    block.location.world.playSound(block.location, Sound.BLOCK_CROP_BREAK, 1f, 1f)
                }
            }

            is Player -> {
                if (block.type == Material.CAKE) {
                    val cake = block.blockData as Cake
                    val id = block.location.chunk.getPDC<String>(getBlockPDC(block.location))?: return

                    createDisplay(block.location, cake.bites.plus(1), id)
                    return
                }
            }

            else -> return
        }

        removeDisplay(event.block.location)
        event.isCancelled = true
    }

    @EventHandler
    fun onLeafDecay(event: LeavesDecayEvent) {
        leafDrops(event.block)
    }

    @EventHandler
    fun cropGrow(event: BlockGrowEvent) {
        runLater(1, CropDisplay(event.block))
    }

    @EventHandler
    fun onPlayerHarvest(event: PlayerHarvestBlockEvent) {
        val ageableBlockData = event.harvestedBlock.blockData as Ageable
        if (dropItems(event.harvestedBlock, ageableBlockData.age, false)) {

            removeDisplay(event.harvestedBlock.location)
            event.itemsHarvested.clear()
        }
    }

    @EventHandler
    fun treeGrow(event: StructureGrowEvent) {
        val location = event.location
        val chunk = location.chunk
        val key = chunk.getPDC<String>(getBlockPDC(location)) ?: return

        if (event.species in listOf(TreeType.TREE, TreeType.BIG_TREE)) {
            event.isCancelled = paste(key, location)
            chunk.removePDC(getBlockPDC(location))
        }
    }

    @EventHandler
    fun dispenserUseItem(event: BlockDispenseEvent) {
        if (event.block.type != Material.DISPENSER || event.item.type != Material.BONE_MEAL) return

        val face = (event.block.blockData as? Directional)?.facing ?: return // where is it looking?
        runLater(1, CropDisplay(event.block.location.add(face.direction).block))
    }
}