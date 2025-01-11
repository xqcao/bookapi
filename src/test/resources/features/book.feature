Feature: Book API
  As a user
  I want to retrieve book information
  So that I can manage my book inventory

  Background:
    Given the following books exist in the database
      | id | title          | author        | isbn          | price |
      | 1  | The Test Book | John Doe      | 123-456-789   | 29.99 |
      | 2  | Spring Boot   | James Smith   | 987-654-321   | 39.99 |

  Scenario: Get all books
    When I send a GET request to "/api/books"
    Then the response status code should be 200
    And the response should contain 2 books
    And the response should include the following books
      | title          | author        | isbn          | price |
      | The Test Book | John Doe      | 123-456-789   | 29.99 |
      | Spring Boot   | James Smith   | 987-654-321   | 39.99 |

  Scenario: Get a specific book by ID
    When I send a GET request to "/api/books/1"
    Then the response status code should be 200
    And the response should contain a book with the following details
      | title          | author   | isbn        | price |
      | The Test Book | John Doe | 123-456-789 | 29.99 |

  Scenario: Get a non-existent book
    When I send a GET request to "/api/books/999"
    Then the response status code should be 500
    And the response should contain error message "Book not found with id: 999" 