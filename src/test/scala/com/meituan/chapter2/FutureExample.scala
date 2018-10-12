package com.meituan.chapter2

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._


// 理解 Future 和 Promise
// 现代化的 Future 隐式地处理了两种情况:失败与延迟
// 要了解如何把阻塞式 IO 转 化成非阻塞式 IO，我们必须学习一些不同的表示失败处理和延迟处理的抽象概念
// Future——在类型中表达失败与延迟
class FutureExample extends FunSpecLike with Matchers {
  val system = ActorSystem()
  // 创建一个隐式的 Timeout(注意到我们需要引入 scala. concurrent.duration，然后把一个时间长度传递给 Timeout)
  implicit val timeout = Timeout(5 seconds)
  val pongActor = system.actorOf(Props(classOf[ScalaPongActor]))


  // 首先定义一个简单的方法，去除冗余，稍稍增加示例的可读性:
  def askPong(message: String): Future[String] = (pongActor ? message).mapTo[String]

  // 接着使用运行在多线程上的异步操作，因此需要引入隐式的 ExecutionContext
  // 可以创建如下的测试用例并进行实验:
  // 这个测试并没有进行任何断言，但是已经展示了真正的异步行为
  // 我们可以通过观察运行效果来确认异步操作是否成功(在这个测试用例中，我们希望打印到控制台)
  // 如果希望事件异步发生的话，我们可能时不时地需要将测试线程休眠
  // 和阻塞一样，在测 试中休眠线程是没问题的，不过在任何时候都不应该在真正的代码中休眠线程
  describe("FutureExamples") {
    import scala.concurrent.ExecutionContext.Implicits.global
    it("should print to console") {

      // 剖析 Future
      //////////////////////////////////////////////////////////////
      // Future[T]/CompletableFuture<T>成功时会返回一个类型为 T 的值，失败时则会返回 Throwable
      // 我们将分别学习如何处理这两种情况(成功与失败)，以及如何将 Future 的 值转换成有用的结果

      // 一、成功的情况处理
      // 1.对返回结果执行代码
      // 如：将事件记录到日志
      (pongActor ? "Ping").onSuccess({
        case x: String => println("replied with: " + x)
      })

      // 2.对返回结果进行转换
      // 最常见的一种用例就是在处理响应之前先异步地对其进行转换
      // 例子中操作会返回一个新的 Future，包含 Char 类型，
      // 我们可以在对返回结果进行转换后将新得到的 Future 再传递给其他方法，做进一步处理
      askPong("Ping").map(x => x.charAt(0))

      // 3.对返回结果进行异步转换
      // 如果需要进行异步调用，那么首先要对返回结果进行另一个异步调用，这样代码就 会看上去有一点乱
      val futureFuture: Future[Future[String]] = askPong("Ping").map(x => {
        askPong(x)
      })
      // 不过这样一来，结果就会嵌套在两层 Future 中了。
      // 这种情况是 很难处理的，要将结果扁平化，使得结果只在一个 Future 中，
      // 我们需要的是一个 Future[String]/CompletionStage[String]。
      // 有很多方法都可以用来做这样的链式异步操作。
      val fs: Future[String] = askPong("Ping").flatMap(x => askPong("Ping"))
      // 我们可以继续像这样把异步操作连接到一起。这是一种进行流数据处理的 很强大的方法。
      // 我们可以向一个远程服务发起调用，然后使用得到的结果向另一个服 务发起调用。


      // 二、失败情况的处理
      // 1.在失败情况下执行代码
      askPong("causeError").onFailure {
        case e: Exception => println("Got exception")
      }

      // 2.从失败中恢复
      // 在发生错误的时候我们仍然想要使用某个结果值
      // 如果想要从错误中恢 复的话，可以对该 Future 进行转换，使之包含一个成功的结果值
      // Scala 中，有一个 recover 方法可以将 Throwable转换为一个可用的值
      // 同样地，recover 方法也接受一个 PartialFunction 作为参数，所以我们可以对异常的类型进行模式匹配
      val ff = askPong("causeError").recover {
        case t: Exception => "default"
      }

      // 3.异步地从失败中恢复
      // 我们经常需要在发生错误时使用另一个异步方法来恢复，下面是两个用例
      // 3.1重试某个失败的操作
      // 3.2没有命中缓存时，需要调用另一个服务的操作
      // 我们需要分两步来完成这一操作。首先，检查 exception 是否为 null
      // 如果为 null， 就返回包含结果的 Future，否则返回重试的 Future。
      // 接着，调用 thenCompose 将 CompletionStage[CompletionStage[String]]扁平化。
      // Scala 中，recoverWith 类似专门用于错误情况的 flatMap
      askPong("causeError").recoverWith({
        case t: Exception => askPong("Ping")
      })

      // 构造 Future
      //////////////////////////////////////////////////////////////
      // 很多时候，我们需要执行多个操作，而且可能想要在代码库的不同位置来执行这些 操作。
      // 之前介绍到的每个方法调用都会返回一个新的 Future，而我们又可以对这个新的 Future 执行其他操作。

      // 1.链式操作
      // 我们已经介绍了 Future 的基本使用方法。
      // 应用函数式风格来处理延迟和失败的好处 之一就是可以把多个操作组合起来，而在组合的过程中无需处理异常。
      // 我们可以把注意力放在成功的情况上，在链式操作的结尾再收集错误。
      // 之前介绍的每个用于结果转换的方法都会返回一个新的 Future，可以处理这个 Future， 也可以将其与更多操作链接到一起。
      // 总结一下，执行多个操作时，我们最后使用一个恢复函数来处理所有可能发生的错 误。
      // 可以用我们想要的任何顺序来组合这些函数(combinators)来完成我们需要完成的 工作。
      val f = askPong("Ping")
        .flatMap(x => askPong("Ping" + x))
        //.recover({ case Exception => "There was an error" })
      // 在上面的例子中，我们得到了一个 Future，
      // 然后调用 flatMap，在第一个操作完成时异步地发起另一个调用。
      // 接着，在发生错误时，我们使用一个 String 值来 恢复错误，保证 Future 能够返回成功。
      // 在执行操作链中的任一操作时发生的错误都可以作为链的末端发生的错误来处理。
      // 这样就形成了一个很有效的操作管道，无论是哪个操作导致了错误，都可以在最后来处 理异常。
      // 我们可以集中注意力描述成功的情况，无需在链的中间做额外的错误检查。可 以在最后单独处理错误。

      // 2.组合 Future
      // 我们经常需要访问执行的多个 Future。同样有很多方法可以用来处理这些情况。
      // 在 Scala 中，也可以使用 for 推导式将多个 Future 组合起来。
      // 我们能够像处理任何其 他集合一样，解析出两个 Future 的结果并对它们进行处理。
      // (要注意的是，这只不过是 flatMap 的一个“语法糖”:相比于 flatMap，我更喜欢这个语法。)
      val f1 = Future {
        4
      }
      val f2 = Future {
        5
      }
      val futureAddition: Future[Int] = for (
        res1 <- f1;
        res2 <- f2
      ) yield res1 + res2
      // 这个例子展示了一种处理多个不同类型 Future 的机制。
      // 通过这种方法，可以并行地 执行任务，同时处理多个请求，更快地将响应返回给用户。
      // 这种对并行的使用可以帮助 我们提高系统的响应速度。

      // 3.处理 Future 列表
      // 如果想要对集合中的每个元素执行异步方法，那么可以使用 Future 列表。
      // 例如 ，在 Scala 中，如果我们有一个消息列表，对于列表中的每个消息，向 PongActor 发送查询，最后会得到如下的一个 Future 列表:
      val listOfFutures: List[Future[String]] = List("Pong", "Pong", "failed").map(x => askPong(x))
      // 对 Future 列表的处理并不容易。
      // 我们希望得到的是一个结果列表，也就是要反转一下类型，把 List[Future]转换成 Future[List]。
      // Future 的 sequence 方法就是用来完成这一工作的:
      val futureOfList: Future[List[String]] = Future.sequence(listOfFutures)
      // 现在我们就有了一个可以使用的类型。
      // 例如，如果在 futureOfList 上调用 map 方法的话，就可以得到一个 List[String]，这就是我们想要的结果类型。
      // 不过这里有一个问题。
      // 一旦 Future 列表中的任何一个 Future 返回失败，那么 sequence 生成的 Future 也会返回失 败。
      // 如果不希望这种情况发生，想要得到一些成功的结果值，那么可以在执行 sequence 之前将返回失败的 Future 逐一恢复:
      //Future.sequence(listOfFutures.map(future => future.recover { case Exception => ""
      //}))

      Thread.sleep(100)
    }
  }
}
