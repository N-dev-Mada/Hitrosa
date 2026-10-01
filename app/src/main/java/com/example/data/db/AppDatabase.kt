package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ClientDao
import com.example.data.dao.TransactionDao
import com.example.data.model.Client
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity

@Database(
    entities = [
        Client::class,
        TransactionEntity::class,
        TransactionItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration sécurisée de la version 1 à la version 2 :
         * Ajout du support des émissaires / mandataires et des prénoms clients.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clients ADD COLUMN prenom TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN is_emissaire INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN emissaire_nom TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN emissaire_lien TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN emissaire_telephone TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN emissaire_confirmation TEXT")
            }
        }

        /**
         * Migration sécurisée de la version 2 à la version 3 :
         * Ajout de la photo obligatoire du mandataire.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN emissaire_photo_uri TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carnet_pro.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Triggers d'immuabilité stricts (Append-Only)
                            db.execSQL(
                                """
                                CREATE TRIGGER IF NOT EXISTS prevent_transaction_update
                                BEFORE UPDATE ON transactions
                                BEGIN
                                    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré est immuable et ne peut pas être modifié !');
                                END;
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                CREATE TRIGGER IF NOT EXISTS prevent_transaction_delete
                                BEFORE DELETE ON transactions
                                BEGIN
                                    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré ne peut pas être supprimé !');
                                END;
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                CREATE TRIGGER IF NOT EXISTS prevent_items_update
                                BEFORE UPDATE ON transaction_items
                                BEGIN
                                    SELECT RAISE(FAIL, 'SÉCURITÉ : Les lignes de facture sont immuables !');
                                END;
                                """.trimIndent()
                            )
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON;")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
