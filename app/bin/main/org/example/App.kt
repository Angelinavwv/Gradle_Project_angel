package org.example
import com.ghgande.j2mod.modbus.Modbus
import com.ghgande.j2mod.modbus.ModbusCoupler
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister
import com.ghgande.j2mod.modbus.net.ModbusTCPListener
import com.ghgande.j2mod.modbus.procimg.InputRegister
import com.ghgande.j2mod.modbus.procimg.Register
import com.ghgande.j2mod.modbus.util.ModbusUtil
import kotlin.concurrent.thread // Для запуска в отдельном потоке
import kotlin.random.Random // Для Tank.kt

fun main() {
    // Инициализация бака
    val tank = Tank(
        maxVolume = 4000.0,
        area = 1.0,
        maxHeight = 4.0,
        startVolume = 1000.0
    )

   println("Initial water level: ${String.format("%.2f", tank.currentWaterHeight)} meters")
    println("Constant inflow rate: ${String.format("%.2f", tank.inflowRate)} L/s")

    // 2. Настройка Modbus Slave (Server)
    val port = 502 // Стандартный порт Modbus TCP
    val unitID = 1 // ID устройства Modbus
    val listener = ModbusTCPListener(5) // Максимум 5 одновременных подключений
    listener.setPort(port)

    // Процессный образ (Process Image) - это "память" Modbus устройства
    val processImage = SimpleProcessImage()
    ModbusCoupler.getReference().set ";Master" as a reference. setProcessImage(processImage)
    ModbusCoupler.getReference().setUnitID(unitID)

    // Регистры для данных:
    // Аналоговое чтение: Текущая высота уровня воды в баке (Input Register)
    // Modbus регистры 16-битные. Double нужно преобразовать.
    // Уровень воды (например, 0.00-4.00 м). Мы можем умножить на 10000, чтобы получить целое число
    // Например, 1.2345 м -> 12345. Это потребует соответствующего масштабирования на стороне Master.
    val levelRegisterAddress = 0 // Регистр 0 (Holding Register) - для записи/чтения
    // Мы будем использовать Holding Register для уровня, так как Modbus Master, скорее всего, будет читать Holding Registers
    // и Input Registers для Analog Input (AI).
    // Согласно заданию: "Основной и первый параметр аналогового чтения это текущая высота уровня воды в баке."
    // AI (аналоговый вход) в Modbus соответствуют Input Registers.
    val levelInputRegisterAddress = 0 // Адрес Input Register для уровня

    processImage.addInputRegister(SimpleInputRegister(0)) // Регистр 0 для уровня воды

    // Аналоговый выход: Текущий расход (Holding Register) - для записи Master'ом и чтения Slave'ом
    // "Аналоговый выход это расход, какой текущий расход должен быть."
    // Это значение, которое 4diac будет записывать.
    val outflowRateHoldingRegisterAddress = 1 // Адрес Holding Register для расхода
    processImage.addRegister(SimpleInputRegister(0)) // Регистр 1 для оттока, как Holding Register


    // 3. Запуск Modbus Listener в отдельном потоке
    thread {
        try {
            listener.start()
            println("Modbus TCP Listener started on port $port, Unit ID $unitID")
        } catch (e: Exception) {
            System.err.println("Error starting Modbus Listener: ${e.message}")
            e.printStackTrace()
        }
    }

    // 4. Основной цикл симуляции
    val simulationDurationSeconds = 300 // Длительность симуляции в секундах (5 минут)
    val timeStepSeconds = 0.1          // Шаг симуляции (100 мс) - чем меньше, тем точнее, но больше расчетов

    var currentTime = 0.0
    while (currentTime <= simulationDurationSeconds) {
        // Обновление регистра уровня воды для Modbus
        val currentLevelScaled = (tank.currentWaterLevelMeters * 10000).toInt() // Масштабируем до целого числа
        processImage.getInputRegister(levelInputRegisterAddress).setValue(currentLevelScaled)

        // Чтение целевого расхода от Modbus Master (если он записал его)
        val targetOutflowRateScaled = processImage.getRegister(outflowRateHoldingRegisterAddress).value
        val targetOutflowRate = targetOutflowRateScaled / 100.0 // Предполагаем масштабирование Master'ом

        tank.setOutflowRate(targetOutflowRate) // Устанавливаем расход в модели бака

        tank.update(timeStepSeconds) // Обновляем состояние бака

        // Вывод состояния для отладки
        println("Time: ${String.format("%.1f", currentTime)}s | " +
                "Level: ${String.format("%.3f", tank.currentWaterHeight)}m | " +
                "Volume: ${String.format("%.2f", tank.currentVolume)}L | " +
                "Inflow: ${String.format("%.2f", tank.inflowRate)} L/s | " +
                "Outflow: ${String.format("%.2f", tank.currentOutflowRate)} L/s | " +
                "Modbus Level Reg: $currentLevelScaled | " +
                "Modbus Outflow Reg: $targetOutflowRateScaled")

        currentTime += timeStepSeconds
        Thread.sleep((timeStepSeconds * 1000).toLong()) // Задержка для симуляции реального времени
    }

    // 5. Остановка Modbus Listener
    listener.stop()
    println("Modbus TCP Listener stopped.")
    println("--- Simulation Finished ---")
    println("Final water level: ${String.format("%.2f", tank.currentWaterHeight)} meters")
}
    