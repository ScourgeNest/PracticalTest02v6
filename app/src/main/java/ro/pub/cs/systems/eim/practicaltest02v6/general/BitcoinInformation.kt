package ro.pub.cs.systems.eim.practicaltest02v6.general

import java.time.LocalDateTime

data class BitcoinInformation(
    val time: LocalDateTime,
    val value: Float,
) {
    // MODIFICARE: Folosim \n (New Line) in loc de virgula
    override fun toString(): String {
        return "Time: $time\n" +
                "Value: $value\n"
    }
}