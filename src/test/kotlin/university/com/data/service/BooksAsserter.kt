package university.com.data.service

import university.com.data.model.Book
import kotlin.test.assertEquals

object BooksAsserter {

    fun assertBookLists(expected: List<Book>, actual: List<Book>) {
        assertEquals(expected.size, actual.size)
        assertEquals(expected[0], actual[0])
        assertEquals(expected[1], actual[1])
        assertEquals(expected[2], actual[2])
        assertEquals(expected[3], actual[3])
        assertEquals(expected[4], actual[4])
        assertEquals(expected[5], actual[5])
        assertEquals(expected[6], actual[6])
        assertEquals(expected[7], actual[7])
        assertEquals(expected[8], actual[8])
        assertEquals(expected[9], actual[9])
    }
}