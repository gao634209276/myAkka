package com.meituan.chapter2

// Actor 基类是基本的 Scala Actor API，非常简单，并且符合 Scala 语言的特性。
import akka.actor.{Actor, Props, Status}

/**
  * 先介绍 Actor 最基 本的特性，理解 Actor 的基本结构和方法。
  * 为了展示最简单的可能情况，我们将在这个例子中构建一个简单的 Actor，
  * 这个 Actor 接收一个字符串“Ping”，返回字符串“Pong” 作为响应。
  */
class ScalaPongActor extends Actor {

  // 重写基类的 receive 方法。并且返回一个 PartialFunction。
  // 要注意的是，receive 方法的返回类型是 Receive。
  // Receive 只不过是定义的一种类型，表示 scala.PartialFunction[scala.Any, scala.Unit]。
  override def receive: Receive = {

    // 向 sender()返回消息
    // 通过 sender()方法获取发送者的 ActorRef，使用 ! 调用tell方法，向发送方发送响应消息
    // 在 tell 方法“!”的方法签名中，有一个隐式的 ActorRef 参数，消息发送者是隐式传入的
    case "Ping" => sender() ! "Pong"
    case _ =>
      sender() ! Status.Failure(new Exception("unknown message"))
  }
}

object ScalaPongActor {
  def props(response: String): Props = {
    Props(classOf[ScalaPongActor], response)
  }
}