package com.github.fsanaulla.chronicler.example.ahc.io

import com.github.fsanaulla.chronicler.ahc.io.InfluxIO
import com.github.fsanaulla.chronicler.core.model.{InfluxFormatter, Point}
import com.github.fsanaulla.chronicler.macros.Influx
import com.github.fsanaulla.chronicler.macros.annotations.{field, tag}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    final case class Test(@tag name: String, @field age: Int)

    // generate formatter at compile-time
    implicit val fmt: InfluxFormatter[Test] = Influx.formatter[Test]

    val t = Test("f", 1)
    val host = args.headOption.getOrElse("localhost")
    val influx = InfluxIO(host)
    val meas = influx.measurement[Test]("db", "cpu")

    val result = for {
      // write record to Influx
      writeResult <- meas.write(t) if writeResult.isSuccess
      // retrieve written record from Influx
      queryResult <- meas.read("SELECT * FROM cpu")
      // close client
      _ = influx.close()

    } yield queryResult.queryResult

    Await.result(result, Duration.Inf).foreach(println)
  }
}
