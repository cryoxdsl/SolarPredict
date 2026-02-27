package com.example.solarpredict.data.csv

import com.example.solarpredict.domain.model.ActualProductionDay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class EnphaseCsvParser {
    fun parse(raw: String): List<ActualProductionDay> {
        val lines = raw.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
        if (lines.isEmpty()) return emptyList()
        val sep = detectSeparator(lines.first())
        val header = lines.first().split(sep).map { it.trim().lowercase() }
        val rows = lines.drop(1)
        val hasHourly = header.any { it.contains("hour") || it.contains("time") }
        val dateIdx = header.indexOfFirst { it.contains("date") || it.contains("day") }
        val timeIdx = header.indexOfFirst { it.contains("time") || it.contains("hour") }
        val energyIdx = header.indexOfFirst { it.contains("energy") || it.contains("kwh") || it.contains("wh") || it.contains("power") }
        if (dateIdx == -1 || energyIdx == -1) return emptyList()

        val byDay = mutableMapOf<LocalDate, MutableList<Pair<Int?, Double>>>()
        rows.forEach { row ->
            val cols = row.split(sep).map { it.trim() }
            if (cols.size <= maxOf(dateIdx, energyIdx, if (timeIdx == -1) 0 else timeIdx)) return@forEach
            val date = parseDate(cols[dateIdx]) ?: return@forEach
            val hour = if (hasHourly && timeIdx >= 0) parseHour(cols[timeIdx]) else null
            val value = parseNumber(cols[energyIdx]) ?: return@forEach
            val kwh = if (header[energyIdx].contains("wh") && !header[energyIdx].contains("kwh")) value / 1000.0 else value
            byDay.getOrPut(date) { mutableListOf() }.add(hour to kwh)
        }

        return byDay.entries.map { (date, items) ->
            val hourly = if (items.any { it.first != null }) {
                MutableList(24) { 0.0 }.also { arr ->
                    items.forEach { (h, v) -> if (h != null && h in 0..23) arr[h] += v * 1000.0 }
                }
            } else null
            val daily = items.sumOf { it.second }
            ActualProductionDay(date = date, hourlyPowerW = hourly, dailyKwh = daily)
        }.sortedBy { it.date }
    }

    private fun detectSeparator(line: String) = if (line.count { it == ';' } > line.count { it == ',' }) ';' else ','

    private fun parseNumber(raw: String): Double? {
        val normalized = raw.replace(" ", "").replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    private fun parseDate(raw: String): LocalDate? {
        val patterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy")
        for (p in patterns) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(p))
            } catch (_: DateTimeParseException) {
            }
        }
        return try {
            LocalDateTime.parse(raw).toLocalDate()
        } catch (_: Exception) {
            null
        }
    }

    private fun parseHour(raw: String): Int? {
        val clean = raw.trim()
        val direct = clean.toIntOrNull()
        if (direct != null && direct in 0..23) return direct
        val parts = clean.split(":")
        return parts.firstOrNull()?.toIntOrNull()?.takeIf { it in 0..23 }
    }
}
