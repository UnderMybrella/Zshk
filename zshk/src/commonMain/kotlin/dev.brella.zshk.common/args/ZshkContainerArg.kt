package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment

data class ZshkContainerArg<T>(val inner: T?): ZshkArg {
    override suspend fun test(env: ShellEnvironment): Boolean = false
    override suspend fun toDebugString(env: ShellEnvironment): String = inner.toString()
}