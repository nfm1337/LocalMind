package il.nfm.localmind

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import il.nfm.localmind.ml.Retriever
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LocalMindApp : Application() {
    @Inject
    lateinit var retriever: Retriever

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        check(::retriever.isInitialized) { "Retriever was not injected" }
    }
}
