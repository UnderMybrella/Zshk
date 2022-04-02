package dev.brella.zshk.common

import dev.brella.kornea.io.common.flow.BinaryPipeFlow
import dev.brella.kornea.io.common.flow.InputFlow
import dev.brella.kornea.io.common.flow.OutputFlow
import dev.brella.zshk.common.args.ZshkPipeArg
import dev.brella.zshk.common.args.ZshkArg
import dev.brella.zshk.common.args.opcodes.ZshkOpcode
import dev.brella.zshk.common.args.opcodes.exec
import dev.brella.zshk.common.args.values.ZshkValueArg
import kotlinx.atomicfu.atomic

typealias ShellFunction = suspend (List<ZshkArg>, ShellEnvironment) -> Int

const val STDIN_FILENO = 0
const val STDOUT_FILENO = 1
const val STDERR_FILENO = 2

public var ShellEnvironment.stdin: InputFlow?
    get() = fileDescriptors[STDIN_FILENO]?.first
    set(value) {
        fileDescriptors[STDIN_FILENO] = Pair(value, null)
    }

public var ShellEnvironment.stdout: OutputFlow?
    get() = fileDescriptors[STDOUT_FILENO]?.second
    set(value) {
        fileDescriptors[STDOUT_FILENO] = Pair(null, value)
    }

public var ShellEnvironment.stderr: OutputFlow?
    get() = fileDescriptors[STDERR_FILENO]?.second
    set(value) {
        fileDescriptors[STDERR_FILENO] = Pair(null, value)
    }


data class ShellEnvironment(
    val parent: ShellEnvironment? = null,

    val variables: MutableMap<String, ZshkValueArg<*>> = HashMap(),
    val environmentalVariables: MutableMap<String, ZshkValueArg<*>> = HashMap(),
    val functionsInScope: MutableMap<String, ShellFunction> = HashMap(),

    var DEBUG_PRETTY_PRINT: Boolean = false,
    var DEBUG_INDENT_LEVEL: Int = 0,

    val fileDescriptors: MutableMap<Int, Pair<InputFlow?, OutputFlow?>> = HashMap()
) {
    private val _exitCode = atomic<Int>(0)
    public var exitCode by _exitCode

    fun fork(
        variables: MutableMap<String, ZshkValueArg<*>>? = null,
        environmentalVariables: MutableMap<String, ZshkValueArg<*>>? = null,
        functionsInScope: MutableMap<String, ShellFunction>? = null,
        fileDescriptorsBlock: (MutableMap<Int, Pair<InputFlow?, OutputFlow?>>.() -> Unit)? = null
    ): ShellEnvironment =
        copy(
            parent = this,
            variables = variables ?: HashMap(),
            environmentalVariables = environmentalVariables ?: HashMap(),
            functionsInScope = functionsInScope ?: HashMap(),
            fileDescriptors = HashMap(fileDescriptors).also { fileDescriptorsBlock?.invoke(it) }
        )

    inline fun pipe(type: ZshkPipeArg, left: (ShellEnvironment) -> Unit, right: (ShellEnvironment) -> Unit) {
        when (type) {
            is ZshkPipeArg.StdoutToStdin -> {
                val io = BinaryPipeFlow()

                val l = fork { this[STDOUT_FILENO] = Pair(null, io) }
                val r = fork { this[STDIN_FILENO] = Pair(io, null) }

                left(l)
                right(r)
            }
            is ZshkPipeArg.StdoutAndStderrToStdin -> {
                val io = BinaryPipeFlow()

                val l = fork {
                    this[STDOUT_FILENO] = Pair(null, io)
                    this[STDERR_FILENO] = Pair(null, io)
                }
                val r = fork { this[STDIN_FILENO] = Pair(io, null) }

                left(l)
                right(r)
            }
        }
    }

    fun getFunction(name: String): ShellFunction? =
        functionsInScope[name] ?: parent?.getFunction(name)

    fun registerFunction(name: String, function: ShellFunction) =
        functionsInScope.put(name, function)

    fun getVariable(name: String): ZshkValueArg<*>? =
        variables[name] ?: environmentalVariables[name] ?: parent?.getVariable(name)
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