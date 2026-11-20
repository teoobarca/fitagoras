package dev.hamper.tabula.ui

internal fun slovakPlural(count: Int, one: String, few: String, other: String): String =
    when (count) {
        1 -> one
        in 2..4 -> few
        else -> other
    }
