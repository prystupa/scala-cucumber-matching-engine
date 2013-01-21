package com.prystupa.matching.step_definitions

import com.prystupa.matching._
import com.prystupa.matching.PegOrder
import com.prystupa.matching.MarketOrder
import com.prystupa.matching.LimitOrder

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/20/13
 * Time: 9:10 PM
 */
trait OrderStepUtils {

  val buyBook: OrderBook
  val sellBook: OrderBook

  protected def bookDisplay(order: Order): String = order match {
    case _: MarketOrder => "MO"
    case LimitOrder(_, _, _, limit) => limit.toString
    case PegOrder(_, side, _) => (side match {
      case Buy => buyBook
      case Sell => sellBook
    }).bestLimit match {
      case Some(limit) => s"Peg($limit)"
      case None => throw new IllegalStateException("Can't have peg order in the book with no best limit")
    }
  }

  protected def parseOrder(broker: String, sideString: String, qty: Double, price: String): Order = {

    val (side, _) = getBook(sideString)
    price match {
      case "MO" => MarketOrder(broker, side, qty)
      case "Peg" => PegOrder(broker, side, qty)
      case _ => LimitOrder(broker, side, qty, price.toDouble)
    }
  }

  protected def getBook(side: String) = side match {
    case "Buy" => (Buy, buyBook)
    case "Sell" => (Sell, sellBook)
  }
}
