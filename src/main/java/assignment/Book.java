package assignment;

public class Book {
    private int id;
    private String author;
    private String title;
    private String ISBN;
    private int pages;
    private int released;

    public Book(int id, String author, String title, String ISBN, int pages, int released) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.ISBN = ISBN;
        this.pages = pages;
        this.released = released;
    }

    @Override
    public String toString() {
        return String.format("Book{id=%d, author='%s', title='%s', ISBN='%s', pages=%d, released=%d}",
                id, author, title, ISBN, pages, released);
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getISBN() {
        return ISBN;
    }

    public int getPages() {
        return pages;
    }

    public int getReleased() {
        return released;
    }
}
