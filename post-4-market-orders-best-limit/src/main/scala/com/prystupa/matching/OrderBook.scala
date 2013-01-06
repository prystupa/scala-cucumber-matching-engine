package com.prystupa.matching


/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 1:33 PM
 */

class OrderBook(side: Side, orderTypes: (Order => OrderType)) {

  private var market: List[Order] = Nil
  private var limit: List[(Double, List[Order])] = Nil
  private val priceOrdering = if (side == Sell) Ordering[Double] else Ordering[Double].reverse

  def add(order: Order) {

    orderTypes(order).price match {

      case MarketPrice => market = market :+ order

      case LimitPrice(level) => {
        def insert(list: List[(Double, List[Order])]): List[(Double, List[Order])] = list match {
          case Nil => List((level, List(order)))
          case (head@(bookLevel, orders)) :: tail => priceOrdering.compare(level, bookLevel) match {
            case 0 => (bookLevel, orders :+ order) :: tail
            case n if n < 0 => (level, List(order)) :: list
            case _ => head :: insert(tail)
          }
        }
        limit = insert(limit)
      }
    }
  }

  def top: Option[Order] = market match {
    case head :: _ => Some(head)
    case _ => limit.headOption.map({
      case (_, orders) => orders.head
    })
  }

  def bestLimit: Option[Double] = limit.headOption.map({
    case (level, _) => level
  })

  def decreaseTopBy(qty: Double) {

    market match {
      case top :: tail => market = if (qty == top.qty) tail else orderTypes(top).decreasedBy(qty) :: tail
      case _ => limit match {
        case ((level, orders) :: tail) => {
          val (top :: rest) = orders
          limit = (qty == top.qty, rest.isEmpty) match {
            case (true, true) => tail
            case (true, false) => (level, rest) :: tail
            case _ => (level, orderTypes(top).decreasedBy(qty) :: rest) :: tail
          }
        }
        case Nil => throw new IllegalStateException()
      }
    }
  }

  def orders(): List[Order] = market ::: limit.flatMap({
    case (_, orders) => orders
  })
}
