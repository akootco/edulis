package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.getBlockPDC
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.isLeaf
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.leafDrops
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.removeDisplay
import co.akoot.plugins.edulis.listeners.tasks.CropDisplay
import co.akoot.plugins.edulis.util.Materials.matches

import co.akoot.plugins.edulis.util.Schematics.paste
import com.destroystokyo.paper.event.block.BlockDestroyEvent
import io.papermc.paper.event.block.BlockBreakBlockEvent
import org.bukkit.ExplosionResult
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.TreeType
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Cake
import org.bukkit.entity.Fox
import org.bukkit.entity.Player
import org.bukkit.entity.Rabbit
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
        if (event.isCancelled) return // this needs to be checked so core protect doesn't break
        val block = event.blockPlaced
        val id = event.itemInHand.itemMeta.getPDC<String>(foodKey) ?: return

        if (block.type.matches(Material.CAKE)) {
            createDisplay(block.location, 0, id)
        }

        block.chunk.setPDC(getBlockPDC(block.location), id)
        runLater(1) {block.chunk.removePDC(getBlockPDC(block.location, "alces")) }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return // this needs to be checked so core protect doesn't break

        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        event.isDropItems = !dropItems(event.block, removePDC = true)

    }

    @EventHandler
    fun blockBreakBlock(event: BlockBreakBlockEvent) {
        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        if (dropItems(event.block, removePDC = true)) event.drops.clear()
    }

    @EventHandler
    fun onDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return

        if (isLeaf(event.block)) return leafDrops(event.block)

        removeDisplay(event.block.location)
        event.setWillDrop(!dropItems(event.block, removePDC = true))

    }

    @EventHandler
    fun onExplosion(event: EntityExplodeEvent) {
        if (event.isCancelled ||
            event.explosionResult != ExplosionResult.DESTROY ) return

        for (block in event.blockList()) {
            if (isLeaf(block)) leafDrops(block)

            removeDisplay(block.location)
            if (dropItems(block, removePDC = true)) block.type = Material.AIR
        }
    }

    @EventHandler
    fun blockChange(event: EntityChangeBlockEvent) {
        val block = event.block

        when (event.entity) {
            is Fox -> {
                if (dropItems(block, 3, setAge = true)) {
                    event.isCancelled = true
                    block.location.world.playSound(block.location, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1f, 1f)
                    removeDisplay(event.block.location)
                }
            }

            is Villager -> {
                if (block.type.matches(Material.AIR) && block.location.subtract(0.0, 1.0, 0.0).block.type.matches(Material.FARMLAND)) {
                    val inventory = (event.entity as Villager).inventory.contents.clone()
                    val item = inventory.filterNotNull().first().clone()
                    val id = item.itemMeta.getPDC<String>(foodKey) ?: return

                    block.chunk.setPDC(getBlockPDC(block.location), id)
                }
            }

            is Player -> {
                if (event.isCancelled) return // this needs to be checked so core protect doesn't break
                if (block.type.matches(Material.CAKE)) {
                    val cake = block.blockData as Cake
                    val id = block.location.chunk.getPDC<String>(getBlockPDC(block.location)) ?: return

                    createDisplay(block.location, cake.bites.plus(1), id)
                }
            }

            is Rabbit -> {
                runLater(1, CropDisplay(event.block))
            }

            else -> return
        }
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
        if (event.isCancelled) return // this needs to be checked so core protect doesn't break
        val blockData = event.harvestedBlock.blockData
        if (blockData is Ageable) {
            if (dropItems(event.harvestedBlock, blockData.age)) {
                removeDisplay(event.harvestedBlock.location)
                event.itemsHarvested.clear()
            }
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