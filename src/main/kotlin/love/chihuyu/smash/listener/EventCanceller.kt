package love.chihuyu.smash.listener

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.weather.WeatherChangeEvent

object EventCanceller: Listener {

    @EventHandler
    fun onWeather(e: WeatherChangeEvent) {
        e.isCancelled = true
    }

    @EventHandler
    fun onHunger(e: FoodLevelChangeEvent) {
        (e.entity as? Player ?: return).foodLevel = 20
        e.isCancelled = true
    }
}