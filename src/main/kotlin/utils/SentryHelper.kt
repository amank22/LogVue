package utils

import com.voxfinite.logvue.APP_VERSION
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryOptions

object SentryHelper {

    private const val SAMPLE_RATE = 0.3

    fun init() {
        Sentry.init { options: SentryOptions ->
            options.dsn = System.getProperty("SENTRY_ENDPOINT").orEmpty()
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = SAMPLE_RATE
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setDebug(System.getProperty("SENTRY_DEBUG").toBoolean())
        }
        Sentry.configureScope { scope ->
            scope.setTag("os.name", System.getProperty("os.name"))
            scope.setTag("os.arch", System.getProperty("os.arch"))
            scope.setTag("os.version", System.getProperty("os.version"))
            scope.setTag("build.version", APP_VERSION)
        }
    }

    fun breadcrumb(breadcrumb: Breadcrumb) {
        Sentry.addBreadcrumb(breadcrumb)
    }
}
