package com.meituan.chapter2

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

// 使用事件驱动的模型，我们需要在代码中用不同的方法来表示结果。
// 我们需要 用一个占位符来表示最终将会返回的结果:Future。然后注册事件完成时应该进行的操作。
// 我们注册的代码会在 Future 占位符的值真正返回可用时被调用执行。
// “事件驱动”这个术语正是描述了这种方法:在发生某些特定事件时，就执行某些对应的代码。
// 尽管接下来的测试用例确实引入了异步 API，但是测试用例仍然会因为等待结果而 阻塞。
class ScalaAskExamplesTest extends FunSpecLike with Matchers {

  val system = ActorSystem()
  // 创建一个隐式的 Timeout(注意到我们需要引入 scala. concurrent.duration，然后把一个时间长度传递给 Timeout)
  implicit val timeout = Timeout(5 seconds)
  val pongActor = system.actorOf(Props(classOf[ScalaPongActor]))

  describe("Pong actor") {
    it("should respond with Pong") {

      // 这里异步请求会使用到 implicit timeout
      // 要完成 ? 这一操作，我们需要引入 akka.pattern.ask
      // 该调用会返回一个占位符，也就是一个 Future，表示 Actor 返回的响应
      val future = pongActor ? "Ping"

      // 最后，我们想在真正接收到结果之前阻塞测试线程
      // 我们使用 Await.result，并传入 Future 和一个超时参数
      // Actor 的返回值是没有类型的，因此我们接收到的结果是 Future[AnyRef]
      // 所以应调 用 future.mapTo[String]将 Future 的类型转换成我们需要的结果类型
      // 在这个测试用例中，我们休眠/阻塞了调用 Await.result 的测试线 程，这样就能同步地得到 Future 的结果
      // 注：不要在非测试代码中休眠或阻塞线程
      val result = Await.result(future.mapTo[String], 1 second)
      assert(result == "Pong")
    }
    it("should fail on unknown message") {
      val future = pongActor ? "unknown"
      intercept[Exception] {
        Await.result(future.mapTo[String], 1 second)
      }
    }
  }


}