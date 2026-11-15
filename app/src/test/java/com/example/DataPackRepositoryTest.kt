package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CareerSaveEntity
import com.example.data.FootballLifeDatabase
import com.example.datapack.DataPackParser
import com.example.datapack.DataPackDocument
import com.example.datapack.DataPackRepository
import com.example.datapack.WorldDataDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataPackRepositoryTest {
    @Test fun installingWorldPackLeavesExistingCareerDatabaseUnchanged() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val careerDb = Room.databaseBuilder(context, FootballLifeDatabase::class.java, "test-career-preservation.db").allowMainThreadQueries().build()
        val worldDb = Room.databaseBuilder(context, WorldDataDatabase::class.java, "test-world-data.db").allowMainThreadQueries().build()
        val root = File(context.cacheDir, "datapack-test-${System.nanoTime()}").apply { mkdirs() }
        try {
            val before = CareerSaveEntity(1, 3, "Kariyer", "Deneme Oyuncusu", "ST", "Kulüp", "Lig", 68, 1, "10 Ağu", 1_000L, 100L, "{}", "{}")
            careerDb.careerSaveDao().upsertSave(before)
            val bytes = javaClass.classLoader!!.getResourceAsStream("datapacks/tff-super-lig-2026-27-sample.json")!!.use { it.readBytes() }
            val parsed = DataPackParser.parse("sample.json", bytes)
            val repository = DataPackRepository(context.contentResolver, worldDb, root)
            val report = repository.install(parsed)

            assertTrue(report.issues.joinToString { it.message }, report.accepted)
            assertEquals(3, worldDb.worldDataDao().recordsForPack("tff-super-lig-2026-27-20261114-sample@1.0.0").size)
            assertEquals("tff-super-lig-2026-27-20261114-sample", worldDb.worldDataDao().getPack("tff-super-lig-2026-27-20261114-sample@1.0.0")?.packId)
            val documentAdapter = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(DataPackDocument::class.java)
            val secondVersion = documentAdapter.toJson(parsed.document!!.copy(metadata = parsed.document!!.metadata.copy(version = "1.0.1"))).toByteArray()
            val secondReport = repository.install(DataPackParser.parse("sample-v2.json", secondVersion))
            assertTrue(secondReport.issues.joinToString { it.message }, secondReport.accepted)
            assertEquals(3, worldDb.worldDataDao().recordsForPack("tff-super-lig-2026-27-20261114-sample@1.0.1").size)
            assertEquals(before, careerDb.careerSaveDao().getSaveBySlot(1))
            assertEquals("test-world-data.db", worldDb.openHelper.databaseName)
        } finally {
            careerDb.close(); worldDb.close(); root.deleteRecursively()
            context.deleteDatabase("test-career-preservation.db")
            context.deleteDatabase("test-world-data.db")
        }
    }
}

