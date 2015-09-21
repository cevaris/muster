package com.muster.core.utils.datetime

import org.joda.time.{DateTime, DateTimeZone}


object DateTimeUtils {

  val utc: DateTimeZone = DateTimeZone.UTC

  def now = DateTime.now.withZone(utc)

}
