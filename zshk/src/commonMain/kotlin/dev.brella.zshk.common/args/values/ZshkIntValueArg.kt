package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArithmeticModifier

interface ZshkIntValueArg: ZshkNumericalValueArg<Int> {
    override suspend fun getValue(env: ShellEnvironment): Int
    override suspend fun toIntValue(env: ShellEnvironment): Int = getValue(env)
}

inline fun ZshkIntValueArg.withModifiers(modifiers: List<ZshkArithmeticModifier>): ZshkIntValueArg =
    if (modifiers.isEmpty()) this else ZshkIntegerModifiedArg(this, modifiers)