package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.Text.Companion.asString
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.listeners.handlers.BlockDrops.dropItems
import co.akoot.plugins.edulis.listeners.tasks.*
import co.akoot.plugins.edulis.util.Materials.getMaterial
import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.edulis.util.Util.foodid
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.Items.itemKey
import co.akoot.plugins.plushies.util.Recipes.unlockRecipes
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import com.dre.brewery.P
import com.dre.brewery.api.events.PlayerPukeEvent
import io.papermc.paper.event.player.AsyncChatEvent
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
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
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
    fun PlayerPukeEvent.onPuke() {
        val foods = player.foodEaten
        if (foods.isEmpty()) return

        isCancelled = true
        for (i in 0 until count) {
            runLater(2L * i) {
                val pukeItem = getMaterial(foods.random()) ?: return@runLater
                val loc = player.location.apply {
                    y += 1.1
                    pitch = pitch - 10 + Random.nextInt(20)
                    yaw = yaw - 10 + Random.nextInt(20)
                }

                player.world.dropItem(loc, pukeItem).apply {
                    velocity = loc.getDirection().multiply(0.5)
                    owner = UUID.fromString("78277c50-4e17-48bf-af38-7a25143da732") // PENJAMIN
                    ticksLived = 5600 + (0..50).random()
                    setMetadata("brewery_puke", FixedMetadataValue(P.p, true))
                }
            }
        }
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

        when {
            "bat_wing" in item.foodid -> giveCovid(player, plugin)
            "cake_slice" in item.foodid -> player.incrementStatistic(Statistic.CAKE_SLICES_EATEN)
        }

        // save food for throw up event
        val id = item.itemMeta?.getPDC(itemKey) ?: item.foodid.lowercase()
        player.foodEaten = (player.foodEaten.filterNot { it == id } + id)

    }

    private fun giveSlice(event: PlayerInteractEvent, cake: String, cutter: Block, player: Player) {
        val cakeSlice = customItems["${cake}_slice"] ?: return

        // cakes should give 8 and pies should give 4
        cakeSlice.amount = when {
            cake.endsWith("cake") -> 8
            cake == "pizza" -> 12
            else -> 4
        }

        // stop stonecutter gui from opening
        event.setUseInteractedBlock(Event.Result.DENY)
        player.inventory.itemInMainHand.amount -= 1
        cutter.world.playSound(cutter.location, Sound.BLOCK_HONEY_BLOCK_HIT, 0.2f, 2.0f)
        cutter.world.dropItemNaturally(cutter.location.add(0.5, 1.0, 0.5), cakeSlice)
    }
}