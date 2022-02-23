package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

interface ZshkNumericalValueArg<out T>: ZshkValueArg<T> {
    override suspend fun toIntValue(env: ShellEnvironment): Int
    override suspend fun toStringValue(env: ShellEnvironment): String =
        toIntValue(env).toString()

    override suspend fun test(env: ShellEnvironment): Boolean =
        toIntValue(env) != 0
}