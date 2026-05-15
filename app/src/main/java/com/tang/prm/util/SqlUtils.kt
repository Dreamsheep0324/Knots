package com.tang.prm.util

fun String.escapeSqlWildcards(): String =
    replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
