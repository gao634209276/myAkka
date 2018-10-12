package com.meituan.simple

object Main {

  def main(args: Array[String]): Unit = {
    akka.Main.main(Array[String](classOf[HelloWorld].getName))

  }

  def test(): Unit = {
    import akka.actor.{ActorSystem, Props}

    val system = ActorSystem.create("Hello")
    val a = system.actorOf(Props.create(classOf[HelloWorld]), "helloWorld")
    System.out.println(a.path)
  }
}