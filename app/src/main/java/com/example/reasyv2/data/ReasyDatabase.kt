package com.example.reasy.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reasy.data.dao.BusinessDao
import com.example.reasy.data.dao.ReservationDao
import com.example.reasy.data.dao.TimeSlotDao
import com.example.reasy.data.dao.UserDao
import com.example.reasy.data.entity.BusinessEntity
import com.example.reasy.data.entity.ReservationEntity
import com.example.reasy.data.entity.TimeSlotEntity
import com.example.reasy.data.entity.UserEntitiy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Database(
    entities = [UserEntitiy::class, BusinessEntity::class, TimeSlotEntity::class, ReservationEntity::class],
    version = 3 // Ensure this version is incremented when you make schema changes
)
abstract class ReasyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun reservationDao(): ReservationDao

    companion object {
        @Volatile
        private var INSTANCE: ReasyDatabase? = null

        fun getDatabase(context: Context): ReasyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReasyDatabase::class.java,
                    "reasy_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Only use migration here
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)

                                val usrID1 = database.userDao().insertUser(predefinedUsers[0])
                                val usrID2 = database.userDao().insertUser(predefinedUsers[1])
                                val usrID3 = database.userDao().insertUser(predefinedUsers[2])
                                val usrID4 = database.userDao().insertUser(predefinedUsers[3])

                                database.businessDao().insertBusiness(
                                    BusinessEntity(
                                        usrId = usrID1.toInt(),
                                        name = "La Bella Italia",
                                        description = "Authentic Italian cuisine",
                                        rating = "4.5 ★",
                                        workingHours = "09:00-17:00",
                                        reservationTime = 30,
                                        category = "Restaurants",
                                        imageUrl = "",
                                        address = "123 Italian Street",
                                        phone = "+1234567890"
                                    )
                                )
                                Log.d("ReasyDatabase", "Inserted business: La Bella Italia")
                                database.businessDao().insertBusiness(
                                    BusinessEntity(
                                        usrId = usrID2.toInt(),
                                        name = "Sushi Master",
                                        description = "Fresh Japanese delicacies",
                                        rating = "4.8 ★",
                                        workingHours = "10:00-18:00",
                                        reservationTime = 30,
                                        category = "Restaurants",
                                        imageUrl = "",
                                        address = "456 Sushi Avenue",
                                        phone = "+1234567891"
                                    )
                                )
                                database.businessDao().insertBusiness(
                                    BusinessEntity(
                                        usrId = usrID3.toInt(),
                                        name = "Grand Hotel",
                                        description = "Luxury 5-star hotel",
                                        rating = "4.7 ★",
                                        workingHours = "09:00-18:00",
                                        reservationTime = 60,
                                        category = "Hotels",
                                        imageUrl = "",
                                        address = "789 Luxury Boulevard",
                                        phone = "+1234567892"
                                    )
                                )
                                database.businessDao().insertBusiness(
                                    BusinessEntity(
                                        usrId = usrID4.toInt(),
                                        name = "Style Studio",
                                        description = "Premium hair salon",
                                        rating = "4.6 ★",
                                        workingHours = "10:00-19:00",
                                        reservationTime = 60,
                                        category = "Beauty Salons",
                                        imageUrl = "",
                                        address = "321 Beauty Street",
                                        phone = "+1234567893"
                                    )
                                )
                            }
                        }
                    }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Ensure the column is added to the table
        database.execSQL("ALTER TABLE BusinessEntity ADD COLUMN RESERVATION_TIME INTEGER")
    }
}
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `timeslot_new` (" +
                    "`tmsId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`BUS_ID` INTEGER NOT NULL, " +
                    "`DATE` TEXT NOT NULL, " +
                    "`START_TIME` TEXT NOT NULL, " +
                    "`END_TIME` TEXT NOT NULL, " +
                    "`STATUS` TEXT NOT NULL, " +
                    "FOREIGN KEY(`BUS_ID`) REFERENCES `BusinessEntity`(`busId`) ON DELETE CASCADE, " +
                    "UNIQUE(`BUS_ID`, `DATE`, `START_TIME`, `END_TIME`))"
        )

        database.execSQL(
            "INSERT INTO timeslot_new (tmsId, BUS_ID, DATE, START_TIME, END_TIME, STATUS) " +
                    "SELECT tmsId, BUS_ID, DATE, START_TIME, END_TIME, STATUS FROM timeslot"
        )

        database.execSQL("DROP TABLE timeslot")
        database.execSQL("ALTER TABLE timeslot_new RENAME TO timeslot")
    }
}
// Predefined data
val predefinedUsers = listOf(
    UserEntitiy(username = "labellaitalia", password = "123", role = "business"),
    UserEntitiy(username = "sushimaster", password = "123", role = "business"),
    UserEntitiy(username = "grandhotel", password = "123", role = "business"),
    UserEntitiy(username = "stylestudio", password = "123", role = "business"),
    )