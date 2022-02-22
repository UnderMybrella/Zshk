package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg
import dev.brella.zshk.common.args.ZshkCommandModifierArg
import dev.brella.zshk.common.joinToStringSuspend

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
            stderr.write("Command not found: ${commandName}\r\n".encodeToByteArray())
            stderr.flush()

            return 127
        } else {
            return func(args, this)
        }
    }
}