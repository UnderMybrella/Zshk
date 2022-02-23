package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.values.ZshkIntegerLiteralArg
import dev.brella.zshk.common.args.values.ZshkValueArg
import kotlin.math.pow

//TODO: Make float aware
fun interface ZshkArithmeticModifier {
    companion object {
        val BITWISE_NOT = evalArgToBoolean { env, arg -> arg.test(env) }
        val BITWISE_INV = simpleArithmeticModifier(Int::inv)
        val UNARY_PLUS  = simpleArithmeticModifier(Int::unaryPlus)
        val UNARY_MINUS = simpleArithmeticModifier(Int::unaryMinus)

        inline fun evalArgToInt(crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Int): ZshkArithmeticModifier =
            ZshkArithmeticModifier { env, arg -> ZshkIntegerLiteralArg(func(env, arg)) }

        inline fun evalArgToBoolean(crossinline func: suspend (env: ShellEnvironment, arg: ZshkValueArg<*>) -> Boolean): ZshkArithmeticModifier =
            ZshkArithmeticModifier { env, arg -> ZshkIntegerLiteralArg(if (func(env, arg)) 1 else 0) }

        inline fun simpleArithmeticModifier(crossinline func: suspend (arg: Int) -> Int): ZshkArithmeticModifier =
            evalArgToInt { env, arg -> func(arg.toIntValue(env)) }

        inline fun comparisonArithmeticModifier(crossinline func: suspend (arg: Int) -> Boolean): ZshkArithmeticModifier =
            evalArgToBoolean { env, arg -> func(arg.toIntValue(env)) }
    }

    suspend fun eval(
        env: ShellEnvironment,
        arg: ZshkValueArg<*>
    ): ZshkValueArg<*>
}



