package com.github.fsanaulla.integration

import com.github.fsanaulla.InfluxClientsFactory
import com.github.fsanaulla.api.SafelyApi
import com.github.fsanaulla.clients.InfluxHttpClient
import com.github.fsanaulla.utils.SampleEntitys._
import com.github.fsanaulla.utils.TestHelper.{FakeEntity, _}
import com.github.fsanaulla.utils.TestSpec
import org.scalatest.BeforeAndAfterAll

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 28.09.17
  */
class SafelyApiSpec extends TestSpec with BeforeAndAfterAll {

  override def afterAll: Unit = {

    influx.dropDatabase(safeDB)

    influx.close()
  }

  val safeDB = "safe_db_spec"

  val safeMeas = "safe_meas"

  // INIT INFLUX CLIENT
  lazy val influx: InfluxHttpClient = InfluxClientsFactory
    .createHttpClient(
      host = influxHost,
      username = credentials.username,
      password = credentials.password)

  lazy val safeApi: SafelyApi[FakeEntity] = influx.safely[FakeEntity](safeDB, safeMeas)


  "Safe entity" should "init env" in {

    influx.createDatabase(safeDB).futureValue shouldEqual OkResult
  }

  it should "make safe single write" in {

    safeApi.write(singleEntity).futureValue shouldEqual NoContentResult
  }

  it should "make safe bulk write" in {

    safeApi.bulkWrite(multiEntitys).futureValue shouldEqual NoContentResult
  }

}
