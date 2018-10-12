package com.meituan.chapter1


import akka.actor.ActorSystem
import akka.util.Timeout
import akka.testkit.TestActorRef
import org.scalatest.{FunSpecLike, Matchers}
import scala.concurrent.duration._

class AkkademyDbSpec extends FunSpecLike with Matchers {

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  describe("akkademyDb") {

    describe("given SetRequest") {
      it("should place key/value into map") {
        // TestActorRef#create 方法，传入创建的 Actor 系统（implicit)
        // TestActorRef 不能展示出Actor异步
        val actorRef = TestActorRef(new AkkademyDb)
        // 传递消息，放入Actor邮箱
        actorRef ! SetRequest("key", "value")
        // 得到指向背后 Actor 实例的引用
        val akkademyDb = actorRef.underlyingActor
        // 确认是否已经将值存入 map 中
        akkademyDb.map.get("key") should equal("value")
      }
    }
  }

}