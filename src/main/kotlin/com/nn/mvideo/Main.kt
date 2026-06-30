package com.nn.mvideo

import com.nn.mvideo.io.CsvReader
import com.nn.mvideo.io.CsvWriter
import com.nn.mvideo.service.InventoryService

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Ошибка: Необходимо передать два аргумента: <путь_к_input.csv> <путь_к_output.csv>")
        return
    }

    val inputPath = args[0]
    val outputPath = args[1]

    println("Запуск обработки инвентаризации...")
    println("Входной файл: $inputPath")
    println("Выходной файл: $outputPath")

    val csvReader = CsvReader()
    val csvWriter = CsvWriter()
    val inventoryService = InventoryService()

    val operations = csvReader.readOperations(inputPath)
    if (operations.isEmpty()) {
        println("Предупреждение: Не найдено операций для обработки или файл пуст.")
        return
    }
    println("Успешно прочитано операций: ${operations.size}")

    for (operation in operations) {
        inventoryService.processOperation(operation)
    }
    println("Все операции успешно обработаны.")

    val finalStocks = inventoryService.getCurrentStocks()

    csvWriter.writeStocks(outputPath, finalStocks)
    println("Готово! Результаты сохранены в $outputPath")
}