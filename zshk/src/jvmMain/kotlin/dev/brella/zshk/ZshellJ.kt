package dev.brella.zshk

import dev.brella.antlr.zshk.zshLexer
import dev.brella.antlr.zshk.zshParser
import dev.brella.kornea.io.common.flow.PrintOutputFlow
import dev.brella.kornea.io.common.flow.readBytes
import dev.brella.kornea.io.jvm.JVMInputFlow
import dev.brella.kornea.io.jvm.JVMOutputFlow
import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.opcodes.ZshkOpcode
import dev.brella.zshk.common.args.opcodes.exec
import dev.brella.zshk.common.joinToStringSuspend
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.CommonTokenStream
import java.io.PrintStream

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
        echo $((5 ** 2 + 2 << 1))
        """.trimIndent()
    }

    val visitor = ZshellJVisitor()

    val env = ShellEnvironment(
        stdin = JVMInputFlow(System.`in`, "stdin"),
        stdout = JVMOutputFlow(System.out),
        stderr = JVMOutputFlow(System.err)
    )

    env.DEBUG_PRETTY_PRINT = true
    env.DEBUG_INDENT_LEVEL = 1

    env.registerFunction("echo") { args, env ->
        env.stdout.write(args.drop(1).joinToStringSuspend(" ") { it.toStringValue(env) }.encodeToByteArray())
        env.stdout.write('\n'.code)
        env.stdout.flush()

        return@registerFunction 0
    }

    env.registerFunction("rev") { args, env ->
        val bytes = env.stdin.readBytes()
        var index = bytes.indexOf('\n'.code.toByte())

        env.stdout.write(
            bytes
                .sliceArray(0 until (if (index == -1) bytes.size else index))
                .reversedArray()
        )
        env.stdout.flush()

        while (index != -1) {
            val nextIndex = bytes.indexOf('\n'.code.toByte(), index + 1)
            env.stdout.write(
                bytes
                    .sliceArray(index until (if (nextIndex == -1) bytes.size else nextIndex))
                    .reversedArray()
            )
            env.stdout.flush()
            index = nextIndex
        }

        return@registerFunction 0
    }

    env.registerFunction("cat") { args, env ->
        env.stdout.write(env.stdin.readBytes())

        return@registerFunction 0
    }

    env.registerFunction("true") { _, _ -> 0 }
    env.registerFunction("false") { _, _ -> 1 }

    val script = visitor.visitScript(parser.script())

    println(script.toDebugString(env))

    if (script is ZshkOpcode) {
        println("==Beginning Script Execution==")
        val ret = script.exec(env)
        println("==Ending    Script Execution==")
        println("Exit Code: $ret")
    }
}