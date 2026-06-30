package com.nn.mvideo.service

import com.nn.mvideo.model.Operation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryServiceTest {

    private lateinit var service: InventoryService

    @BeforeEach
    fun setUp() {
        service = InventoryService()
    }

    @Test
    fun `should process arrival correctly`() {
        // Given
        val arrival = Operation.Arrival(groupId = "g1", productId = "p1", quantity = 10)

        // When
        service.processOperation(arrival)

        // Then
        val stocks = service.getCurrentStocks()
        assertEquals(1, stocks.size)
        assertEquals("g1", stocks[0].groupId)
        assertEquals("p1", stocks[0].productId)
        assertEquals(10, stocks[0].quantity)
    }

    @Test
    fun `should FIFO write-off by lexicographical order`() {
        // Given - заносим два товара в группу. "p1" имеет более высокий ранг, чем "p2" (по алфавиту)
        service.processOperation(Operation.Arrival(groupId = "g1", productId = "p2", quantity = 10))
        service.processOperation(Operation.Arrival(groupId = "g1", productId = "p1", quantity = 10))

        // When - продаем 15 штук. Должно списать все 10 у "p1" и 5 у "p2"
        service.processOperation(Operation.Sale(groupId = "g1", quantity = 15))

        // Then
        val stocks = service.getCurrentStocks().associateBy { it.productId }
        assertEquals(0, stocks["p1"]?.quantity)
        assertEquals(5, stocks["p2"]?.quantity)
    }

    @Test
    fun `should drive first lexicographical product into negative when stock is insufficient`() {
        // Given
        service.processOperation(Operation.Arrival(groupId = "g1", productId = "p2", quantity = 5))
        service.processOperation(Operation.Arrival(groupId = "g1", productId = "p1", quantity = 5))

        // When - продаем 15 штук при наличии всего 10. Долг (5 шт) должен уйти на "p1" (первый по алфавиту)
        service.processOperation(Operation.Sale(groupId = "g1", quantity = 15))

        // Then
        val stocks = service.getCurrentStocks().associateBy { it.productId }
        assertEquals(-5, stocks["p1"]?.quantity)
        assertEquals(0, stocks["p2"]?.quantity)
    }

    @Test
    fun `should create unknown_product with negative balance when selling from empty group`() {
        // When - продаем из группы, которой вообще не было на складе
        service.processOperation(Operation.Sale(groupId = "g2", quantity = 8))

        // Then
        val stocks = service.getCurrentStocks()
        assertEquals(1, stocks.size)
        assertEquals("g2", stocks[0].groupId)
        assertEquals("unknown_product", stocks[0].productId)
        assertEquals(-8, stocks[0].quantity)
    }
}