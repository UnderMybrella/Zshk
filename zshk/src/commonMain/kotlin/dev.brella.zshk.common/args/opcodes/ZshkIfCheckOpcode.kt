package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.ZshkArg

data class ZshkIfCheckOpcode(val checksAndBranches: List<Pair<ZshkArg, ZshkOpcode>>, val elseThen: ZshkOpcode?) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int {
        val branch = checksAndBranches.firstOrNull { (check) -> check.test(this) }
        if (branch != null) {
            branch.second.exec(this)
        } else {
            elseThen?.exec(this)
        }

        return 0
    }
}