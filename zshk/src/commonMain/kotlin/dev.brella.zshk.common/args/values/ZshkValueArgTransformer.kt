package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

//TODO: Make float aware
fun interface ZshkValueArgTransformer {
    class ZshkValueArgTransformerWithName(val name: String, val transformer: ZshkValueArgTransformer): ZshkValueArgTransformer by transformer {
        override fun toString(): String = name
    }

    companion object {
        val BITWISE_NOT = evalArgToBoolean("BITWISE_NOT") { env, arg -> arg.test(env) }
        val BITWISE_INV = simpleArithmeticModifier("BITWISE_INV", Int::inv)
        val UNARY_PLUS = simpleArithmeticModifier("UNARY_PLUS", Int::unaryPlus)
        val UNARY_MINUS = simpleArithmeticModifier("UNARY_MINUS", Int::unaryMinus)
        val INCREMENT = simpleArithmeticModifier("INCREMENT", Int::inc)
        val DECREMENT = simpleArithmeticModifier("DECREMENT", Int::dec)

        inline fun evalArgToInt(crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Int): ZshkValueArgTransformer =
            ZshkValueArgTransformer { env, arg -> ZshkIntegerLiteralArg(func(env, arg)) }

        inline fun evalArgToBoolean(crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Boolean): ZshkValueArgTransformer =
            ZshkValueArgTransformer { env, arg -> ZshkIntegerLiteralArg(if (func(env, arg)) 1 else 0) }

        inline fun simpleArithmeticModifier(crossinline func: suspend (arg: Int) -> Int): ZshkValueArgTransformer =
            evalArgToInt { env, arg -> func(arg.toIntValue(env)) }

        inline fun comparisonArithmeticModifier(crossinline func: suspend (arg: Int) -> Boolean): ZshkValueArgTransformer =
            evalArgToBoolean { env, arg -> func(arg.toIntValue(env)) }

        /** Named */

        inline fun evalArgToInt(name: String, crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Int): ZshkValueArgTransformer =
            withName(name) { env, arg -> ZshkIntegerLiteralArg(func(env, arg)) }

        inline fun evalArgToBoolean(name: String, crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Boolean): ZshkValueArgTransformer =
            withName(name) { env, arg -> ZshkIntegerLiteralArg(if (func(env, arg)) 1 else 0) }

        inline fun simpleArithmeticModifier(name: String, crossinline func: suspend (arg: Int) -> Int): ZshkValueArgTransformer =
            evalArgToInt(name) { env, arg -> func(arg.toIntValue(env)) }

        inline fun comparisonArithmeticModifier(name: String, crossinline func: suspend (arg: Int) -> Boolean): ZshkValueArgTransformer =
            evalArgToBoolean(name) { env, arg -> func(arg.toIntValue(env)) }

        inline fun withName(name: String, func: ZshkValueArgTransformer): ZshkValueArgTransformer =
            ZshkValueArgTransformerWithName(name, func)
    }

    suspend fun eval(
        env: ShellEnvironment,
        arg: ZshkValueArg<*>
    ): ZshkValueArg<*>
}



