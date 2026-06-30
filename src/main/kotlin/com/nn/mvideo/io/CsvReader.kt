package com.nn.mvideo.io

import com.nn.mvideo.model.Operation
import java.io.File

class CsvReader {
    fun readOperations(filePath: String): List<Operation> {
        val file = File(filePath)
        if (!file.exists()) {
            println("Ошибка: Файл не найден по пути $filePath")
            return emptyList()
        }

        val operations = mutableListOf<Operation>()

        file.bufferedReader().useLines { lines ->
            val iterator = lines.iterator()
            if (iterator.hasNext()) {
                iterator.next()
            }

            while (iterator.hasNext()) {
                val line = iterator.next()
                if (line.isBlank()) continue

                val tokens = line.split(",")
                if (tokens.size < 4) continue

                val type = tokens[0].trim()
                val groupId = tokens[1].trim()
                val productId = tokens[2].trim()
                val quantity = tokens[3].trim().toIntOrNull() ?: 0

                when (type.uppercase()) {
                    "ARRIVAL" -> operations.add(Operation.Arrival(groupId, productId, quantity))
                    "SALE" -> operations.add(Operation.Sale(groupId, quantity))
                }
            }
        }

        return operations
    }
}