package shared.core.network

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException

/**
 * A sealed wrapper for network responses to enforce safe error handling.
 *
 * It prevents the UI layer from having to deal with low-level Ktor exceptions.
 * Every network call should return an [ApiResult].
 */
sealed class ApiResult<out T> {
    /** The request was successful and returned [data]. */
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>()

    /** The request failed with a specific [error]. */
    data class Error(
        val error: ApiError,
    ) : ApiResult<Nothing>()
}

/**
 * Functional helper to execute an action if the result is [ApiResult.Success].
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

/**
 * Functional helper to execute an action if the result is [ApiResult.Error].
 */
inline fun <T> ApiResult<T>.onError(action: (ApiError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(error)
    return this
}

/**
 * Represents a structured network error.
 *
 * @property statusCode The HTTP status code (e.g., 404, 500), or null if it was a connection error.
 * @property message A human-readable error message (often the server response body).
 * @property cause The original [Throwable] for debugging.
 */
data class ApiError(
    val statusCode: Int?,
    val message: String,
    val data: Any? = null,
    val cause: Throwable? = null,
) {
    /** Returns true if the server returned a 401 Unauthorized status. */
    val isUnauthorized: Boolean get() = statusCode == HttpStatusCode.Unauthorized.value

    /** Returns true if the server returned a 403 Forbidden status. */
    val isForbidden: Boolean get() = statusCode == HttpStatusCode.Forbidden.value

    /** Returns true if the error was caused by connection issues (DNS, Timeout, etc.). */
    val isNetworkError: Boolean get() = statusCode == null
}

/**
 * Wraps a Ktor call, turning thrown exceptions into [ApiResult.Error] so
 * repositories never need try/catch around every network call.
 *
 * Usage:
 * ```
 * suspend fun getProfile(): ApiResult<ProfileDto> = safeApiCall {
 *     httpClient.get("profile").body()
 * }
 * ```
 */
suspend inline fun <reified T> safeApiCall(crossinline block: suspend () -> T): ApiResult<T> =
    try {
        ApiResult.Success(block())
    } catch (e: CancellationException) {
        throw e // never swallow coroutine cancellation
    } catch (e: ClientRequestException) {
        ApiResult.Error(toApiError(e))
    } catch (e: ServerResponseException) {
        ApiResult.Error(toApiError(e))
    } catch (e: ResponseException) {
        ApiResult.Error(toApiError(e))
    } catch (e: Exception) {
        ApiResult.Error(ApiError(statusCode = 500, message = "Something went wrong", cause = e))
    }

suspend fun toApiError(e: ResponseException): ApiError {
    val response: HttpResponse = e.response
    val errorBody = try {
        response.body<ApiErrorResponse<BaseErrorData>>().error
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    return if (errorBody != null) {
        ApiError(
            statusCode = errorBody.code ?: response.status.value,
            message = errorBody.message ?: "Something went wrong",
            data = errorBody,
            cause = e,
        )
    } else {
        ApiError(
            statusCode = 500,
            message = "Something went wrong",
            data = null,
            cause = e,
        )
    }
}
