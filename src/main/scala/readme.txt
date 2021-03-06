https://blog.csdn.net/liubenlong007/article/details/53782966
https://blog.csdn.net/lovehuangjiaju

1. 重要概念
2. Actor模型
3. Akka架构简介

多核处理器的出现使并发编程（Concurrent Programming）成为开发人员必备的一项技能，许多现代编程语言都致力于解决并发编程问题。
并发编程虽然能够提高程序的性能，但传统并发编程的共享内存通信机制对开发人员的编程技能要求很高，
需要开发人员通过自身的专业编程技能去避免死锁、互斥等待及竞争条件（Race Condition）等，
熟悉Java语言并发编程的读者们对这些问题的理解会比较深刻，这些问题使得并发编程比顺序编程要困难得多。
Scala语言并没有直接使用Java语言提供的并发编程库，而是通过Actor模型来解决Java并发编程中遇到的各种问题，为并发编程提供了更高级的抽象。

1 重要概念
（1）并发和并行
并发和并行从宏观来看，都是为进行多任务运行，但并发（Concurrency）和并行（parallelism）两者之间是有区别的。
并行是指两个或者两个以上任务在同一时刻同时运行;而并发是指两个或两个以上的任务在同一时间段内运行，即一个时间段中有几个任务都处于已启动运行到运行完毕之间，这若干任务在同一CPU上运行但任一个时刻点上只有一个任务运行。

2 Actor模型
在使用Java语言进行并发编程时，需要特别关注共享的数据结构，线程间的资源竞争容易导致死锁等问题，
而Actor模型便是要解决线程和锁带来的问题，Actor是一种基于事件（Event-Based）的轻量级线程，
在使用Actor进行并发编程时只需要关注代码结构，而不需要过分关注数据结构，因此Actor最大限度地减少了数据的共享。
Actor由三个重要部分组成，它们是状态（state），行为（Behavior）和邮箱（Mailbox),
Actor与Actor之间的交互通过消息发送来完成
状态指的是Actor对象的变量信息，它可以是Actor对象中的局部变量、占用的机器资源等，
状态只会根据Actor接受的消息而改变，从而避免并发环境下的死锁等问题；
行为指的是Actor的计算行为逻辑，它通过处理Actor接收的消息而改变Actor状态；
邮箱（mailbox）建立起Actor间的连接，即Actor发送消息后，另外一个Actor将接收的消息放入到邮箱中待后期处理，
邮箱的内部实现是通过队列来实现的，队列可以是有界的（Bounded）也可以是无界的（Unbounded），
有界队列实现的邮箱容量固定，无界队列实现的邮箱容易不受限制。