package com.muster.core.app

import com.muster.core.MusterSpec
import com.muster.core.utils.context.ContextHelper
import com.muster.thriftscala._
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.client.Transporter.ConnectTimeout
import com.twitter.finagle.service.FailFastFactory.FailFast
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.util.{Await, Duration, Future}

class MusterThriftServiceTest extends MusterSpec {

  val server = new EmbeddedHttpServer(
    twitterServer = new MusterAppServer,
    clientFlags = Map()
  )

  val client = ThriftMux.client
    .configured(FailFast(false))
    .configured(ConnectTimeout(Duration.Top))
    .newIface[MusterCacheService.FutureIface](server.thriftHostAndPort)

  "Server" should {
    "startup" in {
      server.assertHealthy()
    }

    "handle successful put request" in {
      val context = ContextHelper.newContext
      val responseFuture: Future[MusterCachePutResponse] = client.put(
        MusterCachePutRequest(
          context = context,
          key = "test", value = "value".getBytes.toSeq
        )
      )

      val response: MusterCachePutResponse = Await.result(responseFuture)

      response.context.id mustBe context.id
      response.context.datetime mustBe context.datetime
      response.status mustBe MusterCacheStatus.Ok
    }


    "handle successful put and get request" in {
      val testKey = "test"
      val testValue = "value"

      Await.result(client.put(
        MusterCachePutRequest(
          context = ContextHelper.newContext,
          key = "test", value = testValue.getBytes.toSeq
        )
      )).status mustBe MusterCacheStatus.Ok

      val getContext = ContextHelper.newContext
      val responseFuture: Future[MusterCacheGetResponse] = client.get(
        MusterCacheGetRequest(context = getContext, key = testKey)
      )

      val response: MusterCacheGetResponse = Await.result(responseFuture)

      response.context.id mustBe getContext.id
      response.context.datetime mustBe getContext.datetime
      response.value mustBe Some(testValue.getBytes.toSeq)
      response.status mustBe MusterCacheStatus.Ok
    }

    s"handle ${MusterCacheStatus.KeyNotFound} get request" in {
      val testKey = "missing-key"

      val getContext = ContextHelper.newContext
      val responseFuture: Future[MusterCacheGetResponse] = client.get(
        MusterCacheGetRequest(context = getContext, key = testKey)
      )

      val response: MusterCacheGetResponse = Await.result(responseFuture)

      response.context.id mustBe getContext.id
      response.context.datetime mustBe getContext.datetime
      response.status mustBe MusterCacheStatus.KeyNotFound
    }

  }


}
