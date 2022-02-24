package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkVariableArg(
    val varName: String,
    val prefix: ZshkValueArgTransformer? = null,
    val postfix: ZshkValueArgTransformer? = null
) : ZshkValueArg<ZshkValueArg<*>> {
    override suspend fun getValue(env: ShellEnvironment): ZshkValueArg<*> {
        if (prefix == null && postfix == null)
            return env.variables[varName] ?: env.environmentalVariables[varName] ?: ZshkUndefinedArg

        var variable = env.variables[varName]
        if (variable != null) {
            if (prefix != null) {
                variable = prefix.eval(env, variable)
                env.variables[varName] = variable
            }

            if (postfix != null) env.variables[varName] = postfix.eval(env, variable)
            return variable
        }

        variable = env.environmentalVariables[varName]
        if (variable != null) {
            if (prefix != null) {
                variable = prefix.eval(env, variable)
                env.environmentalVariables[varName] = variable
            }

            if (postfix != null) env.environmentalVariables[varName] = postfix.eval(env, variable)
            return variable
        }

        return ZshkUndefinedArg
    }

    override suspend fun test(env: ShellEnvironment): Boolean =
        env.variables.containsKey(varName) || env.environmentalVariables.containsKey(varName)

    override suspend fun toStringValue(env: ShellEnvironment): String =
        getValue(env).toStringValue(env)

    override suspend fun toIntValue(env: ShellEnvironment): Int =
        getValue(env).toIntValue(env)

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "%$varName%"
}