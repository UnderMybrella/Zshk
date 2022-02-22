package dev.brella.zshk

public fun ByteArray.indexOf(element: Byte, from: Int = 0, to: Int = size): Int {
    for (index in from until to) {
        if (element == this[index]) {
            return index
        }
    }
    return -1
}
