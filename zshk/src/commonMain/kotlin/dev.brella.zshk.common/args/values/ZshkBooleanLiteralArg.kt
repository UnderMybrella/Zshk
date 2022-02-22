package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkBooleanLiteralArg(override val _value: Boolean) : ZshkStaticValueArg<Boolean>() {
    override suspend fun test(env: ShellEnvironment): Boolean = _value
    override suspend fun toIntValue(env: ShellEnvironment): Int = if (_value) 0 else 1
}