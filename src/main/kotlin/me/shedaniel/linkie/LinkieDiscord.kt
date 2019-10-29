package me.shedaniel.linkie

import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.Channel
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.guild.MemberJoinEvent
import discord4j.core.event.domain.guild.MemberLeaveEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import me.shedaniel.linkie.commands.FabricApiVersionCommand
import me.shedaniel.linkie.kcommands.*
import reactor.core.publisher.Mono
import java.time.Instant

val api: DiscordClient by lazy {
    DiscordClientBuilder(System.getenv("TOKEN")).build()
}
var commandApi: CommandApi = CommandApi("+")

fun start() {
    startLoop()
    api.login().subscribe {
        api.eventDispatcher.on(MessageCreateEvent::class.java).subscribe(commandApi::onMessageCreate)
        commandApi.registerCommand(KYarnClassCommand, "yc")
        commandApi.registerCommand(POMFClassCommand, "mcpc")
        commandApi.registerCommand(KYarnMethodCommand, "ym")
        commandApi.registerCommand(POMFMethodCommand, "mcpm")
        commandApi.registerCommand(KYarnFieldCommand, "yf")
        commandApi.registerCommand(POMFFieldCommand, "mcpf")
        commandApi.registerCommand(KHelpCommand, "help", "?", "commands")
        commandApi.registerCommand(FabricApiVersionCommand(), "fabricapi")
        api.eventDispatcher.on(MemberJoinEvent::class.java).subscribe { event ->
            if (event.guildId.asLong() == 621271154019270675L)
                api.getChannelById(Snowflake.of(621298431855427615L)).filter { c -> c.type == Channel.Type.GUILD_TEXT }.subscribe { textChannel ->
                    val member = event.member
                    val guild = event.guild.block()
                    (textChannel as TextChannel).createMessage {
                        it.setEmbed {
                            it.setTitle("Welcome **${member.discriminatedName}**! #${guild?.memberCount?.asInt}")
                            it.setThumbnail(member.avatarUrl)
                            it.setTimestamp(Instant.now())
                            it.setDescription("Welcome ${member.discriminatedName} to `${guild?.name}`. \n\nEnjoy your stay!")
                        }
                    }.subscribe()
                }
            else if (event.guildId.asLong() == 432055962233470986L)
                api.getChannelById(Snowflake.of(432057546589601792L)).filter { c -> c.type == Channel.Type.GUILD_TEXT }.subscribe { textChannel ->
                    val member = event.member
                    val guild = event.guild.block()
                    (textChannel as TextChannel).createMessage {
                        it.setEmbed {
                            it.setTitle("Welcome **${member.discriminatedName}**! #${guild?.memberCount?.asInt}")
                            it.setThumbnail(member.avatarUrl)
                            it.setTimestamp(Instant.now())
                            it.setDescription("Welcome ${member.discriminatedName} to `${guild?.name}`. Get mod related support at <#576851123345031177>, <#582248149729411072>, <#593809520682205184> and <#576851701911388163>, and chat casually at <#432055962233470988>!\n" +
                                    "\n" +
                                    "Anyways, enjoy your stay!")
                        }
                    }.subscribe()
                }
        }
        api.eventDispatcher.on(MemberLeaveEvent::class.java).subscribe { event ->
            if (event.guildId.asLong() == 621271154019270675L)
                api.getChannelById(Snowflake.of(621298431855427615L)).filter { c -> c.type == Channel.Type.GUILD_TEXT }.subscribe send@{ textChannel ->
                    val member = event.member.orElse(null) ?: return@send
                    (textChannel as TextChannel).createMessage {
                        it.setEmbed {
                            it.setTitle("Goodbye **${member.discriminatedName}**! Farewell.")
                            it.setThumbnail(member.avatarUrl)
                            it.setTimestamp(Instant.now())
                        }
                    }.subscribe()
                }
            else if (event.guildId.asLong() == 432055962233470986L)
                api.getChannelById(Snowflake.of(432057546589601792L)).filter { c -> c.type == Channel.Type.GUILD_TEXT }.subscribe send@{ textChannel ->
                    val member = event.member.orElse(null) ?: return@send
                    (textChannel as TextChannel).createMessage {
                        it.setEmbed {
                            it.setTitle("Goodbye **${member.discriminatedName}**! Farewell.")
                            it.setThumbnail(member.avatarUrl)
                            it.setTimestamp(Instant.now())
                        }
                    }.subscribe()
                }
        }
    }
}

val Member.discriminatedName: String
    get() = "${username}#${discriminator}"

fun EmbedCreateSpec.setTimestampToNow(): EmbedCreateSpec =
        setTimestamp(Instant.now())

fun EmbedCreateSpec.addField(name: String, value: String): EmbedCreateSpec =
        addField(name, value, false)

fun EmbedCreateSpec.addInlineField(name: String, value: String): EmbedCreateSpec =
        addField(name, value, true)

fun Message.addReaction(unicode: String): Mono<Void> = addReaction(ReactionEmoji.unicode(unicode))
fun Message.subscribeReaction(unicode: String) {
    addReaction(unicode).subscribe()
}

fun Message.subscribeReactions(vararg unicodes: String) = unicodes.forEach(::subscribeReaction)