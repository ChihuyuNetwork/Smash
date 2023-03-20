package love.chihuyu.smash.listener

import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent

object EventCanceller : Listener {

    @EventHandler
    fun onWeather(e: WeatherChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onHunger(e: FoodLevelChangeEvent) {
        (e.entity as? Player ?: return).foodLevel = 20
        e.isCancelled = true
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        e.damage = .0
    }

    @EventHandler
    fun onBreak(e: BlockFadeEvent) {
        e.isCancelled = gameTimer == null
    }

    @EventHandler
    fun onChange(e: BlockFromToEvent) {
        e.isCancelled = gameTimer == null
    }
}
