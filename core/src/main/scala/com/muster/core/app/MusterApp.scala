package com.muster.core.app

import com.google.inject.Provides
import com.muster.thriftscala._
import com.twitter.finagle.{ListeningServer, ThriftMux}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.utils.PortUtils
import com.twitter.inject.{Logging, TwitterModule}
import com.twitter.util.Future
import java.net.InetSocketAddress
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}
import javax.inject.Inject


object MusterApp extends MusterAppServer

class MusterAppServer extends HttpServer {

  val thriftServicePort = flag("thrift.port", new InetSocketAddress(0), "Thrift service port")

  var thriftService: ListeningServer = _

  override def modules = Seq(MusterModule)

  override protected def postStartup(): Unit = {
    super.postStartup()

    thriftService = ThriftMux.serveIface(
      thriftServicePort(),
      injector.instance[MusterThriftService]
    )

    closeOnExit(thriftService)
  }

  override def thriftPort: Option[Int] = Some(PortUtils.getPort(thriftService))
}

object MusterModule extends MusterModule

class MusterModule extends TwitterModule {
  @Provides
  def providesMusterThriftService(): MusterThriftService =
    new MusterThriftService()
}

class MusterThriftService @Inject()()
  extends MusterCacheService.FutureIface
  with Logging {

  val store: ConcurrentMap[String, Seq[Byte]] = new ConcurrentHashMap[String, Seq[Byte]]()

  override def put(request: MusterCachePutRequest): Future[MusterCachePutResponse] = {

    store.put(request.key, request.value)

    Future.value(
      MusterCachePutResponse(context = request.context, status = MusterCacheStatus.Ok)
    )
  }

  override def get(request: MusterCacheGetRequest): Future[MusterCacheGetResponse] = {

    val (resultStatus, resultOpt) = if (store.containsKey(request.key)) {
      (MusterCacheStatus.Ok, Some(store.get(request.key)))
    } else {
      (MusterCacheStatus.KeyNotFound, None)
    }

    Future.value(
      MusterCacheGetResponse(context = request.context, status = resultStatus, key = request.key, value = resultOpt)
    )
  }

}

