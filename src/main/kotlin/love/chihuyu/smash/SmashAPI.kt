package love.chihuyu.smash

import java.util.*

object SmashAPI {

    val killCounts = mutableMapOf<UUID, Int>()
    val velocities = mutableMapOf<UUID, Int>()
    val doubleJumpCooltimed = mutableListOf<UUID>()
    val lastAttackers = mutableMapOf<UUID, UUID>()

    var currentMap: String? = null
}
