package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.appendIndent
import dev.brella.zshk.common.args.ZshkPipeArg
import dev.brella.zshk.common.pipe
import dev.brella.zshk.common.withIndented

data class ZshkPipeOpcode(val left: ZshkOpcode, val pipe: ZshkPipeArg, val right: ZshkOpcode) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int =
        pipe(left, pipe, right)

    override suspend fun toDebugString(env: ShellEnvironment): String =
        if (env.DEBUG_PRETTY_PRINT) {
            buildString {
                appendLine("PIPED(")

                env.withIndented {
                    appendIndent(env)
                    append(left.toDebugString(env))
                    appendLine()

                    appendIndent(env)
                    append('|')
                    append(pipe.toDebugString(env))
                    appendLine("|")

                    appendIndent(env)
                    append(right.toDebugString(env))
                    appendLine()
                }

                appendIndent(env)
                append(")")
            }
        } else {
            "PIPE_COMMAND(${left.toDebugString(env)} |${pipe.toDebugString(env)}| ${right.toDebugString(env)})"
        }
}