package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkFloatLiteralArg(override val _value: Float) : ZshkStaticValueArg<Float>() {
    override suspend fun test(env: ShellEnvironment): Boolean =
        _value != 0f

    override suspend fun toIntValue(env: ShellEnvironment): Int = _value.toInt()
}