package com.sksamuel.elastic4s.indexes

import com.sksamuel.elastic4s.{IndexAndType, IndexAndTypes}

trait IndexApi {

  @deprecated("Elasticsearch 6.0 has deprecated types with the intention of removing them in 7.0. You can continue to use them in existing indexes, but all new indexes must only have a single type. Use the index(indexName) method instead which will default to using a type with the same name as the index.", "6.0")
  def indexInto(index: String, `type`: String): IndexDefinition = indexInto(IndexAndType(index, `type`))

  @deprecated("Elasticsearch 6.0 has deprecated types with the intention of removing them in 7.0. You can continue to use them in existing indexes, but all new indexes must only have a single type. Use the index(indexName) method instead which will default to using a type with the same name as the index.", "6.0")
  def indexInto(indexTypes: IndexAndTypes): IndexDefinition = IndexDefinition(indexTypes)

  def indexInto(index: String): IndexDefinition = IndexDefinition(IndexAndTypes(index))

  @deprecated("Elasticsearch 6.0 has deprecated types with the intention of removing them in 7.0. You can continue to use them in existing indexes, but all new indexes must only have a single type. Use the index(indexName) method instead which will default to using a type with the same name as the index.", "6.0")
  def index(kv: (String, String)): IndexDefinition = IndexDefinition(IndexAndType(kv._1, kv._2))

  def getIndex(index: String, others: String*): GetIndexDefinition = getIndex(index +: others)
  def getIndex(indexes: Seq[String]): GetIndexDefinition = GetIndexDefinition(indexes.mkString(","))
}
