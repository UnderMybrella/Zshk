package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg

interface ZshkOpcode : ZshkArg {
    suspend fun ShellEnvironment.exec(): Int
    override suspend fun test(env: ShellEnvironment): Boolean =
        exec(env) == 0
}

suspend inline fun ZshkOpcode.exec(env: ShellEnvironment): Int {
    val ret = env.exec()
    env.exitCode = ret
    return ret
}