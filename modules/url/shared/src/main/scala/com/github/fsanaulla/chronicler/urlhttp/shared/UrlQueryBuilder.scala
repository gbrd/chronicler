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

package com.github.fsanaulla.chronicler.urlhttp.shared

import com.github.fsanaulla.chronicler.core.components.QueryBuilder
import com.github.fsanaulla.chronicler.core.model.InfluxCredentials
import sttp.model.Uri
import sttp.model.Uri.QuerySegment
import sttp.model.Uri.QuerySegment.KeyValue

import scala.annotation.tailrec

private[urlhttp] class UrlQueryBuilder(
    host: String,
    port: Int,
    credentials: Option[InfluxCredentials]
) extends QueryBuilder[Uri](credentials) {

  // todo: move to safer version
  override def buildQuery(path: String): Uri =
    Uri.unsafeApply(host = host, port).withWholePath(path)

  override def buildQuery(path: String, queryParams: List[(String, String)]): Uri = {
    val params = queryParams.map {
      case (k, v) => KeyValue(k, v, valueEncoding = Uri.QuerySegmentEncoding.All)
    }

    @tailrec
    def addQueryParam(u: Uri, lst: Seq[QuerySegment]): Uri = {
      lst match {
        case Nil       => u
        case h :: tail => addQueryParam(u.addQuerySegment(h), tail)
      }
    }

    addQueryParam(buildQuery(path), params)
  }
}
