package shared.core.network

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ApiResultTest {

    @Test
    fun safeApiCall_onSuccess_returnsSuccess() = runTest {
        val result = safeApiCall { "Success" }
        assertIs<ApiResult.Success<String>>(result)
        assertEquals("Success", result.data)
    }

    @Test
    fun safeApiCall_onException_returnsError() = runTest {
        val result = safeApiCall<String> { throw Exception("Failure") }
        assertIs<ApiResult.Error>(result)
        assertEquals("Failure", result.error.message)
        assertTrue(result.error.isNetworkError)
    }

    @Test
    fun onSuccess_helper_executesAction() {
        var called = false
        val result = ApiResult.Success("Data")
        result.onSuccess { 
            called = true
            assertEquals("Data", it)
        }
        assertTrue(called)
    }

    @Test
    fun onError_helper_executesAction() {
        var called = false
        val error = ApiError(401, "Unauthorized")
        val result = ApiResult.Error(error)
        result.onError { 
            called = true
            assertEquals(401, it.statusCode)
            assertTrue(it.isUnauthorized)
        }
        assertTrue(called)
    }
}
