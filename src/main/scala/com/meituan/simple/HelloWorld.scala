package com.meituan.simple

import akka.actor.{Props, UntypedActor}


class HelloWorld extends UntypedActor {
  override def preStart(): Unit = { // create the greeter actor
    val greeter = getContext.actorOf(Props.create(classOf[Greeter]), "greeter")
    // tell it to perform the greeting
    greeter.tell(Greeter.Msg.GREET, getSelf)
  }

  override def onReceive(msg: Any): Unit = {
    if (msg == Greeter.Msg.DONE) { // when the greeter is done, stop this actor and with it the application
      getContext.stop(getSelf)
    }
    else unhandled(msg)
  }
}