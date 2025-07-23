

fun main() {
    // Инициализация бака
    val tank = Tank(
        maxVolume = 4000.0, // 4 м³
        area = 1.0,      // 1 м²
        maxHeight = 4.0,         // 4 м
        startVolume = 1000.0 // Начнем с 1000 литров (1 метр)
    )

    println("--- Tank Simulation Started ---")
    println("Initial water level: ${String.format("%.2f", tank.currentWaterHeight)} meters")
    println("Constant inflow rate: ${String.format("%.2f", tank.inflowRate)} L/s")
    println("------------------------------")

    val simulationDurationSeconds = 60 // Длительность симуляции в секундах
    val timeStepSeconds = 1.0       // Шаг симуляции (1 секунда)

    // Симуляция во времени
    for (time in 0..simulationDurationSeconds step timeStepSeconds.toInt()) {
        // Имитируем внешнее управление расходом (пока просто пример)
        // В реальной задаче здесь будет приходить значение от 4diac
        if (time == 10) {
            println("\n--- At ${time}s: Setting outflow to 50 L/s ---")
            tank.setOutflowRate(50.0) // Пример: установим отток на 50 л/с
        } else if (time == 30) {
            println("\n--- At ${time}s: Setting outflow to 90 L/s ---")
            tank.setOutflowRate(90.0) // Пример: увеличим отток
        } else if (time == 50) {
            println("\n--- At ${time}s: Setting outflow to 0 L/s (close valve) ---")
            tank.setOutflowRate(0.0)  // Пример: закроем клапан
        }


        tank.update(timeStepSeconds) // Обновляем состояние бака

        println("Time: ${String.format("%02d", time)}s | " +
                "Level: ${String.format("%.2f", tank.currentWaterHeight)}m | " +
                "Volume: ${String.format("%.2f", tank.currentVolume)}L | " +
                "Outflow: ${String.format("%.2f", tank.currentOutflowRate)} L/s")

        // Можно добавить задержку для визуализации, если запускать как приложение
        // Thread.sleep(100) // Задержка в 100 миллисекунд для замедления симуляции
    }

    println("\n--- Simulation Finished ---")
    println("Final water level: ${String.format("%.2f", tank.currentWaterHeight)} meters")
}