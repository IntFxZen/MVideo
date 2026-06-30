package com.nn.mvideo.service

import com.nn.mvideo.model.Operation
import com.nn.mvideo.model.ProductBalance
import org.slf4j.LoggerFactory
import java.util.TreeMap

class InventoryService {
    private val logger = LoggerFactory.getLogger(InventoryService::class.java)
    private val storage = TreeMap<String, TreeMap<String, Int>>()

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
        logger.debug("Поступление: группа=${arrival.groupId}, товар=${arrival.productId}, " +
                "было=$currentStock, добавлено=${arrival.quantity}, стало=${currentStock + arrival.quantity}")
    }

    private fun handleSale(sale: Operation.Sale) {
        val group = storage[sale.groupId]
        if (group == null || group.isEmpty()) {
            val fallbackGroup = TreeMap<String, Int>()
            fallbackGroup["unknown_product"] = -sale.quantity
            storage[sale.groupId] = fallbackGroup
            logger.warn("Группа '${sale.groupId}' не содержит товаров. " +
                    "Создана запись 'unknown_product' с отрицательным остатком: ${-sale.quantity}")
            return
        }

        var remainingToSell = sale.quantity
        logger.debug("Начало продажи из группы '${sale.groupId}' на сумму $remainingToSell")

        val iterator = group.entries.iterator()
        while (iterator.hasNext() && remainingToSell > 0) {
            val entry = iterator.next()
            val currentStock = entry.value
            val productId = entry.key

            if (currentStock > 0) {
                if (currentStock >= remainingToSell) {
                    entry.setValue(currentStock - remainingToSell)
                    logger.debug("Товар $productId: продано $remainingToSell (было $currentStock, осталось ${currentStock - remainingToSell})")
                    remainingToSell = 0
                } else {
                    logger.debug("Товар $productId: продано полностью $currentStock штук")
                    remainingToSell -= currentStock
                    entry.setValue(0)
                }
            }
        }

        if (remainingToSell > 0) {
            val firstProductId = group.firstKey()
            val newBalance = group[firstProductId]!! - remainingToSell
            group[firstProductId] = newBalance
            logger.warn("Недостаточно товара в группе '${sale.groupId}': " +
                    "необходимо продать ещё $remainingToSell, долг записан на товар '$firstProductId' (остаток: $newBalance)")
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