package il.nfm.localmind.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import il.nfm.localmind.feature.notes.data.NotesRepositoryImpl
import il.nfm.localmind.feature.notes.domain.NotesRepository

@Module
@InstallIn(SingletonComponent::class)
interface NotesModule {
    @Binds
    fun bindNoteRepository(impl: NotesRepositoryImpl): NotesRepository
}
