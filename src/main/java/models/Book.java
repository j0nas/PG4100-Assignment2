package models;

public class Book {
    private final int id;
    private final String authorFirstName;
    private final String authorLastName;
    private final String title;
    private final String ISBN;
    private final int pages;
    private final int released;

    public Book(int id, String authorFirstName, String authorLastName, String title, String ISBN, int pages, int released) {
        this.id = id;
        this.authorFirstName = authorFirstName;
        this.authorLastName = authorLastName;
        this.title = title;
        this.ISBN = ISBN;
        this.pages = pages;
        this.released = released;
    }

    @Override
    public String toString() {
        return String.format("Book{id=%d, authorFirstName='%s', authorLastName='%s', " +
                        "title='%s', ISBN='%s', pages=%d, released=%d}",
                id, authorFirstName, authorLastName, title, ISBN, pages, released);
    }

    public int getId() {
        return id;
    }

    public String getAuthorFullNameCaps() {
        return (authorLastName + ", " + authorFirstName).toUpperCase();
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

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }
}
