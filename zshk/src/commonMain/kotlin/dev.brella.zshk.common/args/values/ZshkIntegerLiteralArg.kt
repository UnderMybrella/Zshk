package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkIntegerLiteralArg(override val _value: Int) : ZshkStaticValueArg<Int>() {
    override suspend fun test(env: ShellEnvironment): Boolean =
        _value != 0

    override suspend fun toIntValue(env: ShellEnvironment): Int = _value
}