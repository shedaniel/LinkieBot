package me.shedaniel.linkie.discord

import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.shedaniel.linkie.discord.utils.discriminatedName
import me.shedaniel.linkie.discord.utils.sendEmbedMessage
import me.shedaniel.linkie.discord.utils.setTimestampToNow

class CommandMap(private val commandAcceptor: CommandAcceptor, private val defaultPrefix: String) {
    fun onMessageCreate(event: MessageCreateEvent) {
        val channel = event.message.channel.block() ?: return
        val user = event.message.author.orElse(null)?.takeUnless { it.isBot } ?: return
        val message: String = event.message.content
        val prefix = commandAcceptor.getPrefix(event) ?: defaultPrefix
        GlobalScope.launch {
            runCatching {
                if (message.toLowerCase().startsWith(prefix)) {
                    val content = message.substring(prefix.length)
                    val split = content.splitArgs().dropLastWhile(String::isEmpty)
                    if (split.isNotEmpty()) {
                        val cmd = split[0].toLowerCase()
                        val args = split.drop(1).toMutableList()
                        commandAcceptor.execute(event, prefix, user, cmd, args, channel)
                    }
                }
            }.exceptionOrNull()?.also { throwable ->
                if (throwable is SuppressedException) return@also
                try {
                    channel.sendEmbedMessage { generateThrowable(throwable, user) }.subscribe()
                } catch (throwable2: Exception) {
                    throwable2.addSuppressed(throwable)
                    throwable2.printStackTrace()
                }
            }
        }
    }

    fun String.splitArgs(): List<String> {
        val args = mutableListOf<String>()
        val stringBuilder = StringBuilder()
        forEach {
            val whitespace = it.isWhitespace()
            if (whitespace) {
                args.add(stringBuilder.toString())
                stringBuilder.clear()
            }
            if (it == '\n' || !whitespace) {
                stringBuilder.append(it)
            }
        }
        if (stringBuilder.isNotEmpty())
            args.add(stringBuilder.toString())
        return args
    }
}

interface CommandAcceptor {
    fun execute(event: MessageCreateEvent, prefix: String, user: User, cmd: String, args: MutableList<String>, channel: MessageChannel)
    fun getPrefix(event: MessageCreateEvent): String?
}

fun EmbedCreateSpec.generateThrowable(throwable: Throwable, user: User? = null) {
    setTitle("Linkie Error")
    setColor(Color.RED)
    user?.apply { setFooter("Requested by $discriminatedName", avatarUrl) }
    setTimestampToNow()
    when {
        throwable is org.graalvm.polyglot.PolyglotException -> {
            val details = throwable.localizedMessage ?: ""
            addField("Error occurred while processing the command", "```$details```", false)
        }
        throwable.javaClass.name.startsWith("org.graalvm") -> {
            val details = throwable.localizedMessage ?: ""
            addField("Error occurred while processing the command", "```" + throwable.javaClass.name + (if (details.isEmpty()) "" else ":\n") + details + "```", false)
        }
        else -> {
            addField("Error occurred while processing the command:", throwable.javaClass.simpleName + ": " + (throwable.localizedMessage ?: "Unknown Message"), false)
        }
    }
    if (isDebug)
        throwable.printStackTrace()
}