package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.listeners.tasks.CropDisplay
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.edulis.util.Util.updateItem
import co.akoot.plugins.edulis.listeners.tasks.giveCovid
import co.akoot.plugins.edulis.listeners.tasks.isInfected
import co.akoot.plugins.edulis.listeners.tasks.pauseCovid
import co.akoot.plugins.edulis.listeners.tasks.resumeCovid
import co.akoot.plugins.edulis.util.Util.unlockRecipes
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerRespawnEvent

class PlayerEvent(private val plugin: FoxPlugin) : Listener {

    @EventHandler
    fun PlayerItemHeldEvent.onHold() {
        runLater(1) { updateItem(player.inventory) }
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        pauseCovid(player)
    }

    @EventHandler
    fun PlayerQuitEvent.onLeave() {
        pauseCovid(player)
    }

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        unlockRecipes(player)
        resumeCovid(player, plugin)
    }

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        resumeCovid(player, plugin)
    }

    @EventHandler
    fun PlayerInteractEvent.playerInteract() {
        val block = clickedBlock ?: return
        val item = player.inventory.itemInMainHand

        if (action == Action.RIGHT_CLICK_BLOCK) {

            when (block.type) {
                Material.STONECUTTER -> {
                    val type = item.type
                    val pdc = item.itemMeta?.getPDC<String>(foodKey)

                    val id = when {
                        pdc != null -> pdc
                        type == Material.CAKE || type == Material.PUMPKIN_PIE -> type.name.lowercase()
                        else -> return
                    }
                    giveSlice(this, id, block, player)
                }

                Material.POTTED_FERN -> {
                    val basil = customItems["basil"] ?: return

                    if (item.type.matches(Material.SHEARS)) {
                        block.world.apply {
                            dropItemNaturally(block.location.add(0.5, 1.0, 0.5), basil)
                            playSound(block.location, Sound.ENTITY_BOGGED_SHEAR, 1.0f, 2.0f)
                        }
                    } else {
                        player.give(basil)
                        block.type = Material.FLOWER_POT
                    }
                    isCancelled = true
                }

                Material.FLOWER_POT -> {
                    if (item.isSimilar(customItems["basil"] ?: return)) {
                        block.type = Material.POTTED_FERN
                        item.amount -= 1
                    }
                }

                in Tag.CROPS.values.plus(Material.SWEET_BERRY_BUSH) -> {
                    if (block.chunk.getPDC<String>(getBlockPDC(block.location, "edulis")) == null) return

                    val crop = block.state.blockData as? Ageable ?: return
                    if (crop.age == crop.maximumAge) {
                       isCancelled = true
                        dropItems(block, crop.age, setAge = true)
                    }

                    runLater(1, CropDisplay(block))
                }

                in Tag.DIRT.values -> {
                    if (item.isSimilar(customItems["tomato"] ?: return)) {
                        if (blockFace == BlockFace.UP) { // make sure player is clicking top of block
                            // make sure the space above is empty
                            val aboveBlock = block.getRelative(BlockFace.UP).takeIf { it.type.matches(Material.AIR) } ?: return

                            isCancelled = true

                            aboveBlock.apply { // plant tomato
                                type = Material.SWEET_BERRY_BUSH
                                chunk.setPDC(getBlockPDC(aboveBlock.location, "edulis"), item.itemMeta.getPDC<String>(foodKey))
                                location.world.playSound(location, Sound.BLOCK_SWEET_BERRY_BUSH_PLACE, 1f, 1f)
                            }
                            item.amount -= 1
                        }
                    }
                }

                else -> return
            }
        }
    }

    @EventHandler
    fun PlayerItemConsumeEvent.itemConsume() {
        if (player.isInfected && item.type == Material.MILK_BUCKET) {
            Text(player) { Kolor.ERROR("Good trick, but milk won't save you!") }
            return
        }

        val id = item.itemMeta.getPDC<String>(foodKey) ?: return

        when {
            "bat_wing" in id -> giveCovid(player, plugin)
            "cake_slice" in id -> player.incrementStatistic(Statistic.CAKE_SLICES_EATEN)
        }
    }

    private fun giveSlice(event: PlayerInteractEvent, cake: String, cutter: Block, player: Player) {
        val cakeSlice = customItems["${cake}_slice"] ?: return

        // cakes should give 8 and pies should give 4
        cakeSlice.amount = if (cake.endsWith("cake")) 8 else 4

        // stop stonecutter gui from opening
        event.setUseInteractedBlock(Event.Result.DENY)

        // remove 1 item, if only 1 item, set air
        player.inventory.itemInMainHand.amount -= 1

        cutter.world.playSound(cutter.location, Sound.BLOCK_HONEY_BLOCK_HIT, 0.2f, 2.0f)
        cutter.world.dropItemNaturally(cutter.location.add(0.5, 1.0, 0.5), cakeSlice)
    }
}