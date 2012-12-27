Feature: Core Market Order Functionality for Price and Time Priority

  Scenario Outline: Price and time priority of market orders over limit orders
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | B      | 100 | MO    |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | MO    |
      | A      | 100 | 10.5  |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | C      | 100 | 10.5  |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | MO    |
      | A      | 100 | 10.5  |
      | C      | 100 | 10.5  |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | D      | 100 | MO    |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | MO    |
      | D      | 100 | MO    |
      | A      | 100 | 10.5  |
      | C      | 100 | 10.5  |
  Examples:
    | Side |
    | Buy  |
    | Sell |
