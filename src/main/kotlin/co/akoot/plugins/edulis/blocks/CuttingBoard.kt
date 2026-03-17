package co.akoot.plugins.edulis.blocks

import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import co.akoot.plugins.plushies.util.spawnItemDisplay
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent.ItemFrameChangeAction
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class CuttingBoard : Listener {
    companion object { val cbkey = key("cutting_board") }

    init { createCuttingBoards() }

    @EventHandler
    fun onPlace(event: HangingPlaceEvent) {
        val item = event.itemStack ?: return
        val loc = event.block.location
        val itemFrame = event.entity

        if (!item.itemMeta.hasPDC(cbkey)) return

        itemFrame.apply {
            setPDC(cbkey, loc)
            isInvisible = true
        }

        spawnItemDisplay(loc, item)
    }

    @EventHandler
    fun onFrameInteract(event: PlayerItemFrameChangeEvent) {
        val itemFrame = event.itemFrame
        val loc = itemFrame.location
        if (!itemFrame.hasPDC(cbkey)) return

        when (event.action) {
            ItemFrameChangeAction.PLACE -> {
                val transformation = Transformation(
                    Vector3f(0f,0f,-0.45f),
                    AxisAngle4f(),
                    Vector3f(.501f,.501f,.501f),
                    AxisAngle4f()
                )
                itemFrame.addPassenger(spawnItemDisplay(loc,
                    event.itemStack,
                    transformation
                ))

            }

            ItemFrameChangeAction.REMOVE -> { itemFrame.passengers.first().remove() }

            ItemFrameChangeAction.ROTATE -> { event.isCancelled = true }
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