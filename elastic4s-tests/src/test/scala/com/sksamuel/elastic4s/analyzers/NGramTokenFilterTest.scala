package com.sksamuel.elastic4s.analyzers

import org.scalatest.{Matchers, WordSpec}

class NGramTokenFilterTest extends WordSpec with TokenFilterDsl with Matchers {

  "NGramTokenFilter builder" should {
    "not set any defaults" in {
      ngramTokenFilter("testy").json.string shouldBe """{"type":"nGram"}"""
    }
    "set min and max ngrams" in {
      ngramTokenFilter("testy").minMaxGrams(3, 4).json.string shouldBe """{"type":"nGram","min_gram":3,"max_gram":4}"""
    }
  }
}
