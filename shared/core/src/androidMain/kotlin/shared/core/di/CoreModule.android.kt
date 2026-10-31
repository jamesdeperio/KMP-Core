package shared.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import shared.core.Config
import shared.core.security.TokenProvider
import shared.core.security.createAtRestCipher
import shared.core.security.createSecureStorage
import shared.core.security.createSecureTokenProvider
import org.koin.core.qualifier.named

actual val platformModule: Module =
    module {
        // 1. Register Security components
        single<TokenProvider> {
            createSecureTokenProvider(androidContext())
        }

        // 2. Register a general SecureStorage instance
        single {
            createSecureStorage(androidContext(), Config.SECURE_PREFS_NAME)
        }

        single(named("atRestCipher")) {
            createAtRestCipher(androidContext())
        }
    }
