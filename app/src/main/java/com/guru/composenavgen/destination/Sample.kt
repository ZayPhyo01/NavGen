package com.guru.composenavgen.destination

import com.guru.annonation.Destination

@Destination(name = "destinations_name")
data class Sample(
    val phoneArgs: String = "",
    val phoneCodeArgs: String,
    val isAvailable: Boolean,
    val age: Int,
    val speed: Float,
    val nullable : Int?
)
