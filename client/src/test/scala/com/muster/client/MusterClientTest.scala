package com.muster.client

import com.muster.core.MusterSpec
import com.muster.core.app.MusterAppServer
import com.twitter.finatra.http.test.EmbeddedHttpServer
import com.twitter.util.{Await, Future}


class MusterClientTest extends MusterSpec {
  val server = new EmbeddedHttpServer(
    twitterServer = new MusterAppServer,
    clientFlags = Map()
  )

  val client = new MusterClient(Seq(server.thriftHostAndPort))

  "Server" should {
    "startup" in {
      server.assertHealthy()
    }

    "handle successful put request" in {
      val testKey = "key"
      val testValue = "value"
      val response: Future[Unit] = client.put(testKey, testValue)
      response.onFailure(_ => assert(false))
    }

    "handle successful put and get request" in {
      val testKey = "key"
      val testValue = "value"

      val putResponse: Future[Unit] = client.put(testKey, testValue)
      putResponse.onFailure(_ => assert(false))

      val getResponse: Future[Option[Seq[Byte]]] = client.get(testKey)
      getResponse.onFailure(_ => assert(false))
      getResponse.onSuccess(_ mustBe Some(testValue.getBytes.toSeq))
    }

    s"handle $KeyNotFoundError on get request" in {
      val testKey = "nonExistentKey"
      val getResponse: Future[Option[Seq[Byte]]] = client.get(testKey)
      intercept[KeyNotFoundError] {
        Await.result(getResponse)
      }
    }

  }
}
