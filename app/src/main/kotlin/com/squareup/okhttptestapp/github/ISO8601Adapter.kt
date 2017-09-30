package com.squareup.okhttptestapp.github

import com.apollographql.apollo.CustomTypeAdapter
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class ISO8601Adapter : CustomTypeAdapter<Date> {
  override fun decode(value: String): Date {
    try {
      return ISO8601.parse(value.replace("Z", "+00:00"))
    } catch (e: ParseException) {
      throw IllegalArgumentException(value + " is not a valid ISO 8601 date", e)
    }
  }

  override fun encode(value: Date): String {
    return ISO8601.format(value)
  }

  companion object {
    private val ISO8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
  }
}