package com.github.fsanaulla.chronicler.akka.io

import akka.stream.scaladsl.Source
import com.github.fsanaulla.chronicler.akka.shared.handlers.{
  AkkaQueryBuilder,
  AkkaRequestExecutor,
  AkkaResponseHandler
}
import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.api.MeasurementApi
import com.github.fsanaulla.chronicler.core.components.BodyBuilder
import com.github.fsanaulla.chronicler.core.enums.{Epoch, Epochs}
import com.github.fsanaulla.chronicler.core.model.{Failable, Functor, InfluxReader}
import com.softwaremill.sttp.{Response, Uri}
import org.typelevel.jawn.ast.JValue

import scala.concurrent.Future
import scala.reflect.ClassTag

final class AkkaMeasurementApi[T: ClassTag](
    dbName: String,
    measurementName: String,
    gzipped: Boolean
  )(implicit qb: AkkaQueryBuilder,
    bd: BodyBuilder[String],
    re: AkkaRequestExecutor,
    rh: AkkaResponseHandler,
    F: Functor[Future],
    FA: Failable[Future])
  extends MeasurementApi[Future, Response[JValue], Uri, String, T](dbName, measurementName, gzipped) {

  def readChunked(
      query: String,
      epoch: Epoch = Epochs.None,
      pretty: Boolean = false,
      chunkSize: Int
    )(implicit rd: InfluxReader[T]
    ): Future[ErrorOr[Source[ErrorOr[Array[T]], Any]]] = {
    val uri = chunkedQuery(dbName, query, epoch, pretty, chunkSize)
    F.map(re.getStream(uri))(rh.queryChunkedResult[T])
  }
}
