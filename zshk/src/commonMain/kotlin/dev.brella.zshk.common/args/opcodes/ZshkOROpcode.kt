package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment

data class ZshkOROpcode(val pipeline: ZshkPipelineOpcode) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int =
        if (exitCode != 0) pipeline.exec(this)
        else exitCode

    override suspend fun toDebugString(env: ShellEnvironment): String =
        "|| ${pipeline.toDebugString(env)}"
}