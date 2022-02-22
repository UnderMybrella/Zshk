package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment

sealed class ZshkPipeArg : ZshkArg {
    object StdoutToStdin : ZshkPipeArg()
    object StdoutAndStderrToStdin : ZshkPipeArg()

    override suspend fun test(env: ShellEnvironment): Boolean = false
}