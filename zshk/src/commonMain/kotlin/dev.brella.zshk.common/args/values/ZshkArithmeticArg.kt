package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.args.ZshkArithmeticOperator
import dev.brella.zshk.common.ShellEnvironment

data class ZshkArithmeticArg(val lhs: ZshkValueArg<*>, val operator: ZshkArithmeticOperator, val rhs: ZshkValueArg<*>): ZshkValueArg<ZshkValueArg<*>> {
    override suspend fun getValue(env: ShellEnvironment): ZshkValueArg<*> =
        operator.eval(env, lhs, rhs)

    override suspend fun toStringValue(env: ShellEnvironment): String =
        getValue(env).toStringValue(env)

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        getValue(env).toIntValue(env)

    override suspend fun test(env: ShellEnvironment): Boolean =
        toIntValue(env) != 0
}