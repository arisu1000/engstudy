package com.wcjung.engstudy.domain.usecase

import com.wcjung.engstudy.domain.model.LearningProgress
import com.wcjung.engstudy.domain.usecase.CalculateSpacedRepetitionUseCase.SimpleRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateSpacedRepetitionUseCaseTest {

    private lateinit var useCase: CalculateSpacedRepetitionUseCase

    @Before
    fun setup() {
        useCase = CalculateSpacedRepetitionUseCase()
    }

    @Test
    fun `first correct answer sets interval to 1 day`() {
        val progress = LearningProgress(wordId = 1)
        val result = useCase.calculate(progress, quality = 4)

        assertEquals(1, result.intervalDays)
        assertEquals(1, result.repetitions)
    }

    @Test
    fun `second correct answer sets interval to 6 days`() {
        val progress = LearningProgress(wordId = 1, repetitions = 1, intervalDays = 1)
        val result = useCase.calculate(progress, quality = 4)

        assertEquals(6, result.intervalDays)
        assertEquals(2, result.repetitions)
    }

    @Test
    fun `third correct answer multiplies interval by ease factor`() {
        val progress = LearningProgress(
            wordId = 1,
            repetitions = 2,
            intervalDays = 6,
            easeFactor = 2.5f
        )
        val result = useCase.calculate(progress, quality = 4)

        // 6 * newEaseFactor (2.5 + 0.1 - 0.08 - 0.02 = 2.5) = 15
        assertEquals(15, result.intervalDays)
        assertEquals(3, result.repetitions)
    }

    @Test
    fun `incorrect answer resets interval and repetitions`() {
        val progress = LearningProgress(
            wordId = 1,
            repetitions = 5,
            intervalDays = 30,
            easeFactor = 2.5f
        )
        val result = useCase.calculate(progress, quality = 2)

        assertEquals(1, result.intervalDays)
        assertEquals(0, result.repetitions)
    }

    @Test
    fun `ease factor never goes below 1_3`() {
        val progress = LearningProgress(wordId = 1, easeFactor = 1.3f)
        val result = useCase.calculate(progress, quality = 0)

        assertEquals(1.3f, result.easeFactor)
    }

    @Test
    fun `perfect quality increases ease factor`() {
        val progress = LearningProgress(wordId = 1, easeFactor = 2.5f)
        val result = useCase.calculate(progress, quality = 5)

        assertTrue(result.easeFactor > 2.5f)
    }

    @Test
    fun `low quality decreases ease factor`() {
        val progress = LearningProgress(wordId = 1, easeFactor = 2.5f)
        val result = useCase.calculate(progress, quality = 3)

        assertTrue(result.easeFactor < 2.5f)
    }

    @Test
    fun `interval of 21 or more means word is learned`() {
        val progress = LearningProgress(
            wordId = 1,
            repetitions = 2,
            intervalDays = 10,
            easeFactor = 2.5f
        )
        val result = useCase.calculate(progress, quality = 5)

        // interval = 10 * (2.5 + 0.1) = 26
        assertTrue(result.intervalDays >= 21)
    }

    @Test
    fun `nextReviewDate is set to future`() {
        val before = System.currentTimeMillis()
        val progress = LearningProgress(wordId = 1)
        val result = useCase.calculate(progress, quality = 4)

        assertTrue(result.nextReviewDate > before)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `quality below 0 throws exception`() {
        useCase.calculate(LearningProgress(wordId = 1), quality = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `quality above 5 throws exception`() {
        useCase.calculate(LearningProgress(wordId = 1), quality = 6)
    }

    @Test
    fun `qualityFromSimpleRating maps correctly`() {
        assertEquals(1, useCase.qualityFromSimpleRating(SimpleRating.AGAIN))
        assertEquals(3, useCase.qualityFromSimpleRating(SimpleRating.HARD))
        assertEquals(4, useCase.qualityFromSimpleRating(SimpleRating.GOOD))
        assertEquals(5, useCase.qualityFromSimpleRating(SimpleRating.EASY))
    }

    @Test
    fun `boundary quality 3 is considered correct`() {
        val progress = LearningProgress(wordId = 1)
        val result = useCase.calculate(progress, quality = 3)

        assertEquals(1, result.intervalDays)
        assertEquals(1, result.repetitions)
    }

    @Test
    fun `boundary quality 2 is considered incorrect`() {
        val progress = LearningProgress(wordId = 1, repetitions = 3, intervalDays = 15)
        val result = useCase.calculate(progress, quality = 2)

        assertEquals(1, result.intervalDays)
        assertEquals(0, result.repetitions)
    }

    @Test
    fun `multiple consecutive correct answers increase interval progressively`() {
        var progress = LearningProgress(wordId = 1)

        // First correct
        var result = useCase.calculate(progress, quality = 4)
        assertEquals(1, result.intervalDays)
        progress = progress.copy(
            easeFactor = result.easeFactor,
            intervalDays = result.intervalDays,
            repetitions = result.repetitions
        )

        // Second correct
        result = useCase.calculate(progress, quality = 4)
        assertEquals(6, result.intervalDays)
        progress = progress.copy(
            easeFactor = result.easeFactor,
            intervalDays = result.intervalDays,
            repetitions = result.repetitions
        )

        // Third correct
        result = useCase.calculate(progress, quality = 4)
        assertTrue(result.intervalDays > 6)

        // Interval should keep growing
        val thirdInterval = result.intervalDays
        progress = progress.copy(
            easeFactor = result.easeFactor,
            intervalDays = result.intervalDays,
            repetitions = result.repetitions
        )
        result = useCase.calculate(progress, quality = 4)
        assertTrue(result.intervalDays > thirdInterval)
    }
}
