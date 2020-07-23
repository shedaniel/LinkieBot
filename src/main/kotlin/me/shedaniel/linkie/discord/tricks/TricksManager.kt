package me.shedaniel.linkie.discord.tricks

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.shedaniel.linkie.utils.error
import java.io.File
import java.util.*

object TricksManager {
    val tricks = mutableMapOf<UUID, Trick>()
    private val tricksFolder get() = File(File(System.getProperty("user.dir")), "tricks").also { it.mkdirs() }
    private val json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true, isLenient = true))

    fun load() {
        val tempTricks = mutableMapOf<UUID, Trick>()
        tricksFolder.listFiles { _, name -> name.endsWith(".json") }?.forEach { trickFile ->
            val trick = json.parse(Trick.serializer(), trickFile.readText())
            if (trick.id.toString() == trickFile.nameWithoutExtension) tempTricks[trick.id] = trick
            else {
                error("Invalid tricks file: " + trickFile.name)
            }
        }
        tricks.clear()
        tricks.putAll(tempTricks)
        save()
    }

    fun save() {
        tricks.forEach { (uuid, trick) ->
            val trickFile = File(tricksFolder, "$uuid.json")
            if (trickFile.exists().not()) {
                trickFile.writeText(json.stringify(Trick.serializer(), trick))
            }
        }
    }

    fun addTrick(trick: Trick) {
        require(tricks.none { it.value.name == trick.name && it.value.guildId == trick.guildId }) { "Trick with name \"${trick.name}\" already exists!" }
        tricks[trick.id] = trick
        save()
    }

    fun removeTrick(trick: Trick) {
        tricks.remove(trick.id)
        val trickFile = File(tricksFolder, "${trick.id}.json")
        trickFile.delete()
        save()
    }

    operator fun get(pair: Pair<String, Long>): Trick? = tricks.values.firstOrNull { it.name == pair.first && it.guildId == pair.second }
}