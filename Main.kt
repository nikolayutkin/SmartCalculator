package calculator

import java.util.Stack
import java.math.BigInteger

fun main() {
    val mapOfVariables = emptyMap<String, BigInteger>().toMutableMap()
    loop@while (true) {
        val input = readln().replace(" ", "")
            .replace("--".toRegex(), "+")
            .replace("[+]+-|-".toRegex(), ".-.")
            .replace("[+]+".toRegex(), ".+.")
            .replace("[*]".toRegex(), ".*.")
            .replace("/".toRegex(), "./.")
            .replace("[)]".toRegex(), ".)")
            .replace("[(]".toRegex(), "(.")
            .replace("..", ".")
        if (input.contains("*.*") || input.contains("/./")) {
            println("Invalid expression")
            continue@loop
        } else if (input.isEmpty()) {
            continue@loop
        } else if (input.contains("=".toRegex())) { // add new variable
            val arr = input.replace(".", "").split("=")
            if (arr.lastIndex > 1) {
                println("Invalid assignment")
                continue@loop
            }
            var tempName = ""
            var tempValue = BigInteger.valueOf(0)
            for (i in 0..arr.lastIndex) {
                when (giveValue(arr[i], mapOfVariables)) {
                    0 -> if (i == 0) {
                        println("Invalid identifier")
                        continue@loop
                    } else {
                        println("Invalid assignment")
                        continue@loop
                    }

                    1 -> if (i == 0) {
                        println("Invalid identifier")
                        continue@loop
                    } else {
                        println("Unknown variable")
                        continue@loop
                    }

                    2 -> if (i == 0) {
                        println("Invalid identifier")
                        continue@loop
                    } else {
                        tempValue = -mapOfVariables.getValue(arr[i].replace("-", ""))
                    }

                    3 -> if (i == 0) {
                        tempName = arr[i]
                    } else {
                        println("Unknown variable")
                        continue@loop
                    }

                    4 -> if (i == 0) {
                        tempName = arr[i]
                    } else {
                        tempValue = mapOfVariables.getValue(arr[i])
                    }

                    5 -> if (i == 0) {
                        println("Invalid identifier")
                        continue@loop
                    } else {
                        tempValue = arr[i].toBigInteger()
                    }
                }
            }
            mapOfVariables += tempName to tempValue
            continue@loop
        }
        val arr = input.split(".").toMutableList()
        if (!correctBracket(arr)) { //
            println("Invalid expression")
            continue@loop
        }

        if (input.contains("[+*/]|-".toRegex())) {
            for (i in 0 until arr.size) {
                if (arr[i].contains("[+*/]|-".toRegex())) {
                    continue
                }
                when (giveValue(arr[i], mapOfVariables)) {
                    0 -> {
                        println("Invalid identifier")
                        continue@loop
                    }

                    in 1..3 -> {
                        if (input.contains("/".toRegex())) {
                            if (input == "./.help") {
                                println("The program can calculate the product, difference, sum and division of both variables and numbers.\n To write a variable to the database, specify its name and assign a value to it using the \"=\" operand.\n To print the database, type \"/print\". To exit the program, type \"/exit\".")
                                continue@loop
                            } else if (input == "./.exit") {
                                println("Bye!")
                                break@loop
                            } else if (input == "./.print") {
                                println(mapOfVariables)
                                continue@loop
                            } else {
                                println("Unknown command")
                                continue@loop
                            }
                        }
                        println("Unknown variable")
                        continue@loop
                    }
                    4 -> arr[i] = mapOfVariables.getValue(arr[i]).toString()
                    5 -> continue
                }
            }
            val arrRPN= sortToRPN(arr)
            println(solver(arrRPN))
            continue@loop
        } else if (input.contains("[a-zA-Z]".toRegex())) {
            when (giveValue(input, mapOfVariables)) {
                0 -> {
                    println("Invalid identifier")
                    continue@loop
                }
                in 1..3 -> {
                    println("Unknown variable")
                    continue@loop
                }
                4 -> println(mapOfVariables.getValue(input)) //5 never used
            }
        } else if (input.contains("""\d""".toRegex())){
            println(input)
            continue@loop
        } else {
            println("Invalid identifier")
            continue@loop
        }
    }
}

fun solver(arr: MutableList<String>): BigInteger {
    val stack = Stack<String>()

    for (element in arr) {
        if (element.contains("""\d""".toRegex())) {
            stack.push(element)
        } else if (element.contains("[+*/]|-".toRegex())) {
            val y = stack.pop().toBigInteger()
            val x = stack.pop().toBigInteger()

            val result = when (element.toString()) {
                "+" -> x + y
                "-" -> x - y
                "*" -> x * y
                "/" -> x / y
                else -> {
                    println("Unknown operator")
                    0
                }
            }

            stack.push(result.toString())
        }
    }

    //assert(stack.size == 1) { "stack should only have one element at the end of RPN evaluation! but has: $stack" }

    return stack.pop().toBigInteger()
}



fun correctBracket (arr : List<String>): Boolean{
    var bracket = 0
    for (i in arr) {
        if (i == "(") {
            bracket++
        } else if (i == ")") {
            bracket--
        }
    }
    return bracket == 0 // if in input string has wrong quantity of brackets return false
}


fun sortToRPN(inputArray : List<String>): MutableList<String> {
    val stack = mutableListOf<String>()
    val output = mutableListOf<String>()
    for (element in inputArray) {
        when (priority(element)) {

            0 -> {
                stack += element
            }
            1,3 -> {
                sort(element, stack, output)
            }
            2 -> {
                stack += element
            }
            4 -> output += element
        }
    }
    output += stack.asReversed()
    return output
}


fun sort(element: String, stackList: MutableList<String>, outputList: MutableList<String> ) {
    if (stackList.isEmpty()) {
        stackList += element
    } else if (stackList.last() == "(" && element == ")") {
        stackList.removeLast()
    } else if (priority(element) < priority(stackList.last())) {
        stackList += element
    } else {
        outputList += stackList.last()
        stackList.removeLast()
        sort(element, stackList, outputList)
    }
}

fun priority(element: String): Int {
    return when (element) {
        "/", "*" -> {
            0 // max priority
        }
        "+", "-" -> {
            1 // middle priority
        }
        "(" -> {
            2 // low priority
        }
        ")" -> {
            3 // low priority
        }
        else -> {
            4 // number
        }
    }

}

fun giveValue(str: String, map: MutableMap<String, BigInteger>): Int {
    if (str.contains("[a-zA-Z]".toRegex())) {
        return if(str.contains("""\d""".toRegex())) { // correct name
            0 // 0 means continue, name incorrect
        } else if (str.contains("-".toRegex())) {
            if (map[str.replace("-", "")] == null) {  // map doesn't contain this element
                1   // unknown variable
            } else {
                2 // means variable with a minus
            }
        } else {
            if (map[str] == null) {  // map doesn't contain this element
                3   // unknown variable
            } else {
                4 // means number is in map
            }
        }
    } else {
        return 5    // just a number
    }
}
