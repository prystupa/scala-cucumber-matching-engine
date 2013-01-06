package com.prystupa.matching.step_definitions

import scala.collection.JavaConversions._

import cucumber.api.java.en.{Given, When, Then}
import cucumber.api.DataTable
import com.prystupa.matching._
import org.scalatest.matchers.ShouldMatchers

/**
 * Created with IntelliJ IDEA.
 * User: eprystupa
 * Date: 12/24/12
 * Time: 8:46 PM
 */

class MatchingEngineSteps extends ShouldMatchers {

  val orderTypes = OrderType.all()
  val buyBook: OrderBook = new OrderBook(Buy, orderTypes)
  val sellBook: OrderBook = new OrderBook(Sell, orderTypes)
  val matchingEngine = new MatchingEngine(buyBook, sellBook, orderTypes)

  var actualTrades = List[Trade]()

  events {
    case trade: Trade => actualTrades = trade :: actualTrades
  }


  @When("^the following orders are submitted in this order:$")
  def the_following_orders_are_submitted_in_this_order(orders: java.util.List[OrderRow]) {

    orders.toList.foreach(o => matchingEngine.acceptOrder(o.price match {
      case "MO" => MarketOrder(o.broker, parseSide(o.side), o.qty)
      case _ => LimitOrder(o.broker, parseSide(o.side), o.qty, o.price.toDouble)
    }))
  }

  @Then("^market order book looks like:$")
  def market_order_book_looks_like(book: DataTable) {

    val (buyOrders, sellOrders) = parseExpectedBooks(book)

    buyBook.orders().map(o => BookRow(Buy, o.broker, o.qty, orderTypes(o).bookDisplay)) should equal(buyOrders)
    sellBook.orders().map(o => BookRow(Sell, o.broker, o.qty, orderTypes(o).bookDisplay)) should equal(sellOrders)
  }

  @Then("^the following trades are generated:$")
  def the_following_trades_are_generated(trades: java.util.List[Trade]) {

    actualTrades.reverse should equal(trades.toList)
    actualTrades = Nil
  }

  @Then("^no trades are generated$")
  def no_trades_are_generated() {

    actualTrades should equal(Nil)
  }

  @Given("^the reference price is set to \"([^\"]*)\"$")
  def the_reference_price_is_set_to(price: Double) {

    matchingEngine.referencePrice = price
  }

  @Then("^the reference price is reported as \"([^\"]*)\"$")
  def the_reference_price_is_reported_as(price: Double) {

    matchingEngine.referencePrice should equal(price)
  }


  private def events(handler: PartialFunction[OrderBookEvent, Unit]) {
    matchingEngine.subscribe(new matchingEngine.Sub {
      def notify(pub: matchingEngine.Pub, event: OrderBookEvent) {
        handler(event)
      }
    })
  }

  private def parseExpectedBooks(book: DataTable): (List[BookRow], List[BookRow]) = {
    def buildOrders(orders: List[List[String]], side: Side) = {
      orders.filterNot(_.forall(_.isEmpty)).map(order => {
        val (broker :: qty :: price :: Nil) = order
        BookRow(side, broker, qty.toDouble, price)
      })
    }

    val orders = book.raw().toList.drop(1).map(_.toList)
    val buy = orders.map(_.take(3))
    val sell = orders.map(_.drop(3).reverse)
    (buildOrders(buy, Buy), buildOrders(sell, Sell))
  }

  private def parseSide(s: String): Side = s.toLowerCase match {
    case "buy" => Buy
    case "sell" => Sell
  }

  private case class OrderRow(broker: String, side: String, qty: Double, price: String)

  private case class BookRow(side: Side, broker: String, qty: Double, price: String)

}
