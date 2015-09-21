package com.muster.core.utils.context

import com.muster.core.utils.datetime.DateTimeUtils
import java.util.UUID
import org.joda.time.DateTimeZone

object ContextHelper {
  val utc: DateTimeZone = DateTimeZone.UTC

  def newContext = {
    com.muster.thriftscala.Context(
      UUID.randomUUID.toString,
      DateTimeUtils.now.toString
    )
  }
}
