package il.nfm.localmind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import il.nfm.localmind.feature.notes.data.NoteDao
import il.nfm.localmind.feature.notes.data.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
abstract class LocalMindDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
