Feature: Core Functionality for Order Book

  Scenario: Add a single limit order to the BUY order book
    When the following orders are added to the "Buy" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
    Then the "Buy" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |

  Scenario: Add two limit orders to the BUY order book, with more aggressive order first
    When the following orders are added to the "Buy" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.4  |
    Then the "Buy" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.4  |

  Scenario: Add two limit orders to the BUY order book, with less aggressive order first
    When the following orders are added to the "Buy" book:
      | Broker | Qty | Price |
      | B      | 100 | 10.4  |
      | A      | 100 | 10.5  |
    Then the "Buy" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.4  |

  Scenario: Add a single limit order to the SELL order book
    When the following orders are added to the "Sell" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.6  |
    Then the "Sell" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.6  |

  Scenario: Add two limit orders to the SELL order book, with more aggressive order first
    When the following orders are added to the "Sell" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.6  |
      | B      | 100 | 10.7  |
    Then the "Sell" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.6  |
      | B      | 100 | 10.7  |

  Scenario: Add two limit orders to the SELL order book, with less aggressive order first
    When the following orders are added to the "Sell" book:
      | Broker | Qty | Price |
      | B      | 100 | 10.7  |
      | A      | 100 | 10.6  |
    Then the "Sell" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.6  |
      | B      | 100 | 10.7  |

  Scenario: Add two limit orders to the BUY order book, with the same price limit
    When the following orders are added to the "Buy" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.5  |
    Then the "Buy" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.5  |

  Scenario: Add two limit orders to the SELL order book, with the same price limit
    When the following orders are added to the "Sell" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.7  |
      | B      | 100 | 10.7  |
    Then the "Sell" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.7  |
      | B      | 100 | 10.7  |

  Scenario Outline: Decrease top outstanding order partially and then fill it completely
    When the following orders are added to the "<Side>" book:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.5  |
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | A      | 100 | 10.5  |
      | B      | 100 | 10.5  |
    When the top order of the "<Side>" book is filled by "20"
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | A      | 80  | 10.5  |
      | B      | 100 | 10.5  |
    When the top order of the "<Side>" book is filled by "80"
    Then the "<Side>" order book looks like:
      | Broker | Qty | Price |
      | B      | 100 | 10.5  |
  Examples:
    | Side |
    | Buy  |
    | Sell |
