package il.nfm.localmind.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import il.nfm.localmind.feature.notes.presentation.list.NotesListUiState

@Module
@InstallIn(ViewModelComponent::class)
object NotesPresentationModule {
    @Provides
    fun provideInitialNotesListUiState(): NotesListUiState = NotesListUiState()
}
