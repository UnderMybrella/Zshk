package dev.brella.zshk

import dev.brella.antlr.zshk.zshLexer
import dev.brella.antlr.zshk.zshParser
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.kornea.io.common.flow.StdinInputFlow
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.jvm.JVMInputFlow
import dev.brella.kornea.io.jvm.JVMOutputFlow
import dev.brella.kornea.toolkit.common.StdoutPrintFlow
import dev.brella.kornea.toolkit.common.printLine
import dev.brella.zshk.common.*
import dev.brella.zshk.common.args.opcodes.ZshkOpcode
import dev.brella.zshk.common.args.opcodes.exec
import dev.brella.zshk.common.args.values.ZshkIntegerLiteralArg
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream

inline fun buildLexerAndParser(builder: () -> String) =
    buildLexerAndParser(builder())

fun buildLexerAndParser(input: String): Pair<zshLexer, zshParser> {
    val charStream = CharStreams.fromString(input)
    println(input)

    val lexer = zshLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingErrorListener)

    println(
        "Tokens:\n${
            lexer.allTokens.toTypedArray().joinToString("\n") {
                if (it is CommonToken) it.toString(lexer) else it.toString()
            }
        }"
    )

    lexer.reset()

    val tokens = CommonTokenStream(lexer)
    val parser = zshParser(tokens)

    parser.removeErrorListeners()
    parser.addErrorListener(ThrowingErrorListener)

    return lexer to parser
}

suspend fun main(args: Array<String>) {
    val (lexer, parser) = buildLexerAndParser {
//        """
//        if true; echo "Hello, $? World" | rev | cat;
//        """.trimIndent()

        """
        echo $((~ testing++))
        echo $((~ ++testing))
        echo $((testing))
        
        echo "Hello, [$(echo "World")] - ${'$'}testing"
        """.trimIndent()
    }

    val visitor = ZshkVisitor()

    val env = ShellEnvironment(fileDescriptors = HashMap<Int, Pair<InputFlow?, OutputFlow?>>().apply {
        this[STDIN_FILENO] = Pair(StdinInputFlow(), null)
        this[STDOUT_FILENO] = Pair(null, JVMOutputFlow(System.out))
        this[STDERR_FILENO] = Pair(null, JVMOutputFlow(System.err))
    })

    env.DEBUG_PRETTY_PRINT = true
    env.DEBUG_INDENT_LEVEL = 1

    env.registerFunction("echo") { args, env ->
        env.stdout?.printLine(args.drop(1).joinToStringSuspend(" ") { it.toStringValue(env) })
        env.stdout?.flush()

        return@registerFunction 0
    }

    env.registerFunction("echoerr") { args, env ->
        env.stderr?.printLine(args.drop(1).joinToStringSuspend(" ") { it.toStringValue(env) })
        env.stderr?.flush()

        return@registerFunction 0
    }

    env.registerFunction("rev") { args, env ->
        val bytes = env.stdin?.readBytes() ?: return@registerFunction 0
        var index = bytes.indexOf('\n'.code.toByte())

        env.stdout?.write(
            bytes
                .sliceArray(0 until (if (index == -1) bytes.size else index))
                .reversedArray()
        )
        env.stdout?.flush()

        while (index != -1) {
            val nextIndex = bytes.indexOf('\n'.code.toByte(), index + 1)
            env.stdout?.write(
                bytes
                    .sliceArray(index until (if (nextIndex == -1) bytes.size else nextIndex))
                    .reversedArray()
            )
            env.stdout?.flush()
            index = nextIndex
        }

        return@registerFunction 0
    }

    env.registerFunction("cat") { args, env ->
        env.stdout?.write(env.stdin?.readBytes() ?: ByteArray(0))

        return@registerFunction 0
    }

    env.registerFunction("true") { _, _ -> 0 }
    env.registerFunction("false") { _, _ -> 1 }

    env.variables["testing"] = ZshkIntegerLiteralArg(128)

    val script = visitor.visitScript(parser.script())

    println(script.toDebugString(env))

    if (script is ZshkOpcode) {
        println("==Beginning Script Execution==")
        val ret = script.exec(env)
        println("==Ending    Script Execution==")
        println("Exit Code: $ret")
    }
}