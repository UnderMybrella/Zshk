package dev.brella.zshk.common.args.opcodes

import dev.brella.zshk.common.ShellEnvironment
import dev.brella.zshk.common.args.values.ZshkStringLiteralArg
import dev.brella.zshk.common.args.values.ZshkValueArg

data class ZshkForInWordsOpcode(val names: List<String>, val words: List<ZshkValueArg<*>>, val list: ZshkScriptOpcode) : ZshkOpcode {
    override suspend fun ShellEnvironment.exec(): Int {
        var index = 0

        while (index < words.size) {
            val scopedEnv = this.copy(variables = HashMap(variables))
            names.forEach { name -> scopedEnv.variables[name] = words.getOrNull(index++) ?: ZshkStringLiteralArg.EMPTY }
            list.exec(scopedEnv)
        }

        return 0
    }
}
