package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg
import dev.brella.zshk.common.joinToStringSuspend

data class ZshkQuotedStringArg(val components: List<ZshkArg>) : ZshkValueArg<String> {
    override suspend fun getValue(env: ShellEnvironment): String =
        components.joinToStringSuspend("") { it.toStringValue(env) }

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        getValue(env).toIntOrNull() ?: 0

    override suspend fun test(env: ShellEnvironment): Boolean =
        components.isNotEmpty()

    override suspend fun toDebugString(env: ShellEnvironment): String =
        components.joinToStringSuspend("") { it.toDebugString(env) }
}