package com.wcjung.engstudy.data.local.entity

import com.wcjung.engstudy.domain.model.EduLevel
import com.wcjung.engstudy.domain.model.EduWord

fun EduWordEntity.toDomain(): EduWord = EduWord(
    id = id,
    word = word,
    meaning = meaning,
    level = EduLevel.fromKey(level),
    partOfSpeech = partOfSpeech,
    variant1 = variant1,
    variant2 = variant2
)
