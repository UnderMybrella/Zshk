package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArithmeticModifier

data class ZshkFloatLiteralArg(override val _value: Float) : ZshkStaticValueArg<Float>(), ZshkNumericalValueArg<Float> {
    override suspend fun test(env: ShellEnvironment): Boolean =
        _value != 0f

    override suspend fun toIntValue(env: ShellEnvironment): Int = _value.toInt()
}

inline fun ZshkFloatLiteralArg.withModifiers(modifiers: List<ZshkArithmeticModifier>): ZshkValueArg<*> =
    if (modifiers.isEmpty()) this else ZshkIntegerModifiedArg(this, modifiers)