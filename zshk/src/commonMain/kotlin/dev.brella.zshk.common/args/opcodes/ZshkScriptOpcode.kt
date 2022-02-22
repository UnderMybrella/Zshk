package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.*

data class ZshkScriptOpcode(val operands: List<ZshkOpcode>): ZshkOpcode {
    override suspend fun ShellEnvironment.exec() =
        operands.fold(0) { _, op -> op.exec(this) }

    override suspend fun toDebugString(env: ShellEnvironment): String =
        if (env.DEBUG_PRETTY_PRINT) {
            buildString {
                appendLine("SCRIPT(")

                env.withIndented {
                    operands.joinToSuspend(this, "\n") { "${env.getDebugIndent()}${it.toDebugString(env)}" }
                }

                appendLine()

                appendIndent(env)
                append(")")
            }
        } else {
            "SCRIPT(${operands.joinToStringSuspend { it.toDebugString(env) }})"
        }
}