package dev.brella.zshk.common.args

import dev.brella.zshk.common.*

interface ZshkArg {
    suspend fun toStringValue(env: ShellEnvironment): String = toString()
    suspend fun toIntValue(env: ShellEnvironment): Int = toStringValue(env).toIntOrNull() ?: 0

    suspend fun test(env: ShellEnvironment): Boolean

    suspend fun toDebugString(env: ShellEnvironment): String = toStringValue(env)
}
