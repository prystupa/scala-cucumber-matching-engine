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

trait Side

case object Buy extends Side

case object Sell extends Side

case class LimitOrder(broker: String, qty: Double, side: Side, limit: Double) extends Order {

  def typeString = limit.toString
}
