package com.simplrtek.enriched

object PimpTest {
    implicit class StringImprovements(val s: String) {
        def increment(aps:String = ""):String={ s.map(c => (c + 1).toChar)}
    }
}