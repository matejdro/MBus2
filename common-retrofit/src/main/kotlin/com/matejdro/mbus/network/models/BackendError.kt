package com.matejdro.mbus.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendError(
   @Json(name = "Message")
   val message: String?,
   @Json(name = "MessageDetail")
   val messageDetail: String?,
   val response: Response?,
) {
   @JsonClass(generateAdapter = true)
   data class Response(
      @Json(name = "Message")
      val message: String?,
      @Json(name = "Status")
      val status: Int?,
   )
}
