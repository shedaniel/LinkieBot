package me.shedaniel.linkie.commands

import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import me.shedaniel.linkie.*

object AboutCommand : CommandBase {
    override fun execute(event: MessageCreateEvent, user: User, cmd: String, args: Array<String>, channel: MessageChannel) {
        if (args.isNotEmpty())
            throw InvalidUsageException("!$cmd")
        channel.createEmbed {
            it.setTitle("About Linkie")
            api.self.map { it.avatarUrl }.block()?.also { url -> it.setThumbnail(url) }
            it.setFooter("Requested by " + user.discriminatedName, user.avatarUrl)
            it.setDescription("i play g o o d games and i search mappings when i am bored ok?")
            it.addField("Source", "https://github.com/shedaniel/LinkieBot")
            it.setTimestampToNow()
        }.subscribe()
    }

    override fun getDescription(): String? = "Everything about Linkie."
    override fun getName(): String? = "About Command"
}