package com.nn.mvideo.io

import com.nn.mvideo.model.ProductBalance
import java.io.File

class CsvWriter {
    fun writeStocks(filePath: String, stocks: List<ProductBalance>) {
        val file = File(filePath)

        file.bufferedWriter().use { writer ->
            writer.write("group_id,product_id,quantity")
            writer.newLine()

            for (stock in stocks) {
                writer.write("${stock.groupId},${stock.productId},${stock.quantity}")
                writer.newLine()
            }
        }
    }
}