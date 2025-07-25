package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.extensions.hasPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.Edulis.Companion.traderConfig
import co.akoot.plugins.edulis.util.Materials.getMaterial
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.MerchantRecipe

object VillagerTrades {

    private val modifiedKey = key("modified")

    private fun getTrades(type: String): MutableList<MerchantRecipe> {
        val trades: MutableList<MerchantRecipe> = mutableListOf()

        for (key in traderConfig.getKeys(type)) {
            val sell = traderConfig.getString("$type.$key.sell")?.split("/") ?: continue
            val buy = traderConfig.getString("$type.$key.buy")?.split("/") ?: continue

            val sellMaterial = getMaterial(sell[0]) ?: continue
            val buyMaterial = getMaterial(buy[0]) ?: continue

            sellMaterial.amount = sell.getOrNull(1)?.toIntOrNull() ?: 1
            buyMaterial.amount = buy.getOrNull(1)?.toIntOrNull() ?: 1

            trades.add(MerchantRecipe(sellMaterial, Int.MAX_VALUE).apply { addIngredient(buyMaterial) })
        }

        return trades
    }


    fun modifyTrader(trader: WanderingTrader) {
        if (trader.hasPDC(modifiedKey)) return

        val currentTrades = mutableSetOf<MerchantRecipe>()
        currentTrades.apply {
            addAll(trader.recipes)
            addAll(getTrades("wandering_trader"))
        }

        trader.recipes = currentTrades.toMutableList()
        trader.setPDC<Byte>(modifiedKey, 1)
    }

    fun modifyVillager(villager: Villager) {
        if (villager.hasPDC(modifiedKey)) return

        villager.apply {
            villagerType = Villager.Type.JUNGLE
            villagerLevel = 5
            profession = Villager.Profession.NITWIT
            recipes = getTrades("villager").toMutableList()
            setPDC<Byte>(modifiedKey, 1)
        }
    }
}