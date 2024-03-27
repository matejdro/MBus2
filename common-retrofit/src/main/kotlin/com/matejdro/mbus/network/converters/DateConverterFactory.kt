package com.matejdro.mbus.network.converters

import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateConverterFactory : Converter.Factory() {
   override fun stringConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, String>? {
      return if (type == LocalDate::class.java) {
         Converter<LocalDate, String> { DateTimeFormatter.ISO_DATE.format(it) }
      } else {
         null
      }
   }
}
