package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkVariableArg(val varName: String) : ZshkValueArg<ZshkValueArg<*>> {
    override suspend fun getValue(env: ShellEnvironment): ZshkValueArg<*> =
        env.variables[varName] ?: env.environmentalVariables[varName] ?: ZshkUndefinedArg

    override suspend fun test(env: ShellEnvironment): Boolean =
        env.variables.containsKey(varName) || env.environmentalVariables.containsKey(varName)

    override suspend fun toStringValue(env: ShellEnvironment): String =
        getValue(env).toStringValue(env)

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        getValue(env).toIntValue(env)

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "%$varName=${toStringValue(env)}%"
}