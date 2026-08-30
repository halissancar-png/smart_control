package com.sancarteknik.cihazkontrol.ekranlar


fun kanalSayisiniBul(teknikId: String): Int {
    val match = Regex("SNCR_[A-Z](\\d)_").find(teknikId)
    return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
}

fun aciklamaMap(aciklamaStr: String): Map<Int, String> {
    if (aciklamaStr.isBlank()) return emptyMap()
    return aciklamaStr.split("|").associate {
        val parts = it.split(":")
        if (parts.size == 2) {
            parts[0].toIntOrNull() to parts[1]
        } else {
            null to ""
        }
    }.filterKeys { it != null }.mapKeys { it.key!! }
}