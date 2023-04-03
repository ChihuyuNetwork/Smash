package love.chihuyu.smash

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

object SmashAPI {

    val killCounts = mutableMapOf<UUID, Int>()
    val velocities = mutableMapOf<UUID, Int>()
    val doubleJumpCooltimed = mutableListOf<UUID>()
    val lastAttackers = mutableMapOf<UUID, UUID>()

    var currentMap: String? = null

    fun isNotEmptyAround(player: Player) = !player.world.getBlockAt(player.location.apply { this.x -= .5 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { x += .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { z -= .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { x += .9 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { y += 1 }).isEmpty ||
            !player.world.getBlockAt(player.location.apply { y += 2 }).isEmpty

    fun breakAroundBlocks(player: Player) {
        val yList = listOf(
            player.world.getBlockAt(player.location.apply { y += 2 }),
            player.world.getBlockAt(player.location.apply { y += 1 }),
            player.world.getBlockAt(player.location.apply { y }),
            player.world.getBlockAt(player.location.apply { y -= .5 }),
        )
        val zList = mapOf(
            player.world.getBlockAt(player.location.apply { z -= .9 }) to yList,
            player.world.getBlockAt(player.location.apply { z }) to yList,
            player.world.getBlockAt(player.location.apply { z += .9 }) to yList
        )

        mapOf(
            player.world.getBlockAt(player.location.apply { x -= .9 }) to zList,
            player.world.getBlockAt(player.location.apply { x += .9 }) to zList
        ).forEach { (xBlock, z) ->
            if (!xBlock.isEmpty && SmashPlugin.gameTimer != null) {
                xBlock.type = Material.AIR
                player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
            }
            z.forEach { (zBlock, y) ->
                if (!zBlock.isEmpty && SmashPlugin.gameTimer != null) {
                    zBlock.type = Material.AIR
                    player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                }
                y.forEach { yBlock ->
                    if (!yBlock.isEmpty && SmashPlugin.gameTimer != null) {
                        yBlock.type = Material.AIR
                        player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                    }
                }
            }
        }
    }
}
