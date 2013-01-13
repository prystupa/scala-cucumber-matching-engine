package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:03 PM
 */

sealed trait OrderBookEvent

case class Trade(buyingBroker: String, sellingBroker: String,
                 price: Double, qty: Double) extends OrderBookEvent

case class RejectedOrder(order: Order) extends OrderBookEvent

case class CancelledOrder(order: Order) extends OrderBookEvent