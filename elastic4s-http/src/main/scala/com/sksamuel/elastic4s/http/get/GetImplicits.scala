package com.sksamuel.elastic4s.http.get

import cats.Show
import com.sksamuel.elastic4s.HitReader
import com.sksamuel.elastic4s.get.{GetDefinition, MultiGetDefinition}
import com.sksamuel.elastic4s.http.{EnumConversions, HttpEntity, HttpExecutable, HttpRequestClient, HttpResponse, NotFound404ResponseHandler, ResponseHandler}
import com.sksamuel.exts.Logging
import org.apache.http.entity.ContentType

import scala.concurrent.Future
import scala.util.{Failure, Try}

case class MultiGetResponse(docs: Seq[GetResponse]) {
  def items: Seq[GetResponse] = docs
  def size: Int = docs.size

  def to[T: HitReader]: IndexedSeq[T] = docs.map(_.to[T]).toIndexedSeq
  def safeTo[T: HitReader]: IndexedSeq[Either[Throwable, T]] = docs.map(_.safeTo[T]).toIndexedSeq
}

trait GetImplicits {

  implicit object MultiGetShow extends Show[MultiGetDefinition] {
    override def show(f: MultiGetDefinition): String = MultiGetBodyBuilder(f).string()
  }

  implicit object MultiGetHttpExecutable extends HttpExecutable[MultiGetDefinition, MultiGetResponse] with Logging {

    override def responseHandler: ResponseHandler[MultiGetResponse] = new NotFound404ResponseHandler[MultiGetResponse] {
      override def handle(response: HttpResponse): Try[MultiGetResponse] = {
        super.handle(response).map { r =>
          r.copy(docs = r.docs.map { doc =>
            doc.copy(fields = Option(doc.fields).getOrElse(Map.empty))
          })
        }
      }
    }

    override def execute(client: HttpRequestClient, request: MultiGetDefinition): Future[HttpResponse] = {
      val body = MultiGetBodyBuilder(request).string()
      val entity = HttpEntity(body, ContentType.APPLICATION_JSON.getMimeType)
      client.async("POST", "/_mget", Map.empty, entity)
    }
  }

  implicit object GetHttpExecutable extends HttpExecutable[GetDefinition, GetResponse] with Logging {

    override def responseHandler: ResponseHandler[GetResponse] = new ResponseHandler[GetResponse] {
      override def handle(response: HttpResponse): Try[GetResponse] = {
        response.statusCode match {
          case 404 | 200 => Try {
            val r = ResponseHandler.fromEntity[GetResponse](response.entity.get)
            r.copy(fields = Option(r.fields).getOrElse(Map.empty))
          }
          case _ => Failure(new RuntimeException("Error"))
        }
      }
    }

    override def execute(client: HttpRequestClient, request: GetDefinition): Future[HttpResponse] = {

      val endpoint = s"/${request.indexAndType.index}/${request.indexAndType.`type`}/${request.id}"

      val params = scala.collection.mutable.Map.empty[String, String]
      request.fetchSource.foreach { context =>
        if (!context.fetchSource)
          params.put("_source", "false")
        else {
          if (context.includes.nonEmpty) {
            params.put("_source_include", context.includes.mkString(","))
          }
          if (context.excludes.nonEmpty) {
            params.put("_source_exclude", context.excludes.mkString(","))
          }
        }
      }
      if (request.storedFields.nonEmpty) {
        params.put("stored_fields", request.storedFields.mkString(","))
      }
      request.parent.foreach(params.put("parent", _))
      request.routing.foreach(params.put("routing", _))
      request.preference.foreach(params.put("preference", _))
      request.refresh.map(_.toString).foreach(params.put("refresh", _))
      request.realtime.map(_.toString).foreach(params.put("realtime", _))
      request.version.map(_.toString).foreach(params.put("version", _))
      request.versionType.map(EnumConversions.versionType).foreach(params.put("versionType", _))

      client.async("GET", endpoint, params.toMap)
    }
  }
}
