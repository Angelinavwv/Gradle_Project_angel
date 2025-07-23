package org.example

import kotlin.random.Random

class Tank(
    val maxVolume: Double,
    val area: Double,
    val maxHeight: Double,
    startVolume: Double
) {
    var currentVolume: Double = startVolume
        private set // Делаем сеттер приватным, чтобы изменять объем только через методы класса

    val inflowRate: Double = Random.nextDouble(80.0, 100.0)

    var currentOutflowRate: Double = 0.0

    val currentWaterHeight: Double
        get() = (currentVolume / 1000.0) / area

    // Метод для обновления состояния бака за определенный промежуток времени (delta Time)
    fun update(deltaTime: Double) {
        val volumeChange = (inflowRate - currentOutflowRate) * deltaTime
        currentVolume += volumeChange

        // Убедимся, что объем не выходит за пределы бака (0 и maxVolumeLiters)
        if (currentVolume < 0) {
            currentVolume = 0.0
        } else if (currentVolume > maxVolume) {
            currentVolume = maxVolume
        }
    }

    // Метод для установки скорости оттока воды (управление клапаном)
    fun setOutflowRate(rate: Double) {
        // Ограничиваем расход от 0 до 100 литров/сек
        currentOutflowRate = rate.coerceIn(0.0, 100.0)
    }

    // Для Modbus: текущая высота уровня воды в баке (аналоговое чтение)
    // Возвращаем в метрах, т.к. это будет "основной и первый параметр аналогового чтения"
    fun getCurrentHeightForModbus(): Double {
        return currentWaterHeight
    }

    // Для Modbus: текущий расход, какой текущий расход должен быть (аналоговый выход)
    // Это значение, которое "клапан" (ваша модель) пытается достичь
    fun getTargetOutflowRateForModbus(): Double {
        return currentOutflowRate
    }
}