package dev.brella.zshk.common.args.opcodes

import dev.brella.kornea.toolkit.common.printLine
import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg
import dev.brella.zshk.common.args.ZshkCommandModifierArg
import dev.brella.zshk.common.joinToStringSuspend
import dev.brella.zshk.common.stderr

data class ZshkCommandOpcode(
    val commandModifier: ZshkCommandModifierArg?,
    val args: List<ZshkArg>
) : ZshkOpcode {
    override suspend fun toDebugString(env: ShellEnvironment): String =
        "SIMPLE_COMMAND(modifier=$commandModifier args=(${
            args.joinToStringSuspend { "\"${it.toDebugString(env)}\"" }
        }))"

    override suspend fun ShellEnvironment.exec(): Int {
        val commandName = args.firstOrNull()?.toStringValue(this) ?: ""
        val func = getFunction(commandName)
        if (func == null) {
            stderr?.printLine("Command not found: ${commandName}\r\n")
            stderr?.flush()

            return 127
        } else {
            return func(args, this)
        }
    }
}