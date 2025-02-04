package co.akoot.plugins.edulis.util

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.CreateItem.getMaterial
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.tradeConfig
import org.bukkit.NamespacedKey
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.persistence.PersistentDataType

object VillagerTrades {

    private val modifiedKey = NamespacedKey("edulis","modified")

    private fun getTrades(type: String): MutableList<MerchantRecipe> {
        val trades: MutableList<MerchantRecipe> = mutableListOf()
        val section = tradeConfig.getConfigurationSection(type) ?: run {
            log.error("'$type' section missing from trades config")
            return trades
        }

        for (key in section.getKeys(false)) {
            val tradeSection = section.getConfigurationSection(key) ?: continue

            val sellSec = tradeSection.getConfigurationSection("sell") ?: continue
            val buySec = tradeSection.getConfigurationSection("buy") ?: continue

            val sellItem = getMaterial(sellSec, "type") ?: continue
            val buyItem = getMaterial(buySec, "type") ?: continue

            sellItem.amount = sellSec.getInt("amount", 1)
            buyItem.amount = buySec.getInt("amount", 1)

            trades.add(MerchantRecipe(sellItem, Int.MAX_VALUE).apply { addIngredient(buyItem) })
        }

        return trades
    }

    fun modifyTrader(trader: WanderingTrader) {
        val pdc = trader.persistentDataContainer
        if (pdc.has(modifiedKey)) return

        val currentTrades = mutableSetOf<MerchantRecipe>()
        currentTrades.apply {
            addAll(trader.recipes)
            addAll(getTrades("wandering"))
        }

        trader.recipes = currentTrades.toMutableList()
        pdc.set(modifiedKey, PersistentDataType.BOOLEAN, true)
    }

    fun modifyVillager(v: Villager) {
        val pdc = v.persistentDataContainer
        if (pdc.has(modifiedKey)) return

        v.apply {
            villagerType = Villager.Type.SWAMP
            villagerLevel = 5
            profession = Villager.Profession.NITWIT
        }

        v.recipes = getTrades("villager").toMutableList()

        pdc.set(modifiedKey, PersistentDataType.BOOLEAN, true)
    }
}