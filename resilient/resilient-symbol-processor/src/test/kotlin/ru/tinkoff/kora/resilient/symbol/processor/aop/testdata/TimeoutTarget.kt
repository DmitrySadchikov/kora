package ru.tinkoff.kora.resilient.symbol.processor.aop.testdata

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout

@Component
@Root
open class TimeoutTarget {

    @Timeout("custom1")
    open fun getValueSync(): String {
        Thread.sleep(2000)
        return "OK"
    }

    @Timeout("custom2")
    open fun getValueSyncVoid() {
        Thread.sleep(2000)
    }

    @Timeout("custom3")
    open suspend fun getValueSuspend(): String {
        delay(2000)
        return "OK"
    }

    @Timeout("custom4")
    open fun getValueFLow(): Flow<String> {
        return flow {
            delay(2000)
            emit("OK")
        }
    }

    // Throws here is an alias for kotlin.jvm.Throws
    // Method should compile normally
    @Throws(IllegalStateException::class)
    @Timeout("custom5")
    open fun getValueSyncThrows(): String {
        Thread.sleep(2000)
        return "OK"
    }
}
