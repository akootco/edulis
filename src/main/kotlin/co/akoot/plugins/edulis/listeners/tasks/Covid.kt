package co.akoot.plugins.edulis.listeners.tasks

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.TimeUtil.getTimeString
import co.akoot.plugins.bluefox.util.TimeUtil.parseTime
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.plushies.util.Util.pl
import com.dre.brewery.BPlayer
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

class Covid(private val player: Player, private val plugin: FoxPlugin) : BukkitRunnable() {

    private var lastEffectTime = 0L

    override fun run() {
        val endTime = player.getPDC<Long>(endKey)

        if (endTime == null || System.currentTimeMillis() > endTime) {
            player.apply {
                sendMessage("You are no longer contagious.")
                clearActivePotionEffects()
                setPDC<Byte>(caughtKey, 1)
                removePDC(endKey)
            }

            covidTask.remove(player)
            return cancel() // cancel the task
        }

        // 50% chance to trigger effect, every 3 minutes
        if (System.currentTimeMillis() - lastEffectTime >= 180 * 1000 && Random.nextBoolean()) {
            // reset timer
            lastEffectTime = System.currentTimeMillis()
            player.chat("*cough*")

            // 50% chance on which effect to give
            if (Random.nextBoolean()) {
                if (pluginEnabled("brewery")) {
                    BPlayer.addPuke(player, 64)
                } else {
                    player.addPotionEffects(listOf(
                        PotionEffect(PotionEffectType.DARKNESS, 200, 10),
                        PotionEffect(PotionEffectType.POISON, 100, 0)
                    ))
                }

            }
        }

        for (target in player.getNearbyEntities(3.0, 3.0, 3.0)) { // spread it
            if (target is Player && target.getPDC<Long>(endKey) == null) {
                giveCovid(target, plugin, true, player.name)
            }
        }

        player.addPotionEffects(listOf(
                PotionEffect(PotionEffectType.WEAKNESS, -1, 4),
                PotionEffect(PotionEffectType.UNLUCK, -1, 0)
            )
        )
    }

    companion object {
        private val covidTask = mutableMapOf<Player, BukkitTask>()
        val endKey = NamespacedKey("covid", "end")
        val remainingKey = NamespacedKey("covid", "remaining")
        val caughtKey = NamespacedKey("covid", "experienced")
        val immuneKey = NamespacedKey("covid", "immune")

        fun giveCovid(player: Player, plugin: FoxPlugin, wasCaught: Boolean = false, spreader: String? = null) {
            covidTask[player]?.cancel() //  if the player is already infected, cancel the task, so it doesn't run multiple times

            if (player.hasMetadata("CITIZENS_NPC")) return

            if (player.getPDC<Byte>(immuneKey) != null) return

            player.apply {
                Text(this) {
                    Kolor.ERROR("You have been infected") +
                            if (wasCaught) (Kolor.ERROR(" by ") + Kolor.ERROR.player(spreader ?: "someone"))
                            else Kolor.ERROR("!")
                }

                setPDC(endKey, System.currentTimeMillis() + parseTime("30m")) // set the end time to 30 minutes
            }
            covidTask[player] = Covid(player, plugin).runTaskTimer(plugin, 1L, 100L)
            pl.logger.info("${player.name} has been infected")
        }

        fun resumeCovid(player: Player, plugin: FoxPlugin) {
            val remainingTime = player.getPDC<Long>(remainingKey) ?: return

            player.apply {
                removePDC(remainingKey)
                setPDC(endKey, System.currentTimeMillis() + remainingTime)
            }

            log.info("${player.name}'s contagion has been resumed, ${getTimeString(remainingTime)} remaining")
            giveCovid(player, plugin)
        }

        fun pauseCovid(player: Player) {
            val currentTime = System.currentTimeMillis()
            val endTime = player.getPDC<Long>(endKey)

            val remainingTimeMillis = endTime?.minus(currentTime)

            if (remainingTimeMillis != null) {
                player.setPDC(remainingKey, remainingTimeMillis)
                player.saveData() // need to run this or the pdc isn't saved


                covidTask[player]?.cancel()
                covidTask.remove(player)
                log.info("${player.name}'s contagion has been paused")
            }
        }
    }
}