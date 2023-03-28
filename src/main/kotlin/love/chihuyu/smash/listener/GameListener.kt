package love.chihuyu.smash.listener

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashAPI.currentMap
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.inCountdown
import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.smash.game.ScoreboardUpdater
import love.chihuyu.timerapi.TimerAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.block.Block
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

        e.damage = .0
        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(4, 10)) ?: 5
        player.velocity = damager.location.direction.multiply((SmashAPI.velocities[player.uniqueId] ?: 1) / 50.0).setY(0.5)
        SmashAPI.lastAttackers[player.uniqueId] = damager.uniqueId

        ScoreboardUpdater.updateAllVelocity()
        if ((SmashAPI.velocities[player.uniqueId] ?: 0) > 70 && SmashAPI.isNotEmptyAround(player) && gameTimer != null) TimerAPI.build("Smash-Break", 16, 1) {
            tick {
                SmashAPI.breakAroundBlocks(player)
            }
        }.run()
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

        val yBlockList = mutableListOf<Block>()
        repeat(4) {
            yBlockList += player.world.getBlockAt(player.location.apply { y -= it })
        }
        if (player.isSneaking && yBlockList.none { !it.isEmpty } && player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR && gameTimer != null) {
            player.isSneaking = false
            player.velocity = player.velocity.add(Vector(.0, -1.0, .0))
            player.world.playSound(player.location, Sound.ENDERDRAGON_WINGS, .5f, 1f)

            if (!SmashAPI.isNotEmptyAround(player)) return
            TimerAPI.build("Smash-HipDrop", 30, 1, 0) {
                tick {

                    if (!player.world.getBlockAt(player.location.apply { y -= .1 }).isEmpty) {
                        player.getNearbyEntities(3.0, 2.5 ,3.0).forEach { hitted ->
                            if (hitted !is Player) return@forEach
                            SmashAPI.velocities[hitted.uniqueId] = SmashAPI.velocities[hitted.uniqueId]?.plus(nextInt(4, 10)) ?: 5
                            hitted.velocity = hitted.location.direction.multiply(-(SmashAPI.velocities[hitted.uniqueId] ?: 1) / 50.0).setY(0.5)
                            SmashAPI.lastAttackers[hitted.uniqueId] = player.uniqueId

                            ScoreboardUpdater.updateAllVelocity()
                            if ((SmashAPI.velocities[hitted.uniqueId] ?: 0) > 70 && SmashAPI.isNotEmptyAround(hitted) && gameTimer != null) TimerAPI.build("Smash-Break", 16, 1) {
                                tick {
                                    SmashAPI.breakAroundBlocks(hitted)
                                }
                            }.run()
                        }
                        kill()
                    }
                }
            }.run()
        }

        if (!player.world.getBlockAt(player.location.apply { this.y -= .1 }).isEmpty && player.uniqueId in SmashAPI.doubleJumpCooltimed) {
            SmashAPI.doubleJumpCooltimed.remove(player.uniqueId)
            player.exp = .99f
            player.allowFlight = true
        }

        if (player.location.y <= 0) {
            val spawn = if (currentMap != null) (mapsConfig.getConfigurationSection("maps.$currentMap").getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(player.world) }.random() else SmashPlugin.config.getVector("lobby-spawn").toLocation(player.world)
            player.teleport(spawn)
            player.world.playSound(player.location, Sound.EXPLODE, 1f, 1f)
            SmashAPI.velocities[player.uniqueId] = 0
            val killer = SmashAPI.lastAttackers[player.uniqueId] ?: player.uniqueId
            SmashPlugin.server.broadcastMessage(
                if (killer != player.uniqueId) {
                    SmashAPI.killCounts[killer] = SmashAPI.killCounts[killer]?.inc() ?: 1
                    SmashAPI.lastAttackers.remove(player.uniqueId)
                    "${ChatColor.RED}${Bukkit.getOfflinePlayer(killer).name}${ChatColor.RESET} killed ${ChatColor.RED}${player.name}${ChatColor.RESET}."
                } else {
                    "${ChatColor.RED}${player.name}${ChatColor.RESET} died."
                }
            )
            ScoreboardUpdater.updateAllKillCounts()
            ScoreboardUpdater.updateAllVelocity()
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (gameTimer == null || !inCountdown) {
            player.teleport(SmashPlugin.config.getVector("lobby-spawn").toLocation(player.world))
        } else {
            player.teleport((SmashPlugin.config.getConfigurationSection("maps.${currentMap}").getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(player.world) }.random())
        }

        SmashPlugin.server.onlinePlayers.forEach {
            player.showPlayer(it)
            it.showPlayer(player)
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
