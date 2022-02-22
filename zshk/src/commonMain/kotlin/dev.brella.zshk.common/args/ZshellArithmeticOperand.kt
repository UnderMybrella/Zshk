package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.values.ZshkIntegerLiteralArg
import dev.brella.zshk.common.args.values.ZshkValueArg
import kotlin.math.pow

fun interface ZshkArithmeticOperator {
    companion object {
        val BITWISE_SHIFT_LEFT = simpleArithmeticOperator(Int::shl)
        val BITWISE_SHIFT_RIGHT = simpleArithmeticOperator(Int::shr)
        val BITWISE_AND = simpleArithmeticOperator(Int::and)
        val BITWISE_XOR = simpleArithmeticOperator(Int::xor)
        val BITWISE_OR = simpleArithmeticOperator(Int::or)
        val EXPONENTIATION = simpleArithmeticOperator { lhs, rhs -> lhs.toDouble().pow(rhs).toInt() }
        val MULTIPLICATION = simpleArithmeticOperator(Int::times)
        val DIVISION = simpleArithmeticOperator(Int::div)
        val MODULUS = simpleArithmeticOperator(Int::mod)
        val REMAINDER = simpleArithmeticOperator(Int::rem)
        val ADDITION = simpleArithmeticOperator(Int::plus)
        val SUBTRACTION = simpleArithmeticOperator(Int::minus)
        val LESS_THAN = comparisonArithmeticOperator { lhs, rhs -> lhs < rhs }
        val GREATER_THAN = comparisonArithmeticOperator { lhs, rhs -> lhs > rhs }
        val LESS_THAN_EQUAL_TO = comparisonArithmeticOperator { lhs, rhs -> lhs <= rhs }
        val GREATER_THAN_EQUAL_TO = comparisonArithmeticOperator { lhs, rhs -> lhs >= rhs }
        val EQUALITY = comparisonArithmeticOperator { lhs, rhs -> lhs == rhs }
        val INEQUALITY = comparisonArithmeticOperator { lhs, rhs -> lhs != rhs }

        val LOGICAL_AND =
            evalArgsToBoolean { env, lhs, rhs -> lhs.test(env) && rhs.test(env) }

        val LOGICAL_OR =
            evalArgsToBoolean { env, lhs, rhs -> lhs.test(env) || rhs.test(env) }

        val LOGICAL_XOR =
            evalArgsToBoolean { env, lhs, rhs -> lhs.test(env) xor rhs.test(env) }


        val ZSH_OPERATOR_PRECEDENCE by lazy {
            listOf(
                BITWISE_SHIFT_LEFT, BITWISE_SHIFT_RIGHT,
                BITWISE_AND, BITWISE_XOR, BITWISE_OR,
                EXPONENTIATION,
                MULTIPLICATION, DIVISION, REMAINDER,
                ADDITION, SUBTRACTION,
                LESS_THAN, GREATER_THAN, LESS_THAN_EQUAL_TO, GREATER_THAN_EQUAL_TO,
                EQUALITY, INEQUALITY,
                LOGICAL_AND, LOGICAL_OR, LOGICAL_XOR
            )
        }

        inline fun evalArgsToInt(crossinline func: suspend (env: ShellEnvironment, lhs: ZshkValueArg<*>, rhs: ZshkValueArg<*>) -> Int): ZshkArithmeticOperator =
            ZshkArithmeticOperator { env, lhs, rhs -> ZshkIntegerLiteralArg(func(env, lhs, rhs)) }

        inline fun evalArgsToBoolean(crossinline func: suspend (env: ShellEnvironment, lhs: ZshkValueArg<*>, rhs: ZshkValueArg<*>) -> Boolean): ZshkArithmeticOperator =
            ZshkArithmeticOperator { env, lhs, rhs -> ZshkIntegerLiteralArg(if (func(env, lhs, rhs)) 1 else 0) }

        inline fun simpleArithmeticOperator(crossinline func: suspend (lhs: Int, rhs: Int) -> Int): ZshkArithmeticOperator =
            evalArgsToInt { env, lhs, rhs -> func(lhs.toIntValue(env), rhs.toIntValue(env)) }

        inline fun comparisonArithmeticOperator(crossinline func: suspend (lhs: Int, rhs: Int) -> Boolean): ZshkArithmeticOperator =
            evalArgsToBoolean { env, lhs, rhs -> func(lhs.toIntValue(env), rhs.toIntValue(env)) }
    }

    suspend fun eval(
        env: ShellEnvironment,
        lhs: ZshkValueArg<*>,
        rhs: ZshkValueArg<*>
    ): ZshkValueArg<*>
}



