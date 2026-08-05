package shared.core.di

import org.koin.core.module.Module
import org.koin.dsl.module
import shared.core.Config
import shared.core.security.TokenProvider
import shared.core.security.createSecureStorage
import shared.core.security.createSecureTokenProvider

actual val platformModule: Module =
    module {
        // 1. Register Security components
        single<TokenProvider> {
            createSecureTokenProvider()
        }

        // 2. Register a general SecureStorage instance
        single {
            createSecureStorage(Config.SECURE_STORAGE_NAME)
        }
    }
