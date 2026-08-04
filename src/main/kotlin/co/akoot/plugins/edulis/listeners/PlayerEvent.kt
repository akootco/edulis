package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.asString
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.tasks.*
import co.akoot.plugins.edulis.util.Util.foodid
import co.akoot.plugins.plushies.listeners.tasks.Throwable.Companion.axeKey
import co.akoot.plugins.plushies.util.Items
import co.akoot.plugins.plushies.util.Items.itemKey
import co.akoot.plugins.plushies.util.Recipes.unlockRecipes
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import kotlin.random.Random

class PlayerEvent(private val plugin: FoxPlugin) : Listener {

    // 50% chance to mix in coughs with the message content, if infected
    @EventHandler(priority = EventPriority.HIGHEST)
    fun AsyncChatEvent.onChat() {
        if (!player.isInfected || Random.nextBoolean()) return

        val words = message().asString().split(" ")
        val modifiedWords = words.map { word ->
            if (Math.random() <= 0.15) "$word *cough*" else word // 15% chance to add *cough* after a word
        }

        val coughCount = modifiedWords.count { it.contains("*cough*") }
        val extraCoughs = Random.nextInt(0, coughCount + 1) // add extra coughs at the end

        message(Text(modifiedWords.joinToString(" ") + " " + "*cough* ".repeat(extraCoughs).trim()).component)
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
        unlockRecipes(player, "edulis")
        resumeCovid(player, plugin)
    }

    @EventHandler
    fun PlayerRespawnEvent.onRespawn() {
        resumeCovid(player, plugin)
    }

    @EventHandler
    fun PlayerInteractEvent.playerInteract() {
        if (isCancelled) return
        if (hand != EquipmentSlot.HAND) return

        val block = clickedBlock ?: return
        val item = player.inventory.itemInMainHand

        if (action == Action.RIGHT_CLICK_BLOCK) {
            when (block.type) {
                Material.POTTED_FERN -> {
                    val basil = Items.getItem("basil") ?: return

                    if (item.type == Material.SHEARS) {
                        isCancelled = true
                        block.world.apply {
                            dropItemNaturally(block.location.add(0.5, 1.0, 0.5), basil)
                            playSound(block.location, Sound.ENTITY_BOGGED_SHEAR, 1.0f, 2.0f)
                        }
                    }
                }

                Material.FLOWER_POT -> {
                    if (item.foodid == "basil") {
                        block.type = Material.POTTED_FERN
                        item.amount -= 1
                    }
                }

                in Tag.CROPS.values.plus(Material.SWEET_BERRY_BUSH) -> {
                    if (block.chunk.getPDC<String>(getBlockPDC(block.location, "edulis")) == null) return

                    val crop = block.state.blockData as? Ageable ?: return
                    if (crop.age >= crop.maximumAge) {
                        val sound = if (block.type == Material.SWEET_BERRY_BUSH)
                            Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES else Sound.BLOCK_CROP_BREAK

                        block.location.world.playSound(
                            block.location,
                            sound, 1f, 1f
                        )
                        dropItems(block, crop.age, setAge = true)
                        isCancelled = true
                    }

                    runLater(1, CropDisplay(block))
                }

                in Tag.SUPPORTS_VEGETATION.values -> {
                    if (item.foodid == "tomato") {
                        if (blockFace == BlockFace.UP) { // make sure player is clicking top of block
                            // make sure the space above is empty
                            val aboveBlock = block.getRelative(BlockFace.UP).takeIf { it.type == Material.AIR } ?: return

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

        when {
            "bat_wing" in item.foodid -> giveCovid(player, plugin)
            "cake_slice" in item.foodid -> player.incrementStatistic(Statistic.CAKE_SLICES_EATEN)
        }

        // save food for throw up event
        if (!item.itemMeta.hasPDC(axeKey)) {
            val id = item.itemMeta?.getPDC(itemKey) ?: item.foodid.lowercase()
            player.foodEaten = (player.foodEaten.filterNot { it == id } + id)
        }
    }
}