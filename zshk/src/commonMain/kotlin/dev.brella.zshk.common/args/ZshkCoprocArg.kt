package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment

object ZshkCoprocArg : ZshkArg {
    override suspend fun test(env: ShellEnvironment): Boolean = false
}