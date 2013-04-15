package com.socrata.http
package impl

import java.util.concurrent.{Executor, ExecutionException}

import com.ning.http.client.ListenableFuture

import scala.concurrent.{Promise, Future, ExecutionContext}

private[http] object WrappedFuture {
  def apply[A](underlying: ListenableFuture[A])(implicit executionContext: ExecutionContext with Executor): Future[A] = {
    val promise = Promise[A]()
    underlying.addListener(new Runnable {
      override def run() {
        try {
          promise.success(underlying.get())
        } catch {
          case e: ExecutionException =>
            promise.failure(e.getCause)
          case e: Throwable =>
            promise.failure(e)
        }
      }
    }, executionContext)
    promise.future
  }
}
