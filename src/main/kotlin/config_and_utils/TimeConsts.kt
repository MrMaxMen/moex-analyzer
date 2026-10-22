package com.example.config_and_utils

import java.time.LocalDate



enum class Period(val monthsBack: Long) {
    MONTH(1), QUARTER(3), HALF_YEAR(6), YEAR(12);
    val since: LocalDate
        get() = LocalDate.now().minusMonths(monthsBack)
}