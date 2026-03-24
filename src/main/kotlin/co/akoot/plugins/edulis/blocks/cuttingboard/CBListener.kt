package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text.Companion.plus
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops
import co.akoot.plugins.edulis.listeners.handlers.ItemDisplays
import co.akoot.plugins.plushies.util.Util
import co.akoot.plugins.plushies.util.isCustomBlock
import co.akoot.plugins.plushies.util.spawnItemDisplay
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.GlowItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class CBListener : Listener {

    init {
        createCuttingBoards()
        createCBRecipes()
    }

    @EventHandler
    fun HangingPlaceEvent.onPlace() {
        val item = itemStack ?: return
        val loc = block.location
        val id = item.getPDC<String>(Edulis.foodKey) ?: return

        if (!item.itemMeta.hasPDC(cbkey)) return
        if (blockFace != BlockFace.UP) { isCancelled = true ; return }

        entity.apply { setPDC(cbkey, loc) ; isInvisible = true }
        block.chunk.setPDC(Util.getBlockPDC(block.location, "edulis"), id)
        spawnItemDisplay(loc, item, Transformation(
            Vector3f(0f,-.13f,0f),
            AxisAngle4f(),
            Vector3f(1.501f, 1.501f, 1.501f),
            AxisAngle4f())
        )
    }

    @EventHandler
    fun PlayerItemFrameChangeEvent.onInteract() {
        val loc = itemFrame.location
        val itemFrame = itemFrame as GlowItemFrame
        if (!itemFrame.hasPDC(cbkey)) return

        when (action) {
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE -> {
                val offhand = player.inventory.itemInOffHand
                if (cuttingBoardRecipes.any { it.tool.test(itemStack)} && offhand.isEmpty.not()) {
                    isCancelled = true
                    itemFrame.addPassenger(spawnItemDisplay(loc, offhand, getDisplayTransform(offhand)))
                    loc.world.playSound(loc, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1f ,1f)
                    itemFrame.setItem(offhand)
                    offhand.amount -= 1
                }
                else itemFrame.addPassenger(spawnItemDisplay(loc, itemStack, getDisplayTransform(itemStack)))
            }

            PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE -> { itemFrame.passengers.firstOrNull()?.remove() }

            PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE -> {
                isCancelled = true
                if (!cutItem(loc, itemStack, player.inventory.itemInMainHand, itemFrame))
                    return player.sendActionBar(itemStack.effectiveName() + " cannot be cut!")
            }
        }
    }

    @EventHandler
    fun HangingBreakEvent.onBreak() {
        if (isCancelled) return
        val loc = entity.location

        if (entity is GlowItemFrame && loc.block.isCustomBlock) {
            loc.world.apply {
                dropItemNaturally(loc, (entity as GlowItemFrame).item)
                playSound(loc, "entity.item_frame.remove_item", .5f, 1.0f)
            }

            entity.remove()
            BlockDrops.dropItems(loc.block)
            ItemDisplays.removeDisplay(loc, true)
        }
    }

    @EventHandler
    fun HangingBreakByEntityEvent.onPlayerBreak() {
        if (entity !is GlowItemFrame && !entity.location.block.isCustomBlock) return
        if (remover is Player && (remover as Player).inventory.itemInMainHand.isTool) { isCancelled = true }
    }
}