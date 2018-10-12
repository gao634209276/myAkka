package com.meituan.simple

import akka.actor.UntypedActor


object Greeter {

  object Msg extends Enumeration {
    type Msg = Value
    val GREET, DONE = Value
  }

}

class Greeter extends UntypedActor {
  @throws[InterruptedException]
  override def onReceive(msg: Any): Unit = {
    if (msg == Greeter.Msg.GREET) {
      System.out.println("Hello World!")
      Thread.sleep(1000)
      getSender.tell(Greeter.Msg.DONE, getSelf)
    }
    else unhandled(msg)
  }
}