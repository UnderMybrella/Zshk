package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

interface ZshkIntValueArg: ZshkNumericalValueArg<Int> {
    override suspend fun getValue(env: ShellEnvironment): Int
    override suspend fun toIntValue(env: ShellEnvironment): Int = getValue(env)
}