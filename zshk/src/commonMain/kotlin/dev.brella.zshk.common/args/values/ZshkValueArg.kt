package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg

interface ZshkValueArg<out T> : ZshkArg {
    suspend fun getValue(env: ShellEnvironment): T

    override suspend fun toStringValue(env: ShellEnvironment): String =
        getValue(env).toString()

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        toStringValue(env).toIntOrNull() ?: 0

    override suspend fun test(env: ShellEnvironment): Boolean =
        toStringValue(env).isNotEmpty()
}