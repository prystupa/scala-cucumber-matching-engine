Feature: Core Pegged Order Functionality

  Scenario Outline: Adding pegged order to a book with no best limit to peg
  If a book is empty then no best limit is available to peg, so we expect the order to be rejected
  If a book only has market orders then best limit is also undefined, so we also expect the order to be rejected
    Given the "<Side>" order book looks like:
      | Broker | Qty | Price |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | A      | 100 | Peg   |
    Then the following "<Side>" orders are rejected:
      | Broker | Qty | Price |
      | A      | 100 | Peg   |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | B      | 100 | MO    |
      | C      | 100 | Peg   |
    Then the following "<Side>" orders are rejected:
      | Broker | Qty | Price |
      | C      | 100 | Peg   |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | MO    |

  Examples:
    | Side |
    | Buy  |
    | Sell |

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


  Scenario Outline: Pegged order starts pegging more aggressive limit order when the latter enters the book
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


  Scenario Outline: Pegged order starts pegging less aggressive limit order when more aggressive leaves the book
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price                   |
      | A      | 100 | <More Aggressive Limit> |
      | B      | 100 | <Less Aggressive Limit> |
      | C      | 100 | Peg                     |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                        |
      | A      | 100 | <More Aggressive Limit>      |
      | C      | 100 | Peg(<More Aggressive Limit>) |
      | B      | 100 | <Less Aggressive Limit>      |
    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                        |
      | B      | 100 | <Less Aggressive Limit>      |
      | C      | 100 | Peg(<Less Aggressive Limit>) |
  Examples:
    | Side | More Aggressive Limit | Less Aggressive Limit |
    | Buy  | 10.6                  | 10.5                  |
    | Sell | 10.4                  | 10.5                  |


  Scenario Outline: Pegged order is automatically canceled if the best limit of the order book disappears
    Given the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | Peg   |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price     |
      | A      | 100 | 10.5      |
      | B      | 100 | Peg(10.5) |

    When the top order goes away from the "<Side>" book
    Then the following "<Side>" orders are cancelled:
      | Broker | Qty | Price |
      | B      | 100 | Peg   |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |

  Examples:
    | Side |
    | Buy  |
    | Sell |
