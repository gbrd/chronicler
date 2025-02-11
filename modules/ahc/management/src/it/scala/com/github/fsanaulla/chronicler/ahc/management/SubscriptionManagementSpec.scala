package com.github.fsanaulla.chronicler.ahc.management

import com.github.fsanaulla.chronicler.core.duration._
import com.github.fsanaulla.chronicler.core.enums.{Destination, Destinations}
import com.github.fsanaulla.chronicler.core.model.Subscription
import com.github.fsanaulla.chronicler.testing.it.DockerizedInfluxDB
import org.scalatest.{EitherValues, BeforeAndAfterAll}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by
  * Author: fayaz.sanaulla@gmail.com
  * Date: 21.08.17
  */
class SubscriptionManagementSpec
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

  val subName                       = "subs"
  val dbName                        = "async_subs_spec_db"
  val rpName                        = "subs_rp"
  val destType: Destination         = Destinations.ANY
  val newDestType: Destination      = Destinations.ALL
  val hosts: Array[String]          = Array("udp://h1.example.com:9090", "udp://h2.example.com:9090")
  val subscription: Subscription    = Subscription(rpName, subName, destType, hosts)
  val newSubscription: Subscription = subscription.copy(destType = newDestType)

  val duration: String = 1.hours + 30.minutes

  lazy val influx: AhcManagementClient =
    InfluxMng(host, port, Some(credentials))

  "Subscription API" should "create subscription" in {

    influx.createDatabase(dbName).futureValue.value shouldEqual 200

    influx
      .createRetentionPolicy(rpName, dbName, duration, 1, Some(duration))
      .futureValue
      .value shouldEqual 200

    influx.showDatabases().futureValue.value.contains(dbName) shouldEqual true

    influx
      .createSubscription(subName, dbName, rpName, destType, hosts.toSeq)
      .futureValue
      .value shouldEqual 200

    val subscr = influx.showSubscriptionsInfo.futureValue.value.headOption
      .flatMap(_.subscriptions.headOption)
      .get

    subscr.subsName shouldEqual subscription.subsName
    subscr.addresses shouldEqual subscription.addresses
    subscr.destType shouldEqual subscription.destType
    subscr.addresses.toList shouldEqual subscription.addresses.toList
  }

  it should "drop subscription" in {
    influx.dropSubscription(subName, dbName, rpName).futureValue.value shouldEqual 200

    influx.showSubscriptionsInfo.futureValue.value shouldEqual Nil

    influx.dropRetentionPolicy(rpName, dbName).futureValue.value shouldEqual 200

    influx.dropDatabase(dbName).futureValue.value shouldEqual 200
  }
}
