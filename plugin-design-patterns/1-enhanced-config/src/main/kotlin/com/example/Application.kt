package com.example

/**
 * Sample application demonstrating the Enhanced Config pattern. This pattern uses an optimized
 * monolithic build configuration.
 */
class Application {
  fun greet(name: String): String {
    return "Hello, $name! Welcome to Enhanced Config Pattern."
  }

  fun calculateSum(numbers: List<Int>): Int {
    return numbers.sum()
  }
}

fun main() {
  val app = Application()
  println(app.greet("Developer"))

  val numbers = listOf(1, 2, 3, 4, 5)
  println("Sum of $numbers = ${app.calculateSum(numbers)}")
}
