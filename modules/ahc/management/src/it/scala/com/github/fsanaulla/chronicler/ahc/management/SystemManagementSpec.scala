package com.github.fsanaulla.chronicler.ahc.management

import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.{EitherValues, BeforeAndAfterAll}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 07.09.17
  */
class SystemManagementSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with EitherValues
    with IntegrationPatience
    with DockerizedInfluxDB
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    influx.close()
    super.afterAll()
  }

  lazy val influx: AhcManagementClient =
    InfluxMng(host, port, Some(credentials))

  "System Management API" should "ping InfluxDB" in {
    val result = influx.ping.futureValue.value
    result.build shouldEqual "OSS"
    result.version shouldEqual version
  }
}
