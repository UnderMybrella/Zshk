package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

object ZshkUndefinedArg : ZshkStaticValueArg<Any?>() {
    override val _value: Any? = null
    override suspend fun toStringValue(env: ShellEnvironment): String = ""
    override suspend fun toIntValue(env: ShellEnvironment): Int = 0
    override suspend fun test(env: ShellEnvironment): Boolean = false

    override suspend fun toDebugString(env: ShellEnvironment): String = "%undefined%"
}