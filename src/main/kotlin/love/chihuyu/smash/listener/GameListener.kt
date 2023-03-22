package love.chihuyu.smash.listener

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.inCountdown
import love.chihuyu.smash.game.ScoreboardUpdater
import love.chihuyu.timerapi.timer.Timer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import kotlin.random.Random.Default.nextInt

object GameListener : Listener {

    @EventHandler
    fun onHit(e: EntityDamageByEntityEvent) {
        if (inCountdown || gameTimer == null) {
            e.isCancelled = true
            return
        }

        val player = e.entity as? Player ?: return
        val damager = e.damager

        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(4, 10)) ?: 5
        player.velocity = damager.location.direction.multiply((SmashAPI.velocities[player.uniqueId] ?: 1) / 50.0).setY(0.5)
        SmashAPI.lastAttackers[player.uniqueId] = damager.uniqueId

        ScoreboardUpdater.updateAllVelocity()

        val yList = listOf(
            player.world.getBlockAt(player.location.apply { this.y += 2 }),
            player.world.getBlockAt(player.location.apply { this.y += 1 }),
            player.world.getBlockAt(player.location.apply { this.y })
        )
        val zList = mapOf(
            player.world.getBlockAt(player.location.apply { this.z -= .9 }) to yList,
            player.world.getBlockAt(player.location.apply { this.z }) to yList,
            player.world.getBlockAt(player.location.apply { this.z += .9 }) to yList
        )

        fun isNotEmptyAround() = !player.world.getBlockAt(player.location.apply { this.x -= .5 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { this.x += .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { this.z -= .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { this.x += .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { this.y += 1 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { this.y += 2 }).isEmpty

        Timer("Smash-Velocity-${player.uniqueId}", 10, 1)
            .tick {
                if ((SmashAPI.velocities[player.uniqueId] ?: 0) > 50 && isNotEmptyAround()) {
                    mapOf(
                        player.world.getBlockAt(player.location.apply { this.x -= .9 }) to zList,
                        player.world.getBlockAt(player.location.apply { this.x += .9 }) to zList
                    ).forEach { (xBlock, z) ->
                        if (!xBlock.isEmpty) {
                            xBlock.type = Material.AIR
                            player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                        }
                        z.forEach { (zBlock, y) ->
                            if (!zBlock.isEmpty) {
                                zBlock.type = Material.AIR
                                player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                            }
                            y.forEach { yBlock ->
                                if (!yBlock.isEmpty) {
                                    yBlock.type = Material.AIR
                                    player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                                }
                            }
                        }
                    }
                }
            }.runAsync()
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player ?: return

        player.allowFlight = true
        if (player.isFlying && player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
            player.isFlying = false
            val uuid = player.uniqueId
            if (uuid !in SmashAPI.doubleJumpCooltimed) {
                SmashAPI.doubleJumpCooltimed.add(uuid)
                player.velocity = player.velocity.setY(0.9).setX(player.location.direction.x * 0.9).setZ(player.location.direction.z * 0.9)
                player.exp = 0f
                player.allowFlight = false
                player.world.playSound(player.location, Sound.ENDERDRAGON_WINGS, .5f, 1f)
            } else {
                player.velocity = player.velocity.add(Vector(.0, .01, .0))
            }
        }

        if (!player.world.getBlockAt(player.location.apply { this.y -= .1 }).isEmpty && player.uniqueId in SmashAPI.doubleJumpCooltimed) {
            SmashAPI.doubleJumpCooltimed.remove(player.uniqueId)
            player.exp = .99f
            player.allowFlight = true
        }

        if (player.location.y <= 0) {
            player.teleport((SmashPlugin.mapsConfig.getConfigurationSection("maps.${SmashAPI.currentMap}").getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(player.world) }.random())
            player.world.playSound(player.location, Sound.EXPLODE, 1f, 1f)
            SmashAPI.velocities[player.uniqueId] = 0
            val killer = SmashAPI.lastAttackers[player.uniqueId] ?: player.uniqueId
            if (killer != player.uniqueId) {
                SmashAPI.killCounts[killer] = SmashAPI.killCounts[killer]?.inc() ?: 1
                SmashAPI.lastAttackers.remove(player.uniqueId)
                ScoreboardUpdater.updateAllKillCounts()
                ScoreboardUpdater.updateAllVelocity()
            }
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (gameTimer == null && !inCountdown) {
            player.teleport(SmashPlugin.SmashPlugin.config.getVector("lobby-spawn").toLocation(player.world))
        } else {
            player.teleport((SmashPlugin.mapsConfig.getConfigurationSection("maps.${SmashAPI.currentMap}").getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(player.world) }.random())
        }
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        if ((gameTimer == null || inCountdown) && player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
            return
        }

        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(4, 10)) ?: 5
        ScoreboardUpdater.updateVelocity(player)
    }
}
