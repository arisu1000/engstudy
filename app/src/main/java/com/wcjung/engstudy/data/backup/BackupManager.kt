package com.wcjung.engstudy.data.backup

import com.wcjung.engstudy.data.datastore.UserPreferences
import com.wcjung.engstudy.data.local.dao.BackupDao
import com.wcjung.engstudy.data.local.entity.BookmarkEntity
import com.wcjung.engstudy.data.local.entity.KnownItemEntity
import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.UserWordMeaningEntity
import com.wcjung.engstudy.data.local.entity.WrongAnswerEntity
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 학습 데이터 백업/복원.
 *
 * 오프라인 전용 앱이라 기기 변경·분실 시 데이터가 유실되므로,
 * 사용자 데이터(학습 진도·북마크·오답·알아요 표시)와 설정을 JSON으로
 * 내보내고 다시 불러올 수 있게 한다. 콘텐츠 테이블은 앱에 내장되어
 * 백업하지 않으며, 단어 id 기준으로 재연결한다.
 */
@Singleton
class BackupManager @Inject constructor(
    private val backupDao: BackupDao,
    private val userPreferences: UserPreferences
) {

    @Serializable
    data class BackupFile(
        // 포맷 변경 시 하위 호환 판단에 사용
        val version: Int = FORMAT_VERSION,
        val exportedAt: Long,
        val settings: SettingsBackup,
        val learningProgress: List<ProgressBackup>,
        val bookmarks: List<BookmarkBackup>,
        val wrongAnswers: List<WrongAnswerBackup>,
        val knownItems: List<KnownItemBackup>,
        // v11 추가 — 기본값이 있어 v1 백업 파일도 그대로 읽힌다
        val userWordMeanings: List<UserWordMeaningBackup> = emptyList()
    )

    @Serializable
    data class SettingsBackup(
        val darkMode: Boolean,
        val themeMode: String,
        val ttsSpeed: Float,
        val dailyGoal: Int,
        val notificationEnabled: Boolean,
        val notificationHour: Int,
        val notificationMinute: Int,
        val streakDays: Int,
        val lastStudyDate: Long,
        val hasCompletedPlacementTest: Boolean,
        val recommendedStage: Int
    )

    @Serializable
    data class ProgressBackup(
        @SerialName("word_id") val wordId: Int,
        @SerialName("ease_factor") val easeFactor: Float,
        @SerialName("interval_days") val intervalDays: Int,
        val repetitions: Int,
        @SerialName("next_review_date") val nextReviewDate: Long,
        @SerialName("last_reviewed_date") val lastReviewedDate: Long? = null,
        @SerialName("times_correct") val timesCorrect: Int = 0,
        @SerialName("times_incorrect") val timesIncorrect: Int = 0,
        @SerialName("is_learned") val isLearned: Boolean = false,
        @SerialName("is_excluded") val isExcluded: Boolean = false
    )

    @Serializable
    data class BookmarkBackup(
        @SerialName("word_id") val wordId: Int,
        @SerialName("created_at") val createdAt: Long
    )

    @Serializable
    data class WrongAnswerBackup(
        @SerialName("word_id") val wordId: Int,
        @SerialName("wrong_answer") val wrongAnswer: String,
        @SerialName("correct_answer") val correctAnswer: String,
        @SerialName("quiz_type") val quizType: String,
        @SerialName("created_at") val createdAt: Long
    )

    @Serializable
    data class KnownItemBackup(
        @SerialName("item_id") val itemId: Int,
        @SerialName("item_type") val itemType: String,
        @SerialName("marked_at") val markedAt: Long
    )

    @Serializable
    data class UserWordMeaningBackup(
        @SerialName("word_id") val wordId: Int,
        val meaning: String,
        @SerialName("is_primary") val isPrimary: Boolean,
        @SerialName("created_at") val createdAt: Long
    )

    /** 복원 결과 요약 (걸러진 행 수 포함). */
    data class RestoreResult(
        val progressCount: Int,
        val bookmarkCount: Int,
        val wrongAnswerCount: Int,
        val knownItemCount: Int,
        val skippedCount: Int
    )

    suspend fun exportToJson(): String {
        val backup = BackupFile(
            exportedAt = System.currentTimeMillis(),
            settings = SettingsBackup(
                darkMode = userPreferences.darkMode.first(),
                themeMode = userPreferences.themeMode.first(),
                ttsSpeed = userPreferences.ttsSpeed.first(),
                dailyGoal = userPreferences.dailyGoal.first(),
                notificationEnabled = userPreferences.notificationEnabled.first(),
                notificationHour = userPreferences.notificationHour.first(),
                notificationMinute = userPreferences.notificationMinute.first(),
                streakDays = userPreferences.streakDays.first(),
                lastStudyDate = userPreferences.lastStudyDate.first(),
                hasCompletedPlacementTest = userPreferences.hasCompletedPlacementTest.first(),
                recommendedStage = userPreferences.recommendedStage.first()
            ),
            learningProgress = backupDao.getAllLearningProgress().map {
                ProgressBackup(
                    it.wordId, it.easeFactor, it.intervalDays, it.repetitions,
                    it.nextReviewDate, it.lastReviewedDate, it.timesCorrect,
                    it.timesIncorrect, it.isLearned, it.isExcluded
                )
            },
            bookmarks = backupDao.getAllBookmarks().map { BookmarkBackup(it.wordId, it.createdAt) },
            wrongAnswers = backupDao.getAllWrongAnswers().map {
                WrongAnswerBackup(it.wordId, it.wrongAnswer, it.correctAnswer, it.quizType, it.createdAt)
            },
            knownItems = backupDao.getAllKnownItems().map {
                KnownItemBackup(it.itemId, it.itemType, it.markedAt)
            },
            userWordMeanings = backupDao.getAllUserWordMeanings().map {
                UserWordMeaningBackup(it.wordId, it.meaning, it.isPrimary, it.createdAt)
            }
        )
        return json.encodeToString(BackupFile.serializer(), backup)
    }

    /**
     * JSON 백업을 복원한다. 기존 사용자 데이터는 전부 교체된다.
     *
     * 현재 DB에 없는 word_id를 참조하는 행은 FK 위반을 막기 위해 건너뛴다
     * (앱 버전 차이로 단어 구성이 다른 경우).
     */
    suspend fun importFromJson(raw: String): RestoreResult {
        val backup = json.decodeFromString(BackupFile.serializer(), raw)
        require(backup.version <= FORMAT_VERSION) {
            "지원하지 않는 백업 파일 버전입니다 (v${backup.version})"
        }

        val validWordIds = backupDao.getAllWordIds().toHashSet()
        val progress = backup.learningProgress.filter { it.wordId in validWordIds }
        val bookmarks = backup.bookmarks.filter { it.wordId in validWordIds }
        val wrongAnswers = backup.wrongAnswers.filter { it.wordId in validWordIds }
        val userWordMeanings = backup.userWordMeanings.filter { it.wordId in validWordIds }
        val skipped = (backup.learningProgress.size - progress.size) +
            (backup.bookmarks.size - bookmarks.size) +
            (backup.wrongAnswers.size - wrongAnswers.size) +
            (backup.userWordMeanings.size - userWordMeanings.size)

        backupDao.replaceAll(
            learningProgress = progress.map {
                LearningProgressEntity(
                    wordId = it.wordId, easeFactor = it.easeFactor,
                    intervalDays = it.intervalDays, repetitions = it.repetitions,
                    nextReviewDate = it.nextReviewDate, lastReviewedDate = it.lastReviewedDate,
                    timesCorrect = it.timesCorrect, timesIncorrect = it.timesIncorrect,
                    isLearned = it.isLearned, isExcluded = it.isExcluded
                )
            },
            bookmarks = bookmarks.map { BookmarkEntity(wordId = it.wordId, createdAt = it.createdAt) },
            wrongAnswers = wrongAnswers.map {
                WrongAnswerEntity(
                    wordId = it.wordId, wrongAnswer = it.wrongAnswer,
                    correctAnswer = it.correctAnswer, quizType = it.quizType, createdAt = it.createdAt
                )
            },
            knownItems = backup.knownItems.map {
                KnownItemEntity(itemId = it.itemId, itemType = it.itemType, markedAt = it.markedAt)
            },
            userWordMeanings = userWordMeanings.map {
                UserWordMeaningEntity(
                    wordId = it.wordId, meaning = it.meaning,
                    isPrimary = it.isPrimary, createdAt = it.createdAt
                )
            }
        )

        with(backup.settings) {
            userPreferences.setDarkMode(darkMode)
            userPreferences.setThemeMode(themeMode)
            userPreferences.setTtsSpeed(ttsSpeed)
            userPreferences.setDailyGoal(dailyGoal)
            userPreferences.setNotificationTime(notificationHour, notificationMinute)
            userPreferences.setNotificationEnabled(notificationEnabled)
            userPreferences.updateStreak(streakDays, lastStudyDate)
            if (hasCompletedPlacementTest) userPreferences.setPlacementTestCompleted()
            userPreferences.setRecommendedStage(recommendedStage)
        }

        return RestoreResult(
            progressCount = progress.size,
            bookmarkCount = bookmarks.size,
            wrongAnswerCount = wrongAnswers.size,
            knownItemCount = backup.knownItems.size,
            skippedCount = skipped
        )
    }

    companion object {
        const val FORMAT_VERSION = 1
        private val json = Json {
            ignoreUnknownKeys = true  // 향후 필드 추가 시 구버전 앱에서도 읽기 가능
            encodeDefaults = true
        }
    }
}
