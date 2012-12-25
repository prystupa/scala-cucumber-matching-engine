package com.prystupa.matching

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/22/12
 * Time: 11:59 AM
 */

trait Order {
  val broker: String
  val qty: Double
  val side: Side

  def typeString: String
}

case class LimitOrder(broker: String, side: Side, qty: Double, limit: Double) extends Order {

  def typeString = limit.toString
}
