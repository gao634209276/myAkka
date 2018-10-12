package com.meituan.chapter2

import java.util

import akka.actor.{Actor, Status}
import akka.event.Logging

// 准备数据库与消息
// 首先，我们要构造几种消息, 由于我们将在通过网络连接的独立应用程序之间远程发送消息，因此需要能够对所有的消息进行序列化
// 我们将在服务器端实现这些消息及其行为，以及用于启动该数据库的 main 函数。
// 需要注意的是，我们将使用“第 1 章:初识 Actor”中的项目，并在这基础上添加本章介绍 的功能，比如返回响应及失败情况的处理。

// Scala 的 case class 是可以被序列化的。

// Set 消息:设置某个键值对，返回状态。
case class SetRequest(key: String, value: Object)

// Get 消息:如果 key 存在，就返回 value
case class GetRequest(key: String)

// Key Not Found 异常消息:如果 key 不存在，就返回该异常
case class KeyNotFoundException(key: String) extends Exception


class AkkademyDb extends Actor {

  val map = new util.HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive = {
    // 如果 Actor 接收到一个 SetRequest，就将键值存储到 map 中
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {} value: {}", key, value)
      map.put(key, value)
      sender() ! Status.Success
    // 如果接收到的是 GetRequest，Actor 就会尝试从 map 中获取结果
    case GetRequest(key) =>
      log.info("received GetRequest - key: {}", key)
      val response: Option[String] = Option(map.get(key).asInstanceOf[String])
      response match {
        // 如果找到 key，就将 value 返回
        case Some(x) => sender() ! x
        // 如果没有找到，就返回一个包含 KeyNotFoundException 的 失败消息
        case None => sender() ! Status.Failure(new KeyNotFoundException(key))
      }
    // 最后接收到未知消息时的行为，会返回一个包含了 ClassNotFoundException 的错误消息
    case o => Status.Failure(new ClassNotFoundException)
  }
}
