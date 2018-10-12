package com.meituan

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object Example_01 extends App {

  //通过扩展Actor并实现receive方法来定义Actor
  class MyActor extends Actor {
    //获取LoggingAdapter，用于日志输出
    val log = Logging(context.system, this)

    //实现receive方法，定义Actor的行为逻辑，返回的是一个偏函数
    def receive = {
      case "test" => log.info("received test")
      case _ => log.info("received unknown message")
    }
  }

  //  val configStr =
  //    s"""
  //       |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
  //       |akka.remote.netty.tcp.hostname = "127.0.0.1"
  //       |akka.remote.netty.tcp.port = "0"
  //       """.stripMargin
  //  val config = ConfigFactory.parseString(configStr)

  //创建ActorSystem对象
  val system = ActorSystem("MyActorSystem")
  //返回ActorSystem的LoggingAdpater
  val systemLog = system.log
  //创建MyActor,指定actor名称为myactor
  val myactor = system.actorOf(Props[MyActor], name = "myactor")

  systemLog.info("准备向myactor发送消息")
  //向myactor发送消息
  myactor ! "test"
  myactor ! 123

  //关闭ActorSystem，停止程序的运行
  system.shutdown()
  //system.terminate()
}