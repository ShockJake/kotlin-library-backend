package university.com.data.service

import university.com.data.model.Book

object DataSupplier {

    fun getBook(): Book {
        return Book("TEST_TITLE", "TEST_AUTHOR", "TEST_DATE", "TEST_ID")
    }

    fun getCategories(): List<String> {
        return listOf(
            "Fiction",
            "Non-fiction",
            "Mystery",
            "Thriller",
            "Science Fiction",
            "Romance",
            "Historical Fiction",
            "Biography",
            "Personal Development",
            "Philosophy",
            "Travel",
            "Science",
            "Poetry",
            "Art",
            "Photography",
            "Economics",
            "Cooking",
            "History"
        )
    }

    fun getBooksAsObjects(): List<Book> {
        return listOf(
            Book("Title 1", "Author 1", "2024", "id#1"),
            Book("Title 2", "Author 2", "2024", "id#2"),
            Book("Title 3", "Author 3", "2024", "id#3"),
            Book("Title 4", "Author 4", "2024", "id#4"),
            Book("Title 5", "Author 5", "2024", "id#5"),
            Book("Title 6", "Author 6", "2024", "id#6"),
            Book("Title 7", "Author 7", "2024", "id#7"),
            Book("Title 8", "Author 8", "2024", "id#8"),
            Book("Title 9", "Author 9", "2024", "id#9"),
            Book("Title 10", "Author 10", "2024", "id#10")
        )
    }

    fun getBooksAsStrings(): List<String> {
        return getBooksAsObjects().map { "- ${it.title} | ${it.author} | ${it.firstPublishedDate} | [Amazon Link](https://www.amazon.com/dp/${it.amazonBookId})" }
    }
}
