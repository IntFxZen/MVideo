package com.nn.mvideo.model

sealed class Operation {
    data class Arrival(
        val groupId: String,
        val productId: String,
        val quantity: Int
    ) : Operation()

    data class Sale(
        val groupId: String,
        val quantity: Int
    ) : Operation()
}