package com.guru.processor

fun String.snakeUpperCase() =
    this.map {
        if (it.isUpperCase()) {
            "_${it}"
        } else
            it.uppercase()
    }.joinToString("") {
        it
    }