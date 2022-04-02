package dev.brella.zshk.common.args.values

import dev.brella.kornea.io.common.flow.BinaryPipeFlow
import dev.brella.zshk.common.STDOUT_FILENO
import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.opcodes.ZshkOpcode
import dev.brella.zshk.common.args.opcodes.exec

data class ZshkCommandSubstitutionArg(val op: ZshkOpcode) : ZshkValueArg<String> {
    override suspend fun getValue(env: ShellEnvironment): String {
        val io = BinaryPipeFlow()
        val subshell = env.fork { this[STDOUT_FILENO] = Pair(null, io) }
        env.exitCode = op.exec(subshell)
        return io.getData().decodeToString().trimEnd('\n')
    }

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        getValue(env).toIntOrNull() ?: 0

    override suspend fun test(env: ShellEnvironment): Boolean {
        val io = BinaryPipeFlow()
        val subshell = env.fork { this[STDOUT_FILENO] = Pair(null, io) }
        env.exitCode = op.exec(subshell)
        return env.exitCode == 0
    }

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "COMMAND_SUBSTITUTION(${op.toDebugString(env)})"
}