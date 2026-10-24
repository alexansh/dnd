package com.grimoire.dnd.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EntityRecord::class,
        RelationshipRecord::class,
        TimedClockRecord::class,
        DMSecretRecord::class,
        EventLogRecord::class,
        CanonLedgerRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GrimoireDatabase : RoomDatabase() {
    abstract fun entityDao(): EntityDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun timedClockDao(): TimedClockDao
    abstract fun dmSecretDao(): DMSecretDao
    abstract fun eventLogDao(): EventLogDao
    abstract fun canonLedgerDao(): CanonLedgerDao

    companion object {
        @Volatile
        private var INSTANCE: GrimoireDatabase? = null

        fun getInstance(context: Context): GrimoireDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrimoireDatabase::class.java,
                    "grimoire_world_state.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): GrimoireDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                GrimoireDatabase::class.java
            ).allowMainThreadQueries().build()
        }
    }
}
