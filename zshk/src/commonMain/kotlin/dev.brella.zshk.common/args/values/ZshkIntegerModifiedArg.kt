package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArithmeticModifier

data class ZshkIntegerModifiedArg(val base: ZshkValueArg<*>, val modifiers: List<ZshkArithmeticModifier>): ZshkIntValueArg {
    override suspend fun getValue(env: ShellEnvironment): Int =
        modifiers.fold(base) { arg, mod -> mod.eval(env, arg) }
            .toIntValue(env)
}