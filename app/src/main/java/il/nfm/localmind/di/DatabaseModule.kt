package il.nfm.localmind.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import il.nfm.localmind.core.database.LocalMindDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext context: Context,
    ): LocalMindDatabase =
        Room
            .databaseBuilder(
                context,
                LocalMindDatabase::class.java,
                "localmind.db",
            ).build()

    @Provides
    fun provideNoteDao(db: LocalMindDatabase) = db.noteDao()
}
