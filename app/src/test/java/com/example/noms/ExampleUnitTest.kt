package com.example.noms

import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun getUserTest() = runBlocking{
        val res = getUsers(3)
        assertNotNull(res)
        assertEquals(res.last_name, "Nambiar")
    }

    @Test
    fun testFollow() = runBlocking{
        followUser(3,7)
        assert(doesFollow(3, 7))
    }

    @Test
    fun testFollowCount() = runBlocking{
        assertEquals(2, getFollowerCount(6))
        assertEquals(1, getFollowerCount(3))
        assertEquals(0, getFollowerCount(2))
    }
}