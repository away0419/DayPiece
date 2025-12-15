package com.example.daypiece.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * Compose Color를 JSON으로 직렬화/역직렬화하기 위한 TypeAdapter
 */
class ColorTypeAdapter : TypeAdapter<Color>() {
    override fun write(out: JsonWriter, value: Color?) {
        if (value == null) {
            out.nullValue()
        } else {
            // Color를 ARGB Long 값으로 변환하여 저장
            out.value(value.value.toLong())
        }
    }

    override fun read(`in`: JsonReader): Color? {
        if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        // Long 값을 읽어서 Color로 변환
        val colorValue = `in`.nextLong()
        return Color(colorValue.toULong())
    }
}

