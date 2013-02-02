Feature: Core Pegged with Limit Order Functionality

  Scenario Outline: Adding pegged with limit price order to a book with no best limit to peg
  If a book is empty then no best limit is available to peg, so we expect the order to be rejected
  If a book only has market orders then best limit is also undefined, so we also expect the order to be rejected
    Given the "<Side>" order book looks like:
      | Broker | Qty | Price |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price     |
      | A      | 100 | Peg[10.5] |
    Then the following "<Side>" orders are rejected:
      | Broker | Qty | Price     |
      | A      | 100 | Peg[10.5] |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price     |
      | B      | 100 | MO        |
      | C      | 100 | Peg[10.5] |
    Then the following "<Side>" orders are rejected:
      | Broker | Qty | Price     |
      | C      | 100 | Peg[10.5] |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | MO    |

  Examples:
    | Side |
    | Buy  |
    | Sell |

  Scenario Outline: Price and time priority of pegged with limit orders over market and limit orders
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price           |
      | A      | 100 | <Top Limit>     |
      | B      | 100 | <Non-top Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price           |
      | A      | 100 | <Top Limit>     |
      | B      | 100 | <Non-top Limit> |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | C      | 100 | Peg[<Peg Limit>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                         |
      | A      | 100 | <Top Limit>                   |
      | C      | 100 | Peg(<Top Limit>)[<Peg Limit>] |
      | B      | 100 | <Non-top Limit>               |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | D      | 100 | Peg[<Peg Limit>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                         |
      | A      | 100 | <Top Limit>                   |
      | C      | 100 | Peg(<Top Limit>)[<Peg Limit>] |
      | D      | 100 | Peg(<Top Limit>)[<Peg Limit>] |
      | B      | 100 | <Non-top Limit>               |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price       |
      | E      | 100 | <Top Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                         |
      | A      | 100 | <Top Limit>                   |
      | C      | 100 | Peg(<Top Limit>)[<Peg Limit>] |
      | D      | 100 | Peg(<Top Limit>)[<Peg Limit>] |
      | E      | 100 | <Top Limit>                   |
      | B      | 100 | <Non-top Limit>               |
  Examples:
    | Side | Top Limit | Non-top Limit | Peg Limit |
    | Buy  | 10.5      | 10.4          | 10.6      |
    | Sell | 10.5      | 10.6          | 10.4      |

  Scenario Outline: Pegged order with limit starts pegging more aggressive limit order when the latter enters the book
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | A      | 100 | <First Limit>    |
      | B      | 100 | Peg[<Peg Limit>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                           |
      | A      | 100 | <First Limit>                   |
      | B      | 100 | Peg(<First Limit>)[<Peg Limit>] |
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | C      | 100 | <New Best Limit> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                              |
      | C      | 100 | <New Best Limit>                   |
      | B      | 100 | Peg(<New Best Limit>)[<Peg Limit>] |
      | A      | 100 | <First Limit>                      |
  Examples:
    | Side | First Limit | New Best Limit | Peg Limit |
    | Buy  | 10.5        | 10.6           | 10.7      |
    | Sell | 10.5        | 10.4           | 10.3      |

  Scenario Outline: Pegged order with limit starts pegging less aggressive limit order when more aggressive leaves the book
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price                   |
      | A      | 100 | <More Aggressive Limit> |
      | B      | 100 | <Less Aggressive Limit> |
      | C      | 100 | Peg[<Peg Limit>]        |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                                     |
      | A      | 100 | <More Aggressive Limit>                   |
      | C      | 100 | Peg(<More Aggressive Limit>)[<Peg Limit>] |
      | B      | 100 | <Less Aggressive Limit>                   |
    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                                     |
      | B      | 100 | <Less Aggressive Limit>                   |
      | C      | 100 | Peg(<Less Aggressive Limit>)[<Peg Limit>] |
  Examples:
    | Side | More Aggressive Limit | Less Aggressive Limit | Peg Limit |
    | Buy  | 10.6                  | 10.5                  | 10.7      |
    | Sell | 10.4                  | 10.5                  | 10.3      |

  Scenario Outline: Pegged order with limit is automatically canceled if the best limit of the order book disappears
    Given the following orders are added to the "<Side>" book:
      | Broker | Qty | Price            |
      | A      | 100 | <Limit>          |
      | B      | 100 | Peg[<Peg Limit>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price                     |
      | A      | 100 | <Limit>                   |
      | B      | 100 | Peg(<Limit>)[<Peg Limit>] |

    When the top order goes away from the "<Side>" book
    Then the following "<Side>" orders are cancelled:
      | Broker | Qty | Price            |
      | B      | 100 | Peg[<Peg Limit>] |
    And the "<Side>" order book looks like:
      | Broker | Qty | Price |

  Examples:
    | Side | Limit | Peg Limit |
    | Buy  | 10.5  | 10.6      |
    | Sell | 10.5  | 10.4      |


  Scenario Outline: Pegged order can't track best limit because it is too aggressive
    Given the following orders are added to the "<Side>" book:
      | Broker | Qty | Price    |
      | A      | 100 | <Best>   |
      | B      | 100 | <Better> |
      | C      | 100 | <Worse>  |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price      |
      | D      | 100 | Peg[<Peg>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | A      | 100 | <Best>            |
      | B      | 100 | <Better>          |
      | D      | 100 | Peg(<Peg>)[<Peg>] |
      | C      | 100 | <Worse>           |

  Examples:
    | Side | Best | Better | Peg  | Worse |
    | Buy  | 10.8 | 10.7   | 10.6 | 10.5  |
    | Sell | 10.4 | 10.5   | 10.6 | 10.7  |


  Scenario Outline: Pegged order tracks best limit and then stops when it is too aggressive
    Given the following orders are added to the "<Side>" book:
      | Broker | Qty | Price      |
      | A      | 100 | <Worse>    |
      | B      | 100 | Peg[<Peg>] |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price               |
      | A      | 100 | <Worse>             |
      | B      | 100 | Peg(<Worse>)[<Peg>] |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | C      | 100 | <Peg> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | C      | 100 | <Peg>             |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | A      | 100 | <Worse>           |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | D      | 100 | <Peg> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | C      | 100 | <Peg>             |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | D      | 100 | <Peg>             |
      | A      | 100 | <Worse>           |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price    |
      | E      | 100 | <Better> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | E      | 100 | <Better>          |
      | C      | 100 | <Peg>             |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | D      | 100 | <Peg>             |
      | A      | 100 | <Worse>           |

    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price  |
      | F      | 100 | <Best> |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | F      | 100 | <Best>            |
      | E      | 100 | <Better>          |
      | C      | 100 | <Peg>             |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | D      | 100 | <Peg>             |
      | A      | 100 | <Worse>           |

  Examples:
    | Side | Worse | Peg  | Better | Best |
    | Buy  | 10.5  | 10.6 | 10.7   | 10.8 |
    | Sell | 10.7  | 10.6 | 10.5   | 10.4 |


  Scenario Outline: Pegged with limit that wasn't tracking maintains its position (not resubmitted)
  when its limit becomes new best limit

    Given the following orders are added to the "<Side>" book:
      | Broker | Qty | Price      |
      | A      | 100 | <Better>   |
      | B      | 100 | Peg[<Peg>] |
      | C      | 100 | <Peg>      |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | A      | 100 | <Better>          |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | C      | 100 | <Peg>             |

    When the top order goes away from the "<Side>" book
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price             |
      | B      | 100 | Peg(<Peg>)[<Peg>] |
      | C      | 100 | <Peg>             |


  Examples:
    | Side | Peg  | Better |
    | Buy  | 10.6 | 10.7   |
    | Sell | 10.6 | 10.5   |
