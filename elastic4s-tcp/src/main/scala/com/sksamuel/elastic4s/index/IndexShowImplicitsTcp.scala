package com.sksamuel.elastic4s.index

import cats.Show
import com.sksamuel.elastic4s.indexes.{CreateIndexDefinition, IndexContentBuilder, IndexDefinition}

trait IndexShowImplicitsTcp {

  implicit object IndexShowTcp extends Show[IndexDefinition] {
    override def show(req: IndexDefinition): String = IndexContentBuilder(req).string()
  }

  implicit object CreateIndexShowTcp extends Show[CreateIndexDefinition] {
    override def show(req: CreateIndexDefinition): String = CreateIndexContentBuilder(req).string()
  }
}
