package dev.brella.zshk.common.args.values

import dev.brella.zshk.common.ShellEnvironment

data class ZshkCharLiteralArg(override val _value: Char) : ZshkStaticValueArg<Char>() {
    companion object {
        val NULL = ZshkCharLiteralArg('\u0000')
    }

    override suspend fun toIntValue(env: ShellEnvironment): Int = _value.code
    override suspend fun test(env: ShellEnvironment): Boolean = _value != '\u0000'
}