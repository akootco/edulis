package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.leafDrops
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.createDisplay
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.removeDisplay
import co.akoot.plugins.edulis.listeners.tasks.CropDisplay
import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.edulis.util.Schematics.paste
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
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

        block.chunk.setPDC(getBlockPDC(block.location, "edulis"), id)
        runLater(1) {block.chunk.removePDC(getBlockPDC(block.location, "alces")) }
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        if (event.isCancelled || Tag.LEAVES.isTagged(event.block.type).not()) return
        leafDrops(event.block)
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
                    val item = inventory.filterNotNull().firstOrNull()?.clone() ?: return
                    val id = item.itemMeta.getPDC<String>(foodKey) ?: return

                    block.chunk.setPDC(getBlockPDC(block.location, "edulis"), id)
                }
            }

            is Player -> {
                if (event.isCancelled) return // this needs to be checked so core protect doesn't break
                val id = block.location.chunk.getPDC<String>(getBlockPDC(block.location, "edulis")) ?: return

                runLater(1) {
                    if (block.type.name.endsWith("CANDLE_CAKE")) return@runLater // why is this even a thing?
                    if (block.type != Material.CAKE) {
                        removeDisplay(block.location, true)
                        return@runLater
                    }
                    (block.blockData as? Cake)?.let { createDisplay(block.location, it.bites, id) }
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
        val key = chunk.getPDC<String>(getBlockPDC(location, "edulis")) ?: return

        if (event.species in listOf(TreeType.TREE, TreeType.BIG_TREE)) {
            event.isCancelled = paste(key, location)
            chunk.removePDC(getBlockPDC(location, "edulis"))
        }
    }

    @EventHandler
    fun dispenserUseItem(event: BlockDispenseEvent) {
        if (event.block.type != Material.DISPENSER || event.item.type != Material.BONE_MEAL) return

        val face = (event.block.blockData as? Directional)?.facing ?: return // where is it looking?
        runLater(1, CropDisplay(event.block.location.add(face.direction).block))
    }
}