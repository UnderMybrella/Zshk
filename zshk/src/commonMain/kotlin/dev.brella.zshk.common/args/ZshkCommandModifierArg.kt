package dev.brella.zshk.common.args

import dev.brella.zshk.common.ShellEnvironment

sealed class ZshkCommandModifierArg : ZshkArg {
    object Minus : ZshkCommandModifierArg()
    object Builtin : ZshkCommandModifierArg()
    data class Command(
        val searchDefaultPath: Boolean = false,
        val whence: Boolean = false,
        val whenceVerbose: Boolean = false
    ) : ZshkCommandModifierArg()

    data class Exec(val clear: Boolean = false, val minus: Boolean = false, val argv0: ZshkArg? = null) :
        ZshkCommandModifierArg()

    object NoCorrect : ZshkCommandModifierArg()
    object NoGlob : ZshkCommandModifierArg()

    override suspend fun test(env: ShellEnvironment): Boolean = false
}