package dev.almasum.fittrack.models

import java.io.Serializable

data class Item(
    val code: String,
    val name: String,
    val imageUrl: String,
    val group: String,
    val energy: Double,
    val fat: Double,
    val protein: Double,
    val sugar: Double,
    val sodium: Double,
) : Serializable