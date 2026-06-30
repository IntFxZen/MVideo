package com.nn.mvideo.service

import com.nn.mvideo.model.Operation
import com.nn.mvideo.model.ProductBalance
import java.util.TreeMap

class InventoryService {
    private val storage = HashMap<String, TreeMap<String, Int>>()

    fun processOperation(operation: Operation) {
        when (operation) {
            is Operation.Arrival -> handleArrival(operation)
            is Operation.Sale -> handleSale(operation)
        }
    }

    private fun handleArrival(arrival: Operation.Arrival) {
        val group = storage.getOrPut(arrival.groupId) { TreeMap() }
        val currentStock = group.getOrDefault(arrival.productId, 0)
        group[arrival.productId] = currentStock + arrival.quantity
    }

    private fun handleSale(sale: Operation.Sale) {
        val group = storage[sale.groupId]
        if (group == null || group.isEmpty()) {
            val fallbackGroup = TreeMap<String, Int>()
            fallbackGroup["unknown_product"] = -sale.quantity
            storage[sale.groupId] = fallbackGroup
            return
        }

        var remainingToSell = sale.quantity

        val iterator = group.entries.iterator()
        while (iterator.hasNext() && remainingToSell > 0) {
            val entry = iterator.next()
            val currentStock = entry.value

            if (currentStock > 0) {
                if (currentStock >= remainingToSell) {
                    entry.setValue(currentStock - remainingToSell)
                    remainingToSell = 0
                } else {
                    remainingToSell -= currentStock
                    entry.setValue(0)
                }
            }
        }

        if (remainingToSell > 0) {
            val firstProductId = group.firstKey()
            group[firstProductId] = group[firstProductId]!! - remainingToSell
        }
    }

    fun getCurrentStocks(): List<ProductBalance> {
        return storage.flatMap { (groupId, products) ->
            products.map { (productId, quantity) ->
                ProductBalance(groupId, productId, quantity)
            }
        }
    }
}