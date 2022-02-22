package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

object ZshkExitCodeVariableArg : ZshkValueArg<Int> {
    override suspend fun getValue(env: ShellEnvironment): Int =
        env.exitCode

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        env.exitCode

    override suspend fun test(env: ShellEnvironment): Boolean =
        env.exitCode != 0

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "%exitCode=${env.exitCode}%"
}