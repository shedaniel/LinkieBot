package me.shedaniel.linkie.discord.scripting

import p0nki.pesl.api.PESLContext
import p0nki.pesl.api.PESLEvalException
import p0nki.pesl.api.`object`.PESLObject
import p0nki.pesl.api.parse.ASTNode
import p0nki.pesl.api.parse.PESLParseException
import p0nki.pesl.api.parse.PESLParser
import p0nki.pesl.api.token.PESLTokenList
import p0nki.pesl.api.token.PESLTokenizeException
import p0nki.pesl.api.token.PESLTokenizer

object LinkieScripting {
    private val tokenizer by lazy { PESLTokenizer() }
    private val parser by lazy { PESLParser() }
    val simpleContext = simpleContext()

    fun simpleContext() = context {
        it("math", ContextExtensions.math)
        it("system", ContextExtensions.system)
    }

    inline fun context(crossinline builder: ((String, PESLObject) -> Unit) -> Unit): PESLContext {
        return PESLContext(null, mutableMapOf()).context(builder)
    }

    inline fun PESLContext.context(crossinline builder: ((String, PESLObject) -> Unit) -> Unit): PESLContext {
        builder { key, value ->
            this[key] = value
        }
        return this
    }

    fun eval(context: PESLContext, cmd: String) {
        val tokens = parseTokens(cmd)
        while (tokens.hasAny()) {
            val node = parseExpression(cmd, tokens)
            evalNode(context, node)
        }
    }

    private fun parseTokens(cmd: String): PESLTokenList {
        try {
            return tokenizer.tokenize(cmd)
        } catch (e: PESLTokenizeException) {
            throw TokenizeException(cmd, e)
        }
    }

    private fun parseExpression(cmd: String, tokens: PESLTokenList): ASTNode {
        try {
            return parser.parseExpression(tokens)
        } catch (e: PESLParseException) {
            throw ParseException(cmd, e)
        }
    }

    private fun evalNode(context: PESLContext, node: ASTNode) {
        try {
            node.evaluate(context)
        } catch (e: PESLEvalException) {
            throw EvalException(e)
        }
    }
}

class EvalException(private val exception: PESLEvalException) : RuntimeException() {
    override val message: String?
        get() = exception.`object`.toString()
}

class TokenizeException(private val cmd: String, private val exception: PESLTokenizeException) : RuntimeException() {
    override val message: String?
        get() = buildString {
            append("${exception.localizedMessage}\n\n")
            append(cmd)
            append('\n')
            for (i in 1..exception.index) append(' ')
            append('^')
        }
}

class ParseException(private val cmd: String, private val exception: PESLParseException) : RuntimeException() {
    override val message: String?
        get() = buildString {
            append("${exception.localizedMessage}\n\n")
            append(cmd)
            append('\n')
            for (i in 1..exception.token.start) append(' ')
            for (i in exception.token.start until exception.token.end) append('^')
        }
}