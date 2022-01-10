package utils

import com.voxfinite.logvue.APP_VERSION
import com.voxfinite.logvue.SENTRY_ENDPOINT
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryOptions

object SentryHelper {

    private const val SAMPLE_RATE = 0.3

    fun isEnabled() = Sentry.isEnabled()

    fun init() {
        val endPoint = SENTRY_ENDPOINT
        if (endPoint.isBlank()) return
        Sentry.init { options: SentryOptions ->
            options.dsn = endPoint
            options.isEnableAutoSessionTracking = false
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = SAMPLE_RATE
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setDebug(false)
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
