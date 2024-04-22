package DbmsProject;
import java.sql.*;
import java.util.Scanner;

public class LibraryManagementSystem {
    // Database connection parameters
    private static final String DB_NAME = "dbms";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "sohan123";
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "5432";

    // Connection object
    private Connection conn;

    // Connect to the database
    private void connect() {
        try {
            String url = "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            System.out.println("Connection was established with" + url);
        }
        catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found. Make sure it's in your classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error connecting to the database.");
            e.printStackTrace();
        }
        
    }

    // Close the connection
    private void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    // Add book function
    private void addBook(String title, int authorId, String publicationDate, String genre, int availableCopies) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO books (title, author_id, publication_date, genre, available_copies) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setString(1, title);
            pstmt.setInt(2, authorId);
            pstmt.setDate(3, Date.valueOf(publicationDate));
            pstmt.setString(4, genre);
            pstmt.setInt(5, availableCopies);

            pstmt.executeUpdate();
            System.out.println("Book added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error adding book");
        }
    }

    // Delete book function
    private void deleteBook(int bookId) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "DELETE FROM books WHERE book_id = ?")) {

            pstmt.setInt(1, bookId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("Book with ID " + bookId + " not found.");
            } else {
                System.out.println("Book deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting book");
        }
    }

    // Search book function
    private void searchBook(String title) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM books WHERE title ILIKE ?")) {

            pstmt.setString(1, "%" + title + "%");

            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("No books found.");
            } else {
                System.out.println("Books found:");
                do {
                    // Print book details
                    System.out.println("Title: " + rs.getString("title"));
                    System.out.println("Author ID: " + rs.getInt("author_id"));
                    System.out.println("Publication Date: " + rs.getDate("publication_date"));
                    System.out.println("Genre: " + rs.getString("genre"));
                    System.out.println("Available Copies: " + rs.getInt("available_copies"));
                } while (rs.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error searching book");
        }
    }

 // Update book function
    private void updateBook(int bookId) {
        try (PreparedStatement pstmtCheck = conn.prepareStatement(
                "SELECT * FROM books WHERE book_id = ?")) {

            pstmtCheck.setInt(1, bookId);

            ResultSet rs = pstmtCheck.executeQuery();

            if (!rs.next()) {
                System.out.println("Book with ID " + bookId + " not found.");
                return;
            }

            System.out.println("Book found. What do you want to update?");
            System.out.println("1. Title");
            System.out.println("2. Author ID");
            System.out.println("3. Publication Date");
            System.out.println("4. Genre");
            System.out.println("5. Available Copies");
            System.out.print("Enter your choice: ");

            Scanner sc = new Scanner(System.in);
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter new title: ");
                    String newTitle = sc.nextLine();
                    updateBookField("title", newTitle, bookId);
                    break;
                case "2":
                    System.out.print("Enter new author ID: ");
                    int newAuthorId = Integer.parseInt(sc.nextLine());
                    updateBookField("author_id", newAuthorId, bookId);
                    break;
                case "3":
                    System.out.print("Enter new publication date (YYYY-MM-DD): ");
                    String newPublicationDate = sc.nextLine();
                    updateBookField("publication_date", newPublicationDate, bookId);
                    break;
                case "4":
                    System.out.print("Enter new genre: ");
                    String newGenre = sc.nextLine();
                    updateBookField("genre", newGenre, bookId);
                    break;
                case "5":
                    System.out.print("Enter new available copies: ");
                    int newAvailableCopies = Integer.parseInt(sc.nextLine());
                    updateBookField("available_copies", newAvailableCopies, bookId);
                    break;
                default:
                    System.out.println("Invalid choice. No updates were made.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating book");
        }
    }

    // Update a specific field of a book
    private void updateBookField(String field, Object value, int bookId) throws SQLException {
        try (PreparedStatement pstmtUpdate = conn.prepareStatement(
                "UPDATE books SET " + field + " = ? WHERE book_id = ?")) {

            if (value instanceof String) {
                pstmtUpdate.setString(1, (String) value);
            } else if (value instanceof Integer) {
                pstmtUpdate.setInt(1, (int) value);
            }

            pstmtUpdate.setInt(2, bookId);
            int rowsAffected = pstmtUpdate.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Book updated successfully.");
            } else {
                System.out.println("Error updating book. Please try again.");
            }
        }
    }
    
 // Aggregate functions
    private void performAggregateFunction(String function, String column) {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT " + function + "(" + column + ") FROM books";
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            System.out.println(function.toUpperCase() + " of " + column + ": " + rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error performing aggregate function.");
        }
    }

    // Sorting
    private void performSorting(String column, String order) {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT * FROM books ORDER BY " + column + " " + order;
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                // Print book details
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Author ID: " + rs.getInt("author_id"));
                System.out.println("Publication Date: " + rs.getDate("publication_date"));
                System.out.println("Genre: " + rs.getString("genre"));
                System.out.println("Available Copies: " + rs.getInt("available_copies"));
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error performing sorting.");
        }
    }
    
    // Join tables using INNER JOIN
    private void performJoin() {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT books.title, authors.author_name, authors.birth_date, authors.nationality " +
                           "FROM books " +
                           "INNER JOIN authors ON books.author_id = authors.author_id";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("Book Title: " + rs.getString("title"));
                System.out.println("Author Name: " + rs.getString("author_name"));
                System.out.println("Author Birth Date: " + rs.getDate("birth_date"));
                System.out.println("Author Nationality: " + rs.getString("nationality"));
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error performing join.");
        }
    }

    // Grouping query results
    private void performGrouping() {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT genre, COUNT(*) AS count FROM books GROUP BY genre";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("Genre: " + rs.getString("genre"));
                System.out.println("Book Count: " + rs.getInt("count"));
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error performing grouping.");
        }
    }

    
    // subquery example: Fetch authors who have books in the database
    public void performSubquery() {
        try (Statement stmt = conn.createStatement()) {
            String query = "SELECT * FROM authors WHERE author_id IN (SELECT DISTINCT author_id FROM books)";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("Author ID: " + rs.getInt("author_id"));
                System.out.println("Author Name: " + rs.getString("author_name"));
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error performing subquery.");
        }
    }

    // Transactions example: Add book and update available copies within a transaction
    public void performTransaction() {
        try {
            conn.setAutoCommit(false); 
            try (PreparedStatement pstmt1 = conn.prepareStatement(
                    "INSERT INTO books (title, author_id, publication_date, genre, available_copies) VALUES (?, ?, ?, ?, ?)")) {
                pstmt1.setString(1, "New Book");
                pstmt1.setInt(2, 1); 
                pstmt1.setDate(3, Date.valueOf("2022-01-01"));
                pstmt1.setString(4, "Fiction");
                pstmt1.setInt(5, 10);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(
                    "UPDATE books SET available_copies = available_copies - 1 WHERE title = ?")) {
                pstmt2.setString(1, "New Book");
                pstmt2.executeUpdate();
            }

            conn.commit(); 
            System.out.println("Transaction completed successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback(); 
                System.out.println("Transaction rolled back.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error rolling back transaction.");
            }
        } finally {
            try {
                conn.setAutoCommit(true); 
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error restoring auto-commit mode.");
            }
        }
    }

    // Error handling example
    public void performErrorHandling() {
        try (Statement stmt = conn.createStatement()) {
            // Incorrect SQL statement to intentionally cause an error
            String query = "SELECT * FROM library";
            stmt.executeQuery(query); // This will throw SQLException
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }


    // Main function
    public static void main(String[] args) throws ClassNotFoundException {
        Scanner sc = new Scanner(System.in);
        try {
            LibraryManagementSystem app = new LibraryManagementSystem();
            app.connect();

            while (true) {
                // Display menu options
                System.out.println("\nLibrary Management System");
                System.out.println("1. Add Book");
                System.out.println("2. Delete Book");
                System.out.println("3. Search Book");
                System.out.println("4. Update Book");
                System.out.println("5. Perform Aggregate Function");
                System.out.println("6. Perform Sorting");
                System.out.println("7. Perform Join");
                System.out.println("8. Perform Grouping");
                System.out.println("9. Perform Subquery");
                System.out.println("10. Perform Transaction");
                System.out.println("11. Perform Error Handling");
                System.out.println("12. Exit\n");              
                
                System.out.println("Enter Choice: ");
                String userChoice = sc.nextLine();

                switch (userChoice) {

                    case "1":
                    	System.out.print("Enter title of the book: ");
                        String title = sc.nextLine();
                        System.out.print("Enter author ID: ");
                        int authorId = Integer.parseInt(sc.nextLine());
                        System.out.print("Enter publication date (YYYY-MM-DD): ");
                        String publicationDate = sc.nextLine();
                        System.out.print("Enter genre: ");
                        String genre = sc.nextLine();
                        System.out.print("Enter available copies: ");
                        int availableCopies = Integer.parseInt(sc.nextLine());
                        app.addBook(title, authorId, publicationDate, genre, availableCopies);
                        break;
                     case "2":
                    	 System.out.print("Enter book ID to delete: ");
                         int bookIdToDelete = Integer.parseInt(sc.nextLine());
                         app.deleteBook(bookIdToDelete);
                         break;
                     case "3":
                    	 System.out.print("Enter title of the book to search: ");
                         String titleToSearch = sc.nextLine();
                         app.searchBook(titleToSearch);
                         break;
                     case "4":
                    	 System.out.print("Enter book ID to update: ");
                         int bookIdToUpdate = Integer.parseInt(sc.nextLine());
                         app.updateBook(bookIdToUpdate);
                         break;
                     case "5":
                         System.out.print("Enter aggregate function (SUM, AVG, COUNT, MIN, MAX): ");
                         String function = sc.nextLine().toUpperCase();
                         System.out.print("Enter column name: ");
                         String column = sc.nextLine();
                         app.performAggregateFunction(function, column);
                         break;
                     case "6":
                         System.out.print("Enter column name to sort by: ");
                         String sortColumn = sc.nextLine();
                         System.out.print("Enter sorting order (ASC/DESC): ");
                         String sortOrder = sc.nextLine().toUpperCase();
                         app.performSorting(sortColumn, sortOrder);
                         break;
                     case "7":
                         app.performJoin();
                         break;
                     case "8":
                         app.performGrouping();
                         break;
                     case "9":
                         app.performSubquery();
                         break;
                     case "10":
                         app.performTransaction();
                         break;
                     case "11":
                         app.performErrorHandling();
                         break;
                    case "12":
                        app.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
