package dev.brella.zshk

import dev.brella.antlr.zshk.zshLexer
import dev.brella.antlr.zshk.zshParser
import dev.brella.antlr.zshk.zshParser.PipeStdoutAndStderrToStdinContext
import dev.brella.antlr.zshk.zshParser.PipeStdoutToStdinContext
import dev.brella.antlr.zshk.zshParserBaseVisitor
import dev.brella.zshk.common.args.*
import dev.brella.zshk.common.args.opcodes.*
import dev.brella.zshk.common.args.values.*
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.tree.TerminalNode

class ZshkVisitor : zshParserBaseVisitor<ZshkArg>() {
    fun visitPipe(ctx: zshParser.PipeContext): ZshkPipeArg =
        when (ctx) {
            is PipeStdoutToStdinContext -> ZshkPipeArg.StdoutToStdin
            is PipeStdoutAndStderrToStdinContext -> ZshkPipeArg.StdoutAndStderrToStdin
            else -> throw IllegalStateException("Illegal pipe ctx ${ctx.text} (${ctx::class})")
        }

    override fun visitPipeline(ctx: zshParser.PipelineContext): ZshkPipelineOpcode {
        val isCoproc = ctx.COPROC() != null

        val allCommands = ctx.simpleOrComplexCommand()
            .map(this::visitSimpleOrComplexCommand)
            .filterIsInstance<ZshkOpcode>()

        val pipes = ctx.pipe()
            .map(this::visitPipe)

        return when (allCommands.size) {
            1 -> ZshkPipelineOpcode(isCoproc, allCommands[0])
            2 ->
                ZshkPipelineOpcode(
                    isCoproc,
                    ZshkPipeOpcode(allCommands[0], pipes[0], allCommands[1])
                )
            else ->
                ZshkPipelineOpcode(
                    isCoproc,
                    allCommands
                        .drop(1)
                        .foldIndexed(allCommands.first()) { index, left, right ->
                            ZshkPipeOpcode(left, pipes[index], right)
                        }
                )
        }
    }

    override fun visitList(ctx: zshParser.ListContext): ZshkScriptOpcode =
        ZshkScriptOpcode(ctx.sublist().map(this::visitSublist))

    override fun visitListWithOptionalTerminator(ctx: zshParser.ListWithOptionalTerminatorContext): ZshkScriptOpcode =
        ZshkScriptOpcode(ctx.sublist().map(this::visitSublist))

    override fun visitSublist(ctx: zshParser.SublistContext): ZshkScriptOpcode {
        val opcodes: MutableList<ZshkOpcode> = ArrayList()

        val allPipelines = ctx.pipeline()
            .map(this::visitPipeline)

        val allJoins = ctx.sublistJoiner()

        opcodes.add(allPipelines[0])

        for (i in allJoins.indices) {
            val join = allJoins[i]
            if (join.AND() != null) opcodes.add(ZshkANDOpcode(allPipelines[i + 1]))
            else if (join.OR() != null) opcodes.add(ZshkOROpcode(allPipelines[i + 1]))
        }

        return ZshkScriptOpcode(opcodes)
    }

    override fun visitSimpleCommand(ctx: zshParser.SimpleCommandContext): ZshkCommandOpcode {
        val modifier = ctx.commandModifier()?.let(this::visitCommandModifier)
        val args = ctx.literal().mapNotNull(this::visitLiteral)

        return ZshkCommandOpcode(modifier, args)
    }

    override fun visitCommandModifier(ctx: zshParser.CommandModifierContext): ZshkCommandModifierArg {
        if (ctx.SUB() == null) return ZshkCommandModifierArg.Minus
        if (ctx.BUILTIN() == null) return ZshkCommandModifierArg.Builtin
        if (ctx.COMMAND() != null) {
            var searchDefaultPath = false
            var whence = false
            var whenceVerbose = false

            ctx.shortFlagGroup().forEach { shortFlag ->
                val flagGroup = shortFlag.IDENTIFIER().text
                if (flagGroup.contains('p')) searchDefaultPath = true
                if (flagGroup.contains('v')) whence = true
                if (flagGroup.contains('V')) whenceVerbose = true
            }

            return ZshkCommandModifierArg.Command(searchDefaultPath, whence, whenceVerbose)
        }
        if (ctx.EXEC() != null) {
            var clear = false
            var minus = false
            var argv0: ZshkArg? = null

            ctx.shortFlagGroupWithOptionalValue().forEach { shortFlag ->
                val flagGroup = shortFlag.shortFlagGroup().IDENTIFIER().text

                if (flagGroup.contains('c')) clear = true
                if (flagGroup.contains('l')) minus = true
                if (flagGroup.contains('a')) argv0 = visitLiteral(shortFlag.value)
            }

            return ZshkCommandModifierArg.Exec(clear, minus, argv0)
        }
        if (ctx.NOCORRECT() != null) return ZshkCommandModifierArg.NoCorrect
        if (ctx.NOGLOB() != null) return ZshkCommandModifierArg.NoGlob

        throw IllegalStateException("Unknown command modifier in ${ctx.text}")
    }

    override fun visitLiteral(ctx: zshParser.LiteralContext?): ZshkValueArg<*> {
        ctx?.identifier()?.let { return ZshkStringLiteralArg(it.text) }
        ctx?.quotedString()?.let(this::visitQuotedString)?.let { return it }
        ctx?.integerLiteral()?.let(this::visitIntegerLiteral)?.let { return it }
        ctx?.floatLiteral()?.let(this::visitFloatLiteral)?.let { return it }
        ctx?.variableReference()?.let(this::visitVariableReference)?.let { return it }
        ctx?.BOOL_LITERAL()?.let { return ZshkBooleanLiteralArg(it.text.toBoolean()) }
        ctx?.NULL_LITERAL()?.let { return ZshkNullLiteralArg }
        ctx?.arithmeticExpression()?.let(this::visitArithmeticExpression)?.let { return it as ZshkValueArg<*> }

        throw IllegalStateException("Unknown literal in ${ctx?.text}")
    }

    override fun visitIntegerLiteral(ctx: zshParser.IntegerLiteralContext): ZshkIntegerLiteralArg {
        ctx.DECIMAL_LITERAL()
            ?.text
            ?.removePrefix("0d")
            ?.removePrefix("0D")
            ?.toIntOrNull()
            ?.let { return ZshkIntegerLiteralArg(it) }

        ctx.HEX_LITERAL()
            ?.text
            ?.removePrefix("0x")
            ?.removePrefix("0X")
            ?.toIntOrNull(16)
            ?.let { return ZshkIntegerLiteralArg(it) }

        ctx.OCT_LITERAL()
            ?.text
            ?.removePrefix("0o")
            ?.removePrefix("0O")
            ?.toIntOrNull(8)
            ?.let { return ZshkIntegerLiteralArg(it) }

        ctx.HASH_LITERAL()
            ?.text
            ?.let { hashLiteral ->
                val base = hashLiteral.substringBefore('#')
                val num = hashLiteral.substringAfter('#')

                num.toIntOrNull(base.toIntOrNull() ?: 10)
                    ?.let { return ZshkIntegerLiteralArg(it) }
            }

        ctx.BRACKET_LITERAL()
            ?.text
            ?.let { hashLiteral ->
                val base = hashLiteral.substringAfter('[')
                    .substringBefore(']')

                val num = hashLiteral.substringAfter(']')

                num.toIntOrNull(base.toIntOrNull() ?: 10)
                    ?.let { return ZshkIntegerLiteralArg(it) }
            }

        ctx.BINARY_LITERAL()
            ?.text
            ?.removePrefix("0b")
            ?.removePrefix("0B")
            ?.toIntOrNull(2)
            ?.let { return ZshkIntegerLiteralArg(it) }

        throw IllegalStateException("Unknown integer literal in ${ctx.text}")
    }

    override fun visitFloatLiteral(ctx: zshParser.FloatLiteralContext): ZshkFloatLiteralArg {
        ctx.FLOAT_LITERAL()
            ?.text
            ?.toFloatOrNull()
            ?.let { return ZshkFloatLiteralArg(it) }

        //TODO get this working maybe??
//        ctx.HEX_FLOAT_LITERAL()
//            ?.text
//            ?.removePrefix("0x")
//            ?.removePrefix("0X")
//            ?.toFloatOrNull()
//            ?.let { return zshOf(ZshkFloatLiteralArg(it)) }

        throw IllegalStateException("Unknown float literal in ${ctx.text}")
    }

    override fun visitVariableReference(ctx: zshParser.VariableReferenceContext): ZshkValueArg<*> {
        ctx.VARIABLE_REFERENCE()?.let { return ZshkVariableArg(it.text.substring(1)) }
        ctx.EXIT_CODE_VAR_REF()?.let { return ZshkExitCodeVariableArg }
        
        throw IllegalStateException("Unknown var ref in ${ctx.text}")
    }

    override fun visitQuotedString(ctx: zshParser.QuotedStringContext): ZshkQuotedStringArg {
        val components: MutableList<ZshkArg> = ArrayList()
        
        ctx.children.forEach { node ->
            when (node) {
                is TerminalNode -> {
                    when (val token = node.symbol) {
                        is CommonToken -> {
                            val text = token.text

                            when (token.type) {
                                zshLexer.ESCAPES -> {
                                    when (val escaped = text.getOrNull(1)) {
                                        'u' -> text.substring(2, 6)
                                            .toIntOrNull(16)
                                            ?.toChar()
                                            ?.let { components.add(ZshkCharLiteralArg(it)) }

                                        'x' -> text.substring(2, 4)
                                            .toIntOrNull(16)
                                            ?.toChar()
                                            ?.let { components.add(ZshkCharLiteralArg(it)) }

                                        'b' -> components.add(ZshkCharLiteralArg('\b'))
                                        'f' -> components.add(ZshkCharLiteralArg('\u000C'))
                                        'n' -> components.add(ZshkCharLiteralArg('\n'))
                                        'r' -> components.add(ZshkCharLiteralArg('\r'))
                                        't' -> components.add(ZshkCharLiteralArg('\t'))
                                        null -> {}
                                        else -> components.add(ZshkCharLiteralArg(escaped))
                                    }
                                }
                                zshLexer.STRING_CHARACTERS -> components.add(ZshkStringLiteralArg(text))
                                else -> {}
                            }
                        }
                        else -> {}
                    }
                }
                is zshParser.VariableReferenceContext -> visitVariableReference(node)?.let(components::add)
            }
        }

        return ZshkQuotedStringArg(components)
    }

    override fun visitComplexCommands(ctx: zshParser.ComplexCommandsContext?): ZshkArg {
        return super.visitComplexCommands(ctx)
    }

    override fun visitIfThenFi(ctx: zshParser.IfThenFiContext): ZshkIfCheckOpcode {
        val conditionals = ctx.conditional()
            .map(this::visitConditional)

        val lists = ctx.list()
            .map(this::visitList)

        val ifThens: MutableList<Pair<ZshkArg, ZshkOpcode>> = ArrayList()
        var elseThen: ZshkOpcode? = null

        for (i in conditionals.indices) {
            val check =
                conditionals[i] ?: throw IllegalStateException("Invalid conditional @ ${ctx.conditional(i).text}")
            val list = lists[i]
            ifThens.add(check to list)
        }
        if (lists.size > conditionals.size) elseThen = lists.last()

        return ZshkIfCheckOpcode(ifThens, elseThen)
    }

    override fun visitIfElifElse(ctx: zshParser.IfElifElseContext): ZshkIfCheckOpcode {
        val conditionals = ctx.conditional()
            .map(this::visitConditional)

        val lists = ctx.list()
            .map(this::visitList)

        val ifThens: MutableList<Pair<ZshkArg, ZshkOpcode>> = ArrayList()
        var elseThen: ZshkOpcode? = null

        for (i in conditionals.indices) {
            val check =
                conditionals[i] ?: throw IllegalStateException("Invalid conditional @ ${ctx.conditional(i).text}")
            val list = lists[i]
            ifThens.add(check to list)
        }
        if (lists.size > conditionals.size) elseThen = lists.last()

        return ZshkIfCheckOpcode(ifThens, elseThen)
    }

    override fun visitIfSingular(ctx: zshParser.IfSingularContext): ZshkIfCheckSingularOpcode =
        ZshkIfCheckSingularOpcode(visitConditional(ctx.conditional()), visitSublist(ctx.sublist()))

    override fun visitForInDoDone(ctx: zshParser.ForInDoDoneContext): ZshkForInWordsOpcode =
        ZshkForInWordsOpcode(
            ctx.identifier().map { it.text },
            ctx.literal().map { visitLiteral(it) ?: ZshkStringLiteralArg.EMPTY },
            visitList(ctx.list())
        )

    override fun visitForLiteralsDoDone(ctx: zshParser.ForLiteralsDoDoneContext?): ZshkArg {
        return super.visitForLiteralsDoDone(ctx)
    }

    override fun visitWhileDoDone(ctx: zshParser.WhileDoDoneContext?): ZshkArg {
        return super.visitWhileDoDone(ctx)
    }

    override fun visitUntilDoDone(ctx: zshParser.UntilDoDoneContext?): ZshkArg {
        return super.visitUntilDoDone(ctx)
    }

    inline fun visitArithmeticStatement(ctx: zshParser.ArithmeticStatementContext): ZshkValueArg<*> =
        ctx.accept(this) as ZshkValueArg<*>

    override fun visitArithmeticExpression(ctx: zshParser.ArithmeticExpressionContext): ZshkArg {
        return visitArithmeticStatement(ctx.arithmeticStatement(0))
    }

    override fun visitGroupedArithmeticOperation(ctx: zshParser.GroupedArithmeticOperationContext?): ZshkArg {
        return super.visitGroupedArithmeticOperation(ctx)
    }

    override fun visitTernaryArithmeticExpression(ctx: zshParser.TernaryArithmeticExpressionContext?): ZshkArg {
        return super.visitTernaryArithmeticExpression(ctx)
    }

    override fun visitArithmeticAssignment(ctx: zshParser.ArithmeticAssignmentContext?): ZshkArg {
        return super.visitArithmeticAssignment(ctx)
    }

    override fun visitArithmeticOperation(ctx: zshParser.ArithmeticOperationContext): ZshkArg {
        val values = ctx.arithmeticStatement()
            .mapTo(ArrayList(), this::visitArithmeticStatement)

        val operators = mutableListOf(visitArithmeticOperator(ctx.arithmeticOperator()).inner)

        // Flatten out for PEDMAS
        var i = values.indexOfFirst { it is ZshkArithmeticArg }
        while (i != -1) {
            val op = values.removeAt(i) as ZshkArithmeticArg
            values.add(i, op.lhs)
            operators.add(i, op.operator)
            values.add(i + 1, op.rhs)
            i = values.indexOfFirst { it is ZshkArithmeticArg }
        }

        ZshkArithmeticOperator.ZSH_OPERATOR_PRECEDENCE.forEach { operator ->
            var operatorIndex = operators.indexOf(operator)
            while (operatorIndex != -1) {
                values[operatorIndex] =
                    ZshkArithmeticArg(values[operatorIndex], operator, values.removeAt(operatorIndex + 1))
                operators.removeAt(operatorIndex)
                operatorIndex = operators.indexOf(operator)
            }
        }

        if (values.size != 1) throw IllegalStateException("Somehow wasn't able to squash ${values}")

        return values[0]
    }

    override fun visitArithmeticNumericalLiteral(ctx: zshParser.ArithmeticNumericalLiteralContext): ZshkValueArg<*> {
        val modifiers = ctx.arithmeticModifier()
            .mapNotNull { this.visitArithmeticModifier(it).inner }

        ctx.integerLiteral()?.let(this::visitIntegerLiteral)?.let { return it.withModifiers(modifiers) }
        ctx.floatLiteral()?.let(this::visitFloatLiteral)?.let { return it.withModifiers(modifiers) }

        throw IllegalStateException("Unknown arithmetic value @ $ctx")
    }

    override fun visitArithmeticVariableReference(ctx: zshParser.ArithmeticVariableReferenceContext): ZshkValueArg<*> {
        ctx.identifier()?.let { return ZshkVariableArg(it.text) }

        throw IllegalStateException("Unknown arithmetic value @ $ctx")
    }

    override fun visitArithmeticModifier(ctx: zshParser.ArithmeticModifierContext): ZshkContainerArg<ZshkArithmeticModifier> =
        ZshkContainerArg(
            when (ctx.text) {
                "!" -> ZshkArithmeticModifier.BITWISE_NOT
                "~" -> ZshkArithmeticModifier.BITWISE_INV
                "+" -> ZshkArithmeticModifier.UNARY_PLUS
                "-" -> ZshkArithmeticModifier.UNARY_MINUS
                else -> null
            }
        )

    override fun visitArithmeticOperator(ctx: zshParser.ArithmeticOperatorContext): ZshkContainerArg<ZshkArithmeticOperator> =
        ZshkContainerArg(
            when (ctx.text) {
                "<<" -> ZshkArithmeticOperator.BITWISE_SHIFT_LEFT
                ">>" -> ZshkArithmeticOperator.BITWISE_SHIFT_RIGHT
                "&" -> ZshkArithmeticOperator.BITWISE_AND
                "^" -> ZshkArithmeticOperator.BITWISE_XOR
                "|" -> ZshkArithmeticOperator.BITWISE_OR
                "**" -> ZshkArithmeticOperator.EXPONENTIATION
                "*" -> ZshkArithmeticOperator.MULTIPLICATION
                "/" -> ZshkArithmeticOperator.DIVISION
                "%" -> ZshkArithmeticOperator.REMAINDER
                "+" -> ZshkArithmeticOperator.ADDITION
                "-" -> ZshkArithmeticOperator.SUBTRACTION
                "<" -> ZshkArithmeticOperator.LESS_THAN
                ">" -> ZshkArithmeticOperator.GREATER_THAN
                "<=" -> ZshkArithmeticOperator.LESS_THAN_EQUAL_TO
                ">=" -> ZshkArithmeticOperator.GREATER_THAN_EQUAL_TO
                "==" -> ZshkArithmeticOperator.EQUALITY
                "!=" -> ZshkArithmeticOperator.INEQUALITY
                "&&" -> ZshkArithmeticOperator.LOGICAL_AND
                "||" -> ZshkArithmeticOperator.LOGICAL_OR
                "^^" -> ZshkArithmeticOperator.LOGICAL_XOR
                else -> null
            }
        )
}