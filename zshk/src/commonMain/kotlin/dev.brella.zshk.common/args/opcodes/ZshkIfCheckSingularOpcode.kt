package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg

data class ZshkIfCheckSingularOpcode(val check: ZshkArg, val branch: ZshkOpcode) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int {
        if (check.test(this)) branch.exec(this)

        return 0
    }
}