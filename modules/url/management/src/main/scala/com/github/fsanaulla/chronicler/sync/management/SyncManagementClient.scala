/*
 * Copyright 2017-2019 Faiaz Sanaulla
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fsanaulla.chronicler.sync.management

import com.github.fsanaulla.chronicler.core.alias.ErrorOr
import com.github.fsanaulla.chronicler.core.auth.InfluxCredentials
import com.github.fsanaulla.chronicler.core.management.{ManagementResponseHandler, ManagementClient}
import com.github.fsanaulla.chronicler.core.model.InfluxDBInfo
import com.github.fsanaulla.chronicler.core.typeclasses.{FunctionK, Monad, MonadError}
import com.github.fsanaulla.chronicler.sync.shared.{SyncRequestBuilder, SyncRequestExecutor}
import com.github.fsanaulla.chronicler.sync.shared.{RequestE, ResponseE, SyncQueryBuilder, SyncJsonHandler, tryApply, tryFunctor}
import sttp.client3.{Identity, SttpBackend, TryHttpURLConnectionBackend}
import sttp.model.Uri

import scala.util.Try

final class SyncManagementClient(
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials]
)(implicit val ME: MonadError[Try, Throwable], val FK: FunctionK[Try, Try])
    extends ManagementClient[Try, Try, RequestE[Identity], Uri, String, ResponseE] {

  private val backend: SttpBackend[Try, Any] = TryHttpURLConnectionBackend()
  implicit val qb: SyncQueryBuilder          = new SyncQueryBuilder(host, port)
  implicit val rb: SyncRequestBuilder        = new SyncRequestBuilder(credentials)
  implicit val re: SyncRequestExecutor       = new SyncRequestExecutor(backend)
  implicit val rh: ManagementResponseHandler[Try, ResponseE] = new ManagementResponseHandler(
    new SyncJsonHandler
  )

  override def ping: Try[ErrorOr[InfluxDBInfo]] = {
    val uri  = qb.buildQuery("/ping")
    val req  = rb.get(uri, compress = false)
    val resp = re.execute(req)

    resp.flatMap(rh.pingResult)
  }

  override def close(): Unit = {
    backend.close()
    ()
  }
}
