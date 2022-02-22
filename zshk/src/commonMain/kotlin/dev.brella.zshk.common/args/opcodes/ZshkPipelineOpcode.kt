package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment

data class ZshkPipelineOpcode(val isCoProc: Boolean, val op: ZshkOpcode) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int =
        op.exec(this)

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "PIPELINE($isCoProc, ${op.toDebugString(env)})"
}