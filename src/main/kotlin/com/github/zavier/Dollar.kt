package com.github.zavier

interface Expression {

    fun reduce(bank: Bank, to: String): Money

    fun plus(addend: Expression): Expression

    fun times(multiplier: Int): Expression
}

class Bank {
    private val rates = mutableMapOf<Pair<String, String>, Int>()

    fun reduce(source: Expression, to: String): Money {
        return source.reduce(this, to)
    }

    fun rate(from: String, to: String): Int {
        if (from == to) {
            return 1
        }
        return rates[Pair(from, to)]
            ?: throw IllegalArgumentException("Missing rate: $from -> $to")}

    fun addRate(from: String, to: String, rate: Int) {
        rates[Pair(from, to)] = rate
    }
}

class Sum(val augend: Expression, val addend: Expression): Expression {
    override fun reduce(bank: Bank, to: String): Money {
        return Money(to, augend.reduce(bank, to).amount + addend.reduce(bank, to).amount)
    }

    override fun plus(addend: Expression): Expression {
        return Sum(this, addend)
    }

    override fun times(multiplier: Int): Expression {
        return Sum(augend.times(multiplier), addend.times(multiplier))
    }
}

open class Money(protected val currency: String, val amount: Int): Expression {

    override operator fun plus(addend: Expression): Expression {
        return Sum(this, addend)
    }

    override operator fun times(multiplier: Int): Expression {
        return Money(currency, amount * multiplier)
    }

    override fun reduce(bank: Bank, to: String): Money {
        val rate = bank.rate(currency, to)
        return Money(to, amount / rate)
    }

    companion object {
        fun dollar(amount: Int): Money {
            return Money("USD", amount)
        }

        fun franc(amount: Int): Money {
            return Money("CHF", amount)
        }
    }
    fun currency(): String = currency

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Money) {
            return amount == other.amount && currency == other.currency
        }

        return false
    }

    override fun hashCode(): Int {
        return amount
    }

    override fun toString(): String {
        return "Money(currency='$currency', amount=$amount)"
    }
}

