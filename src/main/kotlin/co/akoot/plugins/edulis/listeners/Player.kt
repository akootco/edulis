package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.util.loaders.ItemLoader.Companion.foodKey
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.giveCovid
import co.akoot.plugins.edulis.listeners.tasks.Covid.Companion.resumeCovid
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class Player(private val plugin: FoxPlugin) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        resumeCovid(event.player, plugin)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        resumeCovid(event.player, plugin)
    }

    @EventHandler
    fun sliceEvent(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        val player = event.player
        val itemMeta = player.inventory.itemInMainHand.itemMeta
        val pdc = itemMeta.persistentDataContainer

        if (block?.type == Material.STONECUTTER && event.action == Action.RIGHT_CLICK_BLOCK) {
            val cake = pdc.get(foodKey, PersistentDataType.STRING) ?: return
            giveSlice(event, cake, block, event.player)
        }
    }

    @EventHandler
    fun itemConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val pdc = event.item.itemMeta?.persistentDataContainer ?: return
        // is it a flugin item?
        val itemId = pdc.get(foodKey, PersistentDataType.STRING) ?: return

        // more importantly, is it a bat wing
        if (itemId.contains("bat_wing")) {
            giveCovid(player, plugin)
        }
    }

    private fun giveSlice(event: PlayerInteractEvent, cake: String, cutter: Block, player: Player) {
        val cakeSlice = resolvedResults["${cake}_slice"] ?: return

        // cakes should give 8 and pies should give 4
        cakeSlice.amount = if (cake.endsWith("cake")) 8 else 4

        // stop stonecutter gui from opening
        event.setUseInteractedBlock(Event.Result.DENY)

        // remove 1 item, if only 1 item, set air
        player.inventory.itemInMainHand.takeIf { it.amount > 1 }?.apply { amount-- } ?: ItemStack(Material.AIR)

        cutter.world.playSound(cutter.location, Sound.BLOCK_HONEY_BLOCK_HIT, 0.2f, 2.0f)
        cutter.world.dropItemNaturally(cutter.location.add(0.5, 1.0, 0.5), cakeSlice)
    }
}