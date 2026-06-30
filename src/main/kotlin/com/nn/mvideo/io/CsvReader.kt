package com.nn.mvideo.io

import com.nn.mvideo.model.Operation
import org.slf4j.LoggerFactory
import java.io.File

class CsvReader {
    private val logger = LoggerFactory.getLogger(CsvReader::class.java)

    fun readOperations(filePath: String): List<Operation> {
        val file = File(filePath)
        if (!file.exists()) {
            logger.error("Файл не найден по пути $filePath")
            return emptyList()
        }

        val operations = mutableListOf<Operation>()

        file.bufferedReader().useLines { lines ->
            lines.forEachIndexed { idx, raw ->
                val lineNumber = idx + 1
                val line = raw.trim()
                if (line.isBlank()) return@forEachIndexed

                // Try semicolon-separated first, then comma fallback
                var tokens = line.split(";").map { it.trim() }
                if (tokens.size == 1) tokens = line.split(",").map { it.trim() }

                when (tokens.size) {
                    3 -> {
                        // Arrival: groupId;productId;quantity
                        val groupId = tokens[0]
                        val productId = tokens[1]
                        val quantity = tokens[2].toIntOrNull()
                        if (quantity == null) {
                            logger.warn("Неверное число на строке $lineNumber: ${tokens[2]}")
                            return@forEachIndexed
                        }
                        operations.add(Operation.Arrival(groupId, productId, quantity))
                        logger.debug("Загружена операция поступления: группа=$groupId, товар=$productId, кол-во=$quantity")
                    }
                    2 -> {
                        // Sale: groupId;quantity
                        val groupId = tokens[0]
                        val quantity = tokens[1].toIntOrNull()
                        if (quantity == null) {
                            logger.warn("Неверное число на строке $lineNumber: ${tokens[1]}")
                            return@forEachIndexed
                        }
                        operations.add(Operation.Sale(groupId, quantity))
                        logger.debug("Загружена операция продажи: группа=$groupId, кол-во=$quantity")
                    }
                    else -> {
                        // Fallback: old CSV with type column (comma or semicolon)
                        val alt = line.split(",").map { it.trim() }
                        if (alt.size >= 4) {
                            val type = alt[0]
                            val groupId = alt[1]
                            val productId = alt[2]
                            val quantity = alt[3].toIntOrNull() ?: 0
                            when (type.uppercase()) {
                                "ARRIVAL" -> {
                                    operations.add(Operation.Arrival(groupId, productId, quantity))
                                    logger.debug("Загружена операция (fallback) поступления: группа=$groupId, товар=$productId, кол-во=$quantity")
                                }
                                "SALE" -> {
                                    operations.add(Operation.Sale(groupId, quantity))
                                    logger.debug("Загружена операция (fallback) продажи: группа=$groupId, кол-во=$quantity")
                                }
                                else -> logger.warn("Неизвестный тип операции на строке $lineNumber: $type")
                            }
                        } else {
                            logger.warn("Неверный формат на строке $lineNumber: $line")
                        }
                    }
                }
            }
        }

        logger.info("Успешно загружено операций: ${operations.size}")
        return operations
    }
}