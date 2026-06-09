package com.example.ui

object CurrencyUtils {
    fun getFormatter(currencyCode: String): java.text.NumberFormat {
        return try {
            java.text.NumberFormat.getCurrencyInstance().apply {
                currency = java.util.Currency.getInstance(currencyCode)
            }
        } catch (e: Exception) {
            java.text.NumberFormat.getCurrencyInstance() // fallback
        }
    }

    fun convertFromNPR(amount: Double, currency: String): Double {
        return when(currency) {
            "USD" -> amount * 0.0075
            "EUR" -> amount * 0.0069
            "GBP" -> amount * 0.0059
            "INR" -> amount * 0.625
            "AUD" -> amount * 0.011
            "CAD" -> amount * 0.010
            else -> amount // NPR
        }
    }

    fun convertToNPR(amount: Double, currency: String): Double {
        return when(currency) {
            "USD" -> amount / 0.0075
            "EUR" -> amount / 0.0069
            "GBP" -> amount / 0.0059
            "INR" -> amount / 0.625
            "AUD" -> amount / 0.011
            "CAD" -> amount / 0.010
            else -> amount // NPR
        }
    }
}
