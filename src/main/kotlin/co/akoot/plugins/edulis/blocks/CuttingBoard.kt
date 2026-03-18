package co.akoot.plugins.edulis.blocks

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays.removeDisplay
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import co.akoot.plugins.plushies.util.isCustomBlock
import co.akoot.plugins.plushies.util.spawnItemDisplay
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class CuttingBoard : Listener {
    companion object { val cbkey = key("cutting_board") }

    init { createCuttingBoards() }

    @EventHandler
    fun HangingPlaceEvent.onPlace() {
        val item = itemStack ?: return
        val loc = block.location
        val id = item.getPDC<String>(foodKey) ?: return

        if (!item.itemMeta.hasPDC(cbkey)) return
        if (blockFace != BlockFace.UP) { isCancelled = true ; return }

        entity.apply { setPDC(cbkey, loc) ; isInvisible = true }
        block.chunk.setPDC(getBlockPDC(block.location, "edulis"), id)
        spawnItemDisplay(loc, item)
    }

    @EventHandler
    fun PlayerItemFrameChangeEvent.onInteract() {
        val loc = itemFrame.location
        if (!itemFrame.hasPDC(cbkey)) return

        when (action) {
            ItemFrameChangeAction.PLACE -> {
                val item = Transformation(
                    Vector3f(0f,0f,-0.45f),
                    AxisAngle4f(),
                    Vector3f(0.501f,0.501f,0.501f),
                    AxisAngle4f()
                )
                val tool = Transformation(
                    Vector3f(-.2f, 0f, -0.15f),
                    AxisAngle4f(Math.toRadians(-90.0).toFloat(), 1f, 0f, -.2f),
                    Vector3f(.8f, .8f, .8f),
                    AxisAngle4f()
                )
                val display = if (Tag.ITEMS_BREAKS_DECORATED_POTS.isTagged(itemStack.type)) tool else item
                itemFrame.addPassenger(spawnItemDisplay(loc, itemStack, display))
            }

            ItemFrameChangeAction.REMOVE -> { itemFrame.passengers.firstOrNull()?.remove() }

            ItemFrameChangeAction.ROTATE -> {isCancelled = true }
        }
    }

    @EventHandler
    fun HangingBreakEvent.onBreak() {
        val loc = entity.location

        if (entity is GlowItemFrame && loc.block.isCustomBlock) {
            loc.world.apply {
                dropItemNaturally(loc, (entity as GlowItemFrame).item)
                playSound(loc, "entity.item_frame.remove_item", .5f, 1.0f)
            }

            entity.remove()
            BlockDrops.dropItems(loc.block)
            removeDisplay(loc, true)
        }
    }

    fun createCuttingBoards() {
        Tag.PLANKS.values.forEach { material ->
            val woodType = material.name.lowercase().removeSuffix("_planks")
            val id = woodType + "_cutting_board"
            val displayName = woodType.split("_").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

            customItems[id] = ItemBuilder.builder(Material.GLOW_ITEM_FRAME)
                .itemName(Text("$displayName Cutting Board").component)
                .pdc(cbkey, woodType)
                .pdc(foodKey, id)
                .customModelData(id)
                .build()
        }
    }
}