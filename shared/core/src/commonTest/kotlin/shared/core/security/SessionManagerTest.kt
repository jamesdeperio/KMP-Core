package shared.core.security

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @Test
    fun testAutoLockAfterTimeout() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        
        val manager = SessionManager(
            timeout = 5.seconds,
            scope = testScope,
            currentTimeProvider = { testScheduler.currentTime }
        )

        assertFalse(manager.isLocked.value)

        // Advance time by 11 seconds (more than 5s timeout and includes a timer check)
        testScope.advanceTimeBy(11000.milliseconds)
        
        assertTrue(manager.isLocked.value)
    }

    @Test
    fun testActivityResetsTimer() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        
        val manager = SessionManager(
            timeout = 10.seconds,
            scope = testScope,
            currentTimeProvider = { testScheduler.currentTime }
        )

        assertFalse(manager.isLocked.value)

        // Advance by 6 seconds
        testScope.advanceTimeBy(6000.milliseconds)
        assertFalse(manager.isLocked.value)

        // Report activity at 6s
        manager.reportActivity()

        // Advance by another 6 seconds (total 12s, but only 6s since last activity)
        testScope.advanceTimeBy(6000.milliseconds)
        assertFalse(manager.isLocked.value)

        // Advance to cross the 10s mark since last activity (at 6s + 11s = 17s)
        testScope.advanceTimeBy(11000.milliseconds)
        assertTrue(manager.isLocked.value)
    }
}
