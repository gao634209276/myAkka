package com.meituan.chapter2

import akka.actor.{ActorRef, ActorSystem, Props}

// 我们从来都不会得到 Actor 的 实例，从不调用 Actor 的方法，也不直接改变 Actor 的状态，反之，只向 Actor 发送消息
// 我们也不会直接访问 Actor 的成员，而是通过消息传递来请求获取关于 Actor 状态的信息。
// 使用消息传递代替直接方法调用可以加强封装性 （面向对象思想）
// 我们只需要一种 机制来支持向 Actor 发送消息并接收响应。
// 在 Akka 中，这个指向 Actor 实例的引用叫做 ActorRef
// ActorRef 是一个无类型的引 用，将其指向的 Actor 封装起来，提供了更高层的抽象，并且给用户提供了一种与 Actor 进行通信的机制。
object ActorCreate {


  def main(args: Array[String]): Unit = {

    // 方式1
    // actorOf 方法会生成 一个新的 Actor，并返回指向该 Actor 的引用。
    val actorSystem = ActorSystem.create("actorDemo")
    val actor1: ActorRef = actorSystem.actorOf(Props(classOf[ScalaPongActor]))

    // 方式2
    // 我们将所有构造 函数的参数传给一个 Props 的实例，Props 允许我们传入 Actor 的类型以及一个变长的参 数列表。
    // 如：Props(classOf[PongActor], arg1, arg2, argn)
    // 如果 Actor 的构造函数有参数，那么推荐的做法是通过一个工厂方法来创建 Props。参考：object ScalaPongActor
    // 然后就可以使用 Props 的工厂方法来创建 Actor:
    val actor2: ActorRef = actorSystem.actorOf(ScalaPongActor.props("PongFoo"))

    // 方式3
    // 还有另 一种方法可以获取指向 Actor 的引用:actorSelection
    // 我们可以通过 ActorRef.path 来查看该路径，返回如：akka://default/user/BruceWillis
    // 它甚至可以指向使用 akka.tcp 协议的远程 Actor。akka.tcp://my-sys@remotehost:5678/user/CharlieChaplin
    val selection = actorSystem.actorSelection("akka.tcp://")
  }

}

