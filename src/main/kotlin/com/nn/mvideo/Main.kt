package com.nn.mvideo

import com.nn.mvideo.io.CsvReader
import com.nn.mvideo.io.CsvWriter
import com.nn.mvideo.service.InventoryService
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Main")

fun main(args: Array<String>) {
    if (args.size < 2) {
        logger.error("Необходимо передать два аргумента: <путь_к_input.csv> <путь_к_output.csv>")
        return
    }

    val inputPath = args[0]
    val outputPath = args[1]

    logger.info("Запуск обработки инвентаризации...")
    logger.info("Входной файл: $inputPath")
    logger.info("Выходной файл: $outputPath")

    val csvReader = CsvReader()
    val csvWriter = CsvWriter()
    val inventoryService = InventoryService()

    val operations = csvReader.readOperations(inputPath)
    if (operations.isEmpty()) {
        logger.warn("Не найдено операций для обработки или файл пуст.")
        return
    }
    logger.info("Успешно прочитано операций: ${operations.size}")

    for (operation in operations) {
        inventoryService.processOperation(operation)
    }
    logger.info("Все операции успешно обработаны.")

    val finalStocks = inventoryService.getCurrentStocks()

    csvWriter.writeStocks(outputPath, finalStocks)
    logger.info("Готово! Результаты сохранены в $outputPath")
}