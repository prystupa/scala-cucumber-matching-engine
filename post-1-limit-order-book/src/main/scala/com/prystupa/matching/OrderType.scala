package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 1/2/13
 * Time: 9:37 PM
 */

trait OrderType {
  def bookDisplay: String

  def price: PriceLevel
}

object OrderType {

  def all(): PartialFunction[Order, OrderType] = {

    case LimitOrder(_, _, _, limit) => new OrderType {
      def bookDisplay: String = limit.toString

      def price: PriceLevel = LimitPrice(limit)
    }
  }
}