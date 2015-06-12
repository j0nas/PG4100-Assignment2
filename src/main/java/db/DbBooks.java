package db;

import models.Book;

import java.util.List;

public class DbBooks {
    private static DbBooks instance;
    private List<Book> books = Db.fetchAndParseBooks();
    private Book lastReturnedBook;

    private DbBooks() {
    }

    public static DbBooks getInstance() {
        if (instance == null) {
            instance = new DbBooks();
        }

        return instance;
    }

    public Book getRandomBook() {
        if (books.isEmpty()) {
            throw new RuntimeException("List of books is empty! Check connection to or content availability of DB.");
        }

        lastReturnedBook = books.get((int) (Math.random() * books.size()));
        return lastReturnedBook;
    }

    public Book getLastBook() {
        if (lastReturnedBook == null) {
            return getRandomBook();
        }

        return lastReturnedBook;
    }
}
