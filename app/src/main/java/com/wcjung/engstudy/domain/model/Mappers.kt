package com.wcjung.engstudy.domain.model

import com.wcjung.engstudy.data.local.entity.LearningProgressEntity
import com.wcjung.engstudy.data.local.entity.WordEntity

fun WordEntity.toDomain(): Word = Word(
    id = id,
    word = word,
    pronunciation = pronunciation,
    meaning = meaning,
    meaningType = MeaningType.fromKey(meaningType),
    partOfSpeech = partOfSpeech,
    exampleEn = exampleEn,
    exampleKo = exampleKo,
    stage = Stage.fromLevel(stage),
    domain = Domain.fromKey(domain),
    frequencyRank = frequencyRank,
    difficulty = difficulty,
    synonyms = synonyms?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    antonyms = antonyms?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
    notes = notes
)

fun LearningProgressEntity.toDomain(): LearningProgress = LearningProgress(
    wordId = wordId,
    easeFactor = easeFactor,
    intervalDays = intervalDays,
    repetitions = repetitions,
    nextReviewDate = nextReviewDate,
    lastReviewedDate = lastReviewedDate,
    timesCorrect = timesCorrect,
    timesIncorrect = timesIncorrect,
    isLearned = isLearned
)
