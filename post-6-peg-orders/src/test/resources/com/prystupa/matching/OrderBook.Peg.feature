Feature: Core Pegged Order Functionality

  Scenario Outline: Price and time priority of pegged orders over market and limit orders
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price           |
      | A      | 100 | <Top Limit>     |
      | B      | 100 | <Non-top Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price           |
      | A      | 100 | <Top Limit>     |
      | B      | 100 | <Non-top Limit> |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | C      | 100 | Peg   |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price            |
      | A      | 100 | <Top Limit>      |
      | C      | 100 | Peg(<Top Limit>) |
      | B      | 100 | <Non-top Limit>  |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | D      | 100 | Peg   |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price            |
      | A      | 100 | <Top Limit>      |
      | C      | 100 | Peg(<Top Limit>) |
      | D      | 100 | Peg(<Top Limit>) |
      | B      | 100 | <Non-top Limit>  |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price       |
      | E      | 100 | <Top Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price            |
      | A      | 100 | <Top Limit>      |
      | C      | 100 | Peg(<Top Limit>) |
      | D      | 100 | Peg(<Top Limit>) |
      | E      | 100 | <Top Limit>      |
      | B      | 100 | <Non-top Limit>  |
  Examples:
    | Side | Top Limit | Non-top Limit |
    | Buy  | 10.5      | 10.4          |
    | Sell | 10.5      | 10.6          |

  Scenario Outline: Pegged order starts pegging more aggressive limit order when it enters the book
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price         |
      | A      | 100 | <First Limit> |
      | B      | 100 | Peg           |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price              |
      | A      | 100 | <First Limit>      |
      | B      | 100 | Peg(<First Limit>) |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | C      | 100 | <New Best Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                 |
      | C      | 100 | <New Best Limit>      |
      | B      | 100 | Peg(<New Best Limit>) |
      | A      | 100 | <First Limit>         |
  Examples:
    | Side | First Limit | New Best Limit |
    | Buy  | 10.5        | 10.6           |
    | Sell | 10.5        | 10.4           |