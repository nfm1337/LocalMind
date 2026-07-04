package il.nfm.localmind.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import il.nfm.localmind.feature.notes.data.NoteRepositoryImpl
import il.nfm.localmind.feature.notes.domain.NoteRepository

@Module
@InstallIn(SingletonComponent::class)
interface NoteModule {
    @Binds
    fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
