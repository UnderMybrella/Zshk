package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkIntegerModifiedArg(val base: ZshkValueArg<*>, val modifiers: List<ZshkValueArgTransformer>): ZshkIntValueArg {
    override suspend fun getValue(env: ShellEnvironment): Int =
        modifiers.fold(base) { arg, mod -> mod.eval(env, arg) }
            .toIntValue(env)

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "%${base.toDebugString(env)} ~ ${modifiers.joinToString()}%"
}