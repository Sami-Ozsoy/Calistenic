package com.example.calistenic.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.toInt() }
        }
    }

    @TypeConverter
    fun fromList(list: List<Int>): String {
        return list.joinToString(",")
    }
}
