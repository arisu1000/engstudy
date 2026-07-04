package com.wcjung.engstudy.data.local

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteStatement
import java.io.File

/**
 * v9 → v10: 단어 뜻 콘텐츠 갱신 마이그레이션.
 *
 * kengdic 가나다순 정렬 문제로 재생성한 대표 뜻(교육부 검수 + 로컬 LLM)을
 * 기존 설치 기기에 반영한다. Room은 createFromAsset을 최초 설치 때만 적용하므로,
 * 업그레이드 기기는 이 마이그레이션이 APK에 내장된 asset DB를 임시 파일로 복사해
 * 콘텐츠 테이블만 갱신한다.
 *
 * 사용자 데이터(learning_progress, bookmarks, wrong_answers, known_items)는
 * 건드리지 않는다. 특히 words는 DELETE하지 않고 id 기준 UPDATE만 한다 —
 * wrong_answers 등이 words(id)를 ON DELETE CASCADE로 참조하므로
 * 행 삭제 시 사용자 기록이 연쇄 삭제될 수 있다.
 */
class RefreshWordContentMigration(private val context: Context) : Migration(9, 10) {

    override fun migrate(db: SupportSQLiteDatabase) {
        val tempFile = File.createTempFile("asset_content", ".db", context.cacheDir)
        try {
            context.assets.open(ASSET_PATH).use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            SQLiteDatabase.openDatabase(
                tempFile.path, null, SQLiteDatabase.OPEN_READONLY
            ).use { assetDb ->
                upsertWords(db, assetDb)
                replaceContentTable(db, assetDb, "word_meanings")
            }
        } finally {
            tempFile.delete()
        }
    }

    /** words를 id 기준 UPDATE, 기기에 없는 id는 INSERT. */
    private fun upsertWords(db: SupportSQLiteDatabase, assetDb: SQLiteDatabase) {
        assetDb.rawQuery("SELECT * FROM words", null).use { cursor ->
            val columns = cursor.columnNames
            val idIndex = cursor.getColumnIndexOrThrow("id")
            val dataIndexes = columns.indices.filter { it != idIndex }

            val update = db.compileStatement(
                "UPDATE words SET ${dataIndexes.joinToString { "${columns[it]} = ?" }} WHERE id = ?"
            )
            val insert = db.compileStatement(
                "INSERT INTO words (${columns.joinToString()}) " +
                    "VALUES (${columns.joinToString { "?" }})"
            )
            while (cursor.moveToNext()) {
                dataIndexes.forEachIndexed { bindIndex, columnIndex ->
                    bind(update, bindIndex + 1, cursor, columnIndex)
                }
                update.bindLong(dataIndexes.size + 1, cursor.getLong(idIndex))
                if (update.executeUpdateDelete() == 0) {
                    columns.indices.forEach { bind(insert, it + 1, cursor, it) }
                    insert.executeInsert()
                }
            }
        }
    }

    /** 사용자 데이터가 참조하지 않는 콘텐츠 전용 테이블을 asset 내용으로 전체 교체. */
    private fun replaceContentTable(
        db: SupportSQLiteDatabase,
        assetDb: SQLiteDatabase,
        table: String,
    ) {
        db.execSQL("DELETE FROM $table")
        assetDb.rawQuery("SELECT * FROM $table", null).use { cursor ->
            val columns = cursor.columnNames
            val insert = db.compileStatement(
                "INSERT INTO $table (${columns.joinToString()}) " +
                    "VALUES (${columns.joinToString { "?" }})"
            )
            while (cursor.moveToNext()) {
                columns.indices.forEach { bind(insert, it + 1, cursor, it) }
                insert.executeInsert()
            }
        }
    }

    private fun bind(statement: SupportSQLiteStatement, index: Int, cursor: Cursor, columnIndex: Int) {
        when (cursor.getType(columnIndex)) {
            Cursor.FIELD_TYPE_NULL -> statement.bindNull(index)
            Cursor.FIELD_TYPE_INTEGER -> statement.bindLong(index, cursor.getLong(columnIndex))
            Cursor.FIELD_TYPE_FLOAT -> statement.bindDouble(index, cursor.getDouble(columnIndex))
            Cursor.FIELD_TYPE_BLOB -> statement.bindBlob(index, cursor.getBlob(columnIndex))
            else -> statement.bindString(index, cursor.getString(columnIndex))
        }
    }

    private companion object {
        const val ASSET_PATH = "databases/engstudy.db"
    }
}
