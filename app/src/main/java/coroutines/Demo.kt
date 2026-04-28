package coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class Demo {



    fun main() = runBlocking {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.IO + job)

        scope.launch {
            println("coroutine 1")
        }

        job.cancel()

        scope.launch {
            delay(1)
            println("coroutine 2")
        }
    }

















    fun main2() = runBlocking {
        try {
            withContext(Dispatchers.IO) {
                val a = async {
                    throw RuntimeException("Ошибка во внутренней корутине")
                }
                a.await()
            }
        } catch(exception: Exception) {
            println("Handle $exception")
        }
    }























    fun main3() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO)
        val job = scope.launch {
            var i = 0

            while (i < 1000) {
                println(i++)
                Thread.sleep(100)
            }
        }

        // Через 1 секунду отменяем скоуп
        Thread.sleep(1000)
        scope.cancel()

        // Ожидаем завершение джобы
        job.join()
    }












    class BankAccount(val balance: Int) {
        fun deposit(amount: Int) {
            balance += amount
        }

        fun withdraw(amount: Int) {
            balance -= amount
        }

        fun main4() = runBlocking {
            val account = BankAccount(1000)

            withContext(Dispatchers.Default) {
                repeat(1000) {
                    launch {
                        account.deposit(100)
                    }
                }
                repeat(1000) {
                    launch {
                        account.withdraw(100)
                    }
                }
            }

            println(account.balance)
        }
    }

}
