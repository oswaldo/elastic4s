package com.sksamuel.elastic4s.index

import com.sksamuel.elastic4s.Executable
import com.sksamuel.elastic4s.indexes.CreateIndexDefinition
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.client.Client

import scala.concurrent.Future

trait CreateIndexExecutables extends IndexShowImplicitsTcp {

  implicit object CreateIndexDefinitionExecutable
    extends Executable[CreateIndexDefinition, CreateIndexResponse, CreateIndexResponse] {
    override def apply(c: Client, t: CreateIndexDefinition): Future[CreateIndexResponse] = {
      val req = CreateIndexBuilder(t)
      injectFuture(c.admin.indices.create(req, _))
    }
  }

  implicit class CreateIndexShowOps(f: CreateIndexDefinition) {
    def show: String = CreateIndexShowTcp.show(f)
  }
}
