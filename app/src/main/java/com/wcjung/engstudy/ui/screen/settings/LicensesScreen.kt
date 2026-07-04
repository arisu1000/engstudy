package com.wcjung.engstudy.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 데이터 출처 항목. CC BY 등 저작자 표시 의무가 있는 소스는 반드시 이 화면에 노출한다. */
private data class DataSource(
    val name: String,
    val license: String,
    val usage: String,
    val url: String,
    val attribution: String? = null
)

private val dataSources = listOf(
    DataSource(
        name = "Tatoeba",
        license = "CC BY 2.0 FR",
        usage = "문법 예문 및 단어 예문",
        url = "https://tatoeba.org",
        attribution = "예문은 Tatoeba 프로젝트 기여자들이 작성했으며 " +
            "CC BY 2.0 FR 라이선스에 따라 사용됩니다."
    ),
    DataSource(
        name = "kengdic (Joe Speigle)",
        license = "MPL 2.0",
        usage = "영한 단어 사전 데이터",
        url = "https://github.com/garfieldnate/kengdic"
    ),
    DataSource(
        name = "교육부 공공데이터",
        license = "공공누리 (정부 공공저작물)",
        usage = "초·중·고 교육과정 필수 영단어 3,000개",
        url = "https://www.data.go.kr"
    ),
    DataSource(
        name = "Semigradsky/phrasal-verbs",
        license = "MIT",
        usage = "숙어 및 구동사",
        url = "https://github.com/Semigradsky/phrasal-verbs"
    ),
    DataSource(
        name = "wordfreq (Robyn Speer)",
        license = "MIT",
        usage = "단어 빈도 기반 학습 단계(Stage) 분류",
        url = "https://github.com/rspeer/wordfreq"
    ),
    DataSource(
        name = "Free Dictionary API",
        license = "무료 사용",
        usage = "단어 발음 기호 보충",
        url = "https://dictionaryapi.dev"
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("데이터 출처 및 라이선스") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "EngStudy의 학습 콘텐츠는 아래 오픈 데이터를 기반으로 제작되었습니다. " +
                    "훌륭한 데이터를 공개해 주신 모든 기여자들께 감사드립니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            dataSources.forEach { source ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = source.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${source.license} · ${source.usage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = source.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        source.attribution?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
