package com.meituan.chapter1

import java.util

import akka.actor.Actor
import akka.event.Logging

// 1.构造消息
// 通过 case class 来创建不可变消息，一旦在构造函数中设置属性初值后，之后就只能够读 取属性值，不能再进行修改
case class SetRequest(key: String, value: Object)


/**
  * 我们将创建一个 Actor，该 Actor 接收一条消息，将消息中的值存入 Map 以修改 Actor 的内部状态。
  * 这是我们构建分布式数据库的简单的开始。
  */
class AkkademyDb extends Actor {
  val map = new util.HashMap[String, Object]
  val log = Logging(context.system, this)

  // 2.定义 Actor 收到消息后的响应
  // Actor Trait 中的 receive 方法返回一个 Receive，在 Akka 的源代码中， 将 Receive 定义为一个 PartialFunction
  override def receive: PartialFunction[Any, Unit] = {
    // 3.使用模式匹配生成的 PartialFunction 来定义接收到 SetRequest 消息时的响应
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {} value: {}", key, value)
      map.put(key, value)
    // 4.捕捉其他所有未知类型的消息
    case o => log.info("received unknown message: {}", o)
  }
}