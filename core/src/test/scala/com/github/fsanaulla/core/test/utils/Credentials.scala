package com.github.fsanaulla.core.test.utils

import com.github.fsanaulla.core.model.InfluxCredentials

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 02.03.18
  */
trait Credentials {
  implicit val credentials: InfluxCredentials
}
