package com.example.bookapi.steps;

import com.example.bookapi.model.Book;
import com.example.bookapi.model.ErrorResponse;
import com.example.bookapi.repository.BookRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    private ResponseEntity<?> response;
    private List<Book> savedBooks;  // Add this field to store saved books

    @Before
    public void setup() {
        bookRepository.deleteAll();
    }

    @Given("the following books exist in the database")
    public void theFollowingBooksExistInTheDatabase(DataTable dataTable) {
        bookRepository.deleteAll();
        savedBooks = new ArrayList<>();
        
        List<Map<String, String>> books = dataTable.asMaps();
        books.forEach(book -> {
            Book newBook = new Book();
            newBook.setTitle(book.get("title"));
            newBook.setAuthor(book.get("author"));
            newBook.setIsbn(book.get("isbn"));
            newBook.setPrice(Double.parseDouble(book.get("price")));
            savedBooks.add(bookRepository.save(newBook));
        });
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String endpoint) {
        String url = "http://localhost:" + port + endpoint;
        
        if (endpoint.equals("/actuator/health")) {
            response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return;
        }

        // Existing code for other endpoints...
        if (endpoint.equals("/api/books/1")) {
            url = "http://localhost:" + port + "/api/books/" + savedBooks.get(0).getId();
        }

        if (endpoint.equals("/api/books")) {
            response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Book>>() {}
            );
        } else if (endpoint.contains("/api/books/999")) {
            response = restTemplate.getForEntity(url, ErrorResponse.class);
        } else {
            response = restTemplate.getForEntity(url, Book.class);
        }
    }

    @When("I send a POST request to {string} with JSON:")
    public void iSendAPOSTRequestToWithJSON(String endpoint, String jsonBody) {
        String url = "http://localhost:" + port + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        response = restTemplate.postForEntity(url, request, Book.class);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int statusCode) {
        assertEquals(HttpStatus.valueOf(statusCode), response.getStatusCode());
    }

    @Then("the response should contain {int} books")
    public void theResponseShouldContainBooks(int count) {
        List<Book> books = (List<Book>) response.getBody();
        assertEquals(count, books.size());
    }

    @Then("the response should include the following books")
    public void theResponseShouldIncludeTheFollowingBooks(DataTable dataTable) {
        List<Map<String, String>> expectedBooks = dataTable.asMaps();
        List<Book> actualBooks = (List<Book>) response.getBody();

        assertEquals(expectedBooks.size(), actualBooks.size());
        for (int i = 0; i < expectedBooks.size(); i++) {
            Map<String, String> expected = expectedBooks.get(i);
            Book actual = actualBooks.get(i);
            
            assertEquals(expected.get("title"), actual.getTitle());
            assertEquals(expected.get("author"), actual.getAuthor());
            assertEquals(expected.get("isbn"), actual.getIsbn());
            assertEquals(Double.parseDouble(expected.get("price")), actual.getPrice());
        }
    }

    @Then("the response should contain a book with the following details")
    public void theResponseShouldContainABookWithTheFollowingDetails(DataTable dataTable) {
        Map<String, String> expectedBook = dataTable.asMaps().get(0);
        Book actualBook = (Book) response.getBody();

        assertEquals(expectedBook.get("title"), actualBook.getTitle());
        assertEquals(expectedBook.get("author"), actualBook.getAuthor());
        assertEquals(expectedBook.get("isbn"), actualBook.getIsbn());
        assertEquals(Double.parseDouble(expectedBook.get("price")), actualBook.getPrice());
    }

    @Then("the response should contain error message {string}")
    public void theResponseShouldContainErrorMessage(String errorMessage) {
        if (response.getBody() instanceof ErrorResponse) {
            ErrorResponse errorResponse = (ErrorResponse) response.getBody();
            assertEquals(errorMessage, errorResponse.getMessage());
        } else {
            Map<String, String> errorMap = (Map<String, String>) response.getBody();
            assertEquals(errorMessage, errorMap.get("message"));
        }
    }

    @Then("the health response should show status {string}")
    public void theHealthResponseShouldShowStatus(String expectedStatus) {
        Map<String, Object> healthResponse = (Map<String, Object>) response.getBody();
        assertEquals(expectedStatus, healthResponse.get("status"));
    }
} 