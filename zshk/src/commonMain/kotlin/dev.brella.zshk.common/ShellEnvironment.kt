package dev.brella.zshk.common

import dev.brella.kornea.io.common.flow.BinaryPipeFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.zshk.common.args.ZshkPipeArg
import dev.brella.zshk.common.args.ZshkArg
import dev.brella.zshk.common.args.opcodes.ZshkOpcode
import dev.brella.zshk.common.args.opcodes.exec
import dev.brella.zshk.common.args.values.ZshkValueArg

typealias ShellFunction=suspend (List<ZshkArg>, ShellEnvironment) -> Int

data class ShellEnvironment(
    val variables: MutableMap<String, ZshkValueArg<*>> = HashMap(),
    val environmentalVariables: MutableMap<String, ZshkValueArg<*>> = HashMap(),
    val functionsInScope: MutableMap<String, ShellFunction> = HashMap(),
    var exitCode: Int = 0,

    var DEBUG_PRETTY_PRINT: Boolean = false,
    var DEBUG_INDENT_LEVEL: Int = 0,

    val stdin: InputFlow,
    val stdout: OutputFlow,
    val stderr: OutputFlow
) {
    inline fun pipe(type: ZshkPipeArg, left: (ShellEnvironment) -> Unit, right: (ShellEnvironment) -> Unit) {
        when (type) {
            is ZshkPipeArg.StdoutToStdin -> {
                val io = BinaryPipeFlow()

                val l = copy(stdout = io)
                val r = copy(stdin = io)

                left(l)
                right(r)
            }
            is ZshkPipeArg.StdoutAndStderrToStdin -> {
                val io = BinaryPipeFlow()

                val l = copy(stdout = io, stderr = io)
                val r = copy(stdin = io)

                left(l)
                right(r)
            }
        }
    }

    fun getFunction(name: String): ShellFunction? =
        functionsInScope[name]

    fun registerFunction(name: String, function: ShellFunction) =
        functionsInScope.put(name, function)
}

inline fun ShellEnvironment.getDebugIndent(): String =
    CharArray(DEBUG_INDENT_LEVEL) { '\t' }.concatToString()

inline fun StringBuilder.appendIndent(env: ShellEnvironment): StringBuilder {
    repeat(env.DEBUG_INDENT_LEVEL) { append('\t') }
    return this
}

inline fun <T> ShellEnvironment.withIndented(block: () -> T): T {
    try {
        this.DEBUG_INDENT_LEVEL++
        return block()
    } finally {
        this.DEBUG_INDENT_LEVEL--
    }
}

suspend inline fun ShellEnvironment.pipe(left: ZshkOpcode, type: ZshkPipeArg, right: ZshkOpcode): Int {
    var exitCode = 0
    pipe(type, { exitCode = left.exec(it) }, { exitCode = right.exec(it) })
    return exitCode
}