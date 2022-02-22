package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkStringLiteralArg(override val _value: String) : ZshkStaticValueArg<String>() {
    companion object {
        val EMPTY = ZshkStringLiteralArg("")
    }

    override suspend fun toIntValue(env: ShellEnvironment): Int = _value.toIntOrNull() ?: 0
    override suspend fun test(env: ShellEnvironment): Boolean = _value.isNotEmpty()
}