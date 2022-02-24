package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.values.ZshkIntegerLiteralArg
import dev.brella.zshk.common.args.values.ZshkValueArg
import kotlin.math.pow

//TODO: Make float aware
fun interface ZshkArithmeticOperator {
    data class ZshkArithmeticOperatorWithName(val name: String, val operator: ZshkArithmeticOperator): ZshkArithmeticOperator by operator {
        override fun toString(): String = name
    }

    companion object {
        val BITWISE_SHIFT_LEFT = simpleArithmeticOperator("BITWISE_SHIFT_LEFT", Int::shl)
        val BITWISE_SHIFT_RIGHT = simpleArithmeticOperator("BITWISE_SHIFT_RIGHT", Int::shr)
        val BITWISE_AND = simpleArithmeticOperator("BITWISE_AND", Int::and)
        val BITWISE_XOR = simpleArithmeticOperator("BITWISE_XOR", Int::xor)
        val BITWISE_OR = simpleArithmeticOperator("BITWISE_OR", Int::or)
        val EXPONENTIATION = simpleArithmeticOperator("EXPONENTIATION") { lhs, rhs -> lhs.toDouble().pow(rhs).toInt() }
        val MULTIPLICATION = simpleArithmeticOperator("MULTIPLICATION", Int::times)
        val DIVISION = simpleArithmeticOperator("DIVISION", Int::div)
        val MODULUS = simpleArithmeticOperator("MODULUS", Int::mod)
        val REMAINDER = simpleArithmeticOperator("REMAINDER", Int::rem)
        val ADDITION = simpleArithmeticOperator("ADDITION", Int::plus)
        val SUBTRACTION = simpleArithmeticOperator("SUBTRACTION", Int::minus)
        val LESS_THAN = comparisonArithmeticOperator("LESS_THAN") { lhs, rhs -> lhs < rhs }
        val GREATER_THAN = comparisonArithmeticOperator("GREATER_THAN") { lhs, rhs -> lhs > rhs }
        val LESS_THAN_EQUAL_TO = comparisonArithmeticOperator("LESS_THAN_EQUAL_TO") { lhs, rhs -> lhs <= rhs }
        val GREATER_THAN_EQUAL_TO = comparisonArithmeticOperator("GREATER_THAN_EQUAL_TO") { lhs, rhs -> lhs >= rhs }
        val EQUALITY = comparisonArithmeticOperator("EQUALITY") { lhs, rhs -> lhs == rhs }
        val INEQUALITY = comparisonArithmeticOperator("INEQUALITY") { lhs, rhs -> lhs != rhs }

        val LOGICAL_AND =
            evalArgsToBoolean("LOGICAL_AND") { env, lhs, rhs -> lhs.test(env) && rhs.test(env) }

        val LOGICAL_OR =
            evalArgsToBoolean("LOGICAL_OR") { env, lhs, rhs -> lhs.test(env) || rhs.test(env) }

        val LOGICAL_XOR =
            evalArgsToBoolean("LOGICAL_XOR") { env, lhs, rhs -> lhs.test(env) xor rhs.test(env) }

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

        /** Named */

        inline fun evalArgsToInt(name: String, crossinline func: suspend (env: ShellEnvironment, lhs: ZshkValueArg<*>, rhs: ZshkValueArg<*>) -> Int): ZshkArithmeticOperator =
            withName(name) { env, lhs, rhs -> ZshkIntegerLiteralArg(func(env, lhs, rhs)) }

        inline fun evalArgsToBoolean(name: String, crossinline func: suspend (env: ShellEnvironment, lhs: ZshkValueArg<*>, rhs: ZshkValueArg<*>) -> Boolean): ZshkArithmeticOperator =
            withName(name) { env, lhs, rhs -> ZshkIntegerLiteralArg(if (func(env, lhs, rhs)) 1 else 0) }

        inline fun simpleArithmeticOperator(name: String, crossinline func: suspend (lhs: Int, rhs: Int) -> Int): ZshkArithmeticOperator =
            evalArgsToInt(name) { env, lhs, rhs -> func(lhs.toIntValue(env), rhs.toIntValue(env)) }

        inline fun comparisonArithmeticOperator(name: String, crossinline func: suspend (lhs: Int, rhs: Int) -> Boolean): ZshkArithmeticOperator =
            evalArgsToBoolean(name) { env, lhs, rhs -> func(lhs.toIntValue(env), rhs.toIntValue(env)) }

        inline fun withName(name: String, operator: ZshkArithmeticOperator): ZshkArithmeticOperator =
            ZshkArithmeticOperatorWithName(name, operator)
    }

    suspend fun eval(
        env: ShellEnvironment,
        lhs: ZshkValueArg<*>,
        rhs: ZshkValueArg<*>
    ): ZshkValueArg<*>
}



