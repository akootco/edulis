package co.akoot.plugins.edulis.listeners.tasks

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.log
import com.dre.brewery.BPlayer
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

class Covid(private val player: Player, private val plugin: FoxPlugin) : BukkitRunnable() {

    private var lastEffectTime = 0L

    override fun run() {
        val currentTime = System.currentTimeMillis()
        val pdc = player.persistentDataContainer

        if (player.isDead || !player.isOnline) {
            val endTime = pdc.get(endKey, PersistentDataType.LONG)
            val remainingTimeMillis = endTime?.minus(currentTime)

            if (remainingTimeMillis != null) {
                pdc.set(remainingKey, PersistentDataType.LONG, remainingTimeMillis)
                player.saveData() // need to run this or the pdc isn't saved
            }

            log.info("${player.name}'s contagion has been paused")
            return cancel()  // stop the task since we don't need it anymore
        }

        val endTime = pdc.get(endKey, PersistentDataType.LONG)

        if (endTime == null || currentTime > endTime) {
            player.apply {
                sendMessage("You are no longer contagious.")
                clearActivePotionEffects()
            }
            pdc.apply {
                set(caughtKey, PersistentDataType.BOOLEAN, true) // this will give the player access to /immune
                remove(endKey)
            }
            return cancel() // cancel the task
        }

        // 50% chance to trigger effect, every 3 minutes
        if (currentTime - lastEffectTime >= 180 * 1000 && Random.nextDouble() > 0.5) {
            // reset timer
            lastEffectTime = currentTime

            // 50% chance on which effect to give
            if (Random.nextDouble() < 0.5) {
                if (Bukkit.getPluginManager().isPluginEnabled("brewery")) {
                    BPlayer.addPuke(player, 64)
                } else player.apply {
                    addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 200, 10))
                    addPotionEffect(PotionEffect(PotionEffectType.POISON, 100, 0))
                }
            }
        }

        for (target in player.getNearbyEntities(3.0, 3.0, 3.0)) { // spread it
            if (target is Player && !target.persistentDataContainer.has(endKey)) {
                giveCovid(target, plugin, true, player.name)
            }
        }

        player.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, -1, 10))
        // gonna change unluck in the texture pack
        player.addPotionEffect(PotionEffect(PotionEffectType.UNLUCK, -1, 0))
    }

    companion object {
        private val covidTask = mutableMapOf<Player, BukkitTask>()
        val endKey = NamespacedKey("covid", "end")
        val remainingKey = NamespacedKey("covid", "remaining")
        val caughtKey = NamespacedKey("covid", "experienced")
        val immuneKey = NamespacedKey("covid", "immune")

        fun giveCovid(player: Player, plugin: FoxPlugin, wasCaught: Boolean = false, spreader: String? = null) {
            covidTask[player]?.cancel() //  if the player is already infected, cancel the task, so it doesn't run multiple times

            if (player.persistentDataContainer.get(immuneKey, PersistentDataType.BOOLEAN) == true) return

            player.apply {
                sendMessage((Text("You have been infected", "error_accent")
                        + (Text(if (wasCaught) " by $spreader!" else "!", "error_accent"))).component)

                persistentDataContainer.set(
                    endKey, PersistentDataType.LONG,
                    System.currentTimeMillis() + (30 * 60 * 1000L) // set the end time to 30 minutes
                )
            }
            covidTask[player] = Covid(player, plugin).runTaskTimer(plugin, 1L, 100L)
        }

        fun resumeCovid(player: Player, plugin: FoxPlugin) {
            val pdc = player.persistentDataContainer

            pdc.get(remainingKey, PersistentDataType.LONG)?.let { remainingTime ->

                pdc.apply {
                    remove(remainingKey)
                    set(endKey, PersistentDataType.LONG, System.currentTimeMillis() + remainingTime)
                }

                log.info("${player.name}'s contagion has been resumed, ${remainingTime / 60000} minutes remaining")
                giveCovid(player, plugin)
            }
        }
    }
}