package pl.handsome.club.ketoscanner

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import pl.handsome.club.ketoscanner.di.appModules


@Suppress("unused")
class KetoSApplication : Application() {

    override fun onCreate(){
        super.onCreate()

        startKoin {
            androidContext(this@KetoSApplication)
            modules(appModules)
        }
    }

}