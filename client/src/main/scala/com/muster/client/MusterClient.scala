package com.muster.client

import com.muster.core.utils.context.ContextHelper
import com.muster.thriftscala.MusterCacheService.FutureIface
import com.muster.thriftscala.MusterCacheStatus.{KeyNotFound, Ok}
import com.muster.thriftscala._
import com.twitter.finagle.ThriftMux
import com.twitter.finagle.client.Transporter.ConnectTimeout
import com.twitter.finagle.service.FailFastFactory.FailFast
import com.twitter.util.{Duration, Future}
import scala.util.Random


//class ByteCodec[A] {
//  import collection.JavaConversions._
//  import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
//
//
//  def serialize(obj: AnyRef): Array[Byte] = {
//  def serialize(obj: AnyRef): Array[Byte] = {
//    val b: ByteArrayOutputStream = new ByteArrayOutputStream()
//    val o: ObjectOutputStream = new ObjectOutputStream(b)
//    o.writeObject(obj)
//    b.toByteArray().toSeq
//  }
//
//  def deserialize (bytes: Seq[Byte] ) = {
//    val b: ByteArrayInputStream = new ByteArrayInputStream(bytes.toArray)
//    val o: ObjectInputStream = new ObjectInputStream(b)
//    o.readObject()
//  }
//}

object MusterClient {

}

abstract class MusterClientError(msg: String) extends Exception(msg)

case class PutRequestError(msg: String) extends MusterClientError(msg)

case class GetRequestError(msg: String) extends MusterClientError(msg)

case class KeyNotFoundError(msg: String) extends MusterClientError(msg)


class MusterClient(hosts: Seq[String]) {
  assert(hosts.nonEmpty, "No hosts provided")

  private lazy val nodes: Seq[FutureIface] = {
    hosts map { dest =>
      ThriftMux.client
        .configured(FailFast(false))
        .configured(ConnectTimeout(Duration.Top))
        .newIface[MusterCacheService.FutureIface](dest)
    }
  }

  private def nextNode() = {
    Random.shuffle(nodes.toList).head
  }


  def put(key: String, value: String): Future[Unit] = {
    val client = nextNode()
    val context = ContextHelper.newContext
    val responseFuture: Future[MusterCachePutResponse] = client.put(
      MusterCachePutRequest(context, key, value.getBytes.toSeq)
    )
    responseFuture flatMap {
      case MusterCachePutResponse(_, Ok) => Future.Unit
      case other => Future.exception(new PutRequestError(s"Failed put $key -> $value reason: $other"))
    }
  }

  def get(key: String): Future[Option[Seq[Byte]]] = {
    val client = nextNode()
    val context = ContextHelper.newContext
    val responseFuture: Future[MusterCacheGetResponse] = client.get(
      MusterCacheGetRequest(context, key)
    )
    responseFuture flatMap {
      case MusterCacheGetResponse(_, Ok, k, valueOpt) =>
        Future.value(valueOpt)
      case MusterCacheGetResponse(_, KeyNotFound, k, valueOpt) =>
        Future.exception(new KeyNotFoundError(s"Key not found error: $key"))
      case other => Future.exception(new GetRequestError(s"Failed get request with $key reason: $other}"))
    }
  }

}
