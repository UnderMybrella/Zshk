package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

abstract class ZshkStaticValueArg<T> : ZshkValueArg<T> {
    abstract val _value: T

    override suspend fun getValue(env: ShellEnvironment): T = _value
}