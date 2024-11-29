package com.example.noms

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.noms.backend.createPlaylist
import com.example.noms.backend.doesFollow
import com.example.noms.backend.findImage
import com.example.noms.backend.followUser
import com.example.noms.backend.getFollowers
import com.example.noms.backend.getImage
import com.example.noms.backend.getPlaylistsofUser
import com.example.noms.backend.getRestaurant
import com.example.noms.backend.getReviewsFromRestaurant
import com.example.noms.backend.getReviewsFromUser
import com.example.noms.backend.searchByLocation
import com.example.noms.backend.unfollowUser
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.noms", appContext.packageName)
    }

    @Test
    fun testGetReviewsFromUser() = runBlocking {
        val reviews = getReviewsFromUser(1)
        assertNotNull(reviews)
    }

    @Test
    fun testGetReviewsFromRestaurant() = runBlocking {
        val reviews = getReviewsFromRestaurant(1)
        assertNotNull(reviews)
    }

    // Playlist API Tests
    @Test
    fun testGetPlaylistsOfUser() = runBlocking {
        val playlists = getPlaylistsofUser(1)
        assertNotNull(playlists)
    }

    // Followers API Tests
    @Test
    fun testFollowAndUnfollow() = runBlocking {
        followUser(1, 2)
        assertTrue(doesFollow(1, 2))
        unfollowUser(1, 2)
        assertFalse(doesFollow(1, 2))
    }

    @Test
    fun testGetFollowers() = runBlocking {
        val followers = getFollowers(1)
        assertNotNull(followers)
    }

    // Restaurants API Tests
    @Test
    fun testGetRestaurant() = runBlocking {
        val restaurant = getRestaurant(1)
        assertNotNull(restaurant)
    }

    @Test
    fun testSearchByLocation() = runBlocking {
        val results = searchByLocation("43.4723,-80.5449")
        assertNotNull(results)
    }

    // Bucket API Tests
    @Test
    fun testFindImage() = runBlocking {
        val exists = findImage(1)
        assertFalse(exists) // Assuming test environment has no images
    }

    @Test
    fun testGetImage() = runBlocking {
        val image = getImage(1)
        assertNull(image) // Assuming test environment has no images
    }
}