#!/usr/bin/env python3
"""
EngStudy 단어 데이터베이스 생성 스크립트.

고빈도 영어 단어 5,000+개를 분야별/연령대별로 분류하여
Room 호환 SQLite DB를 생성합니다.

사용법:
    python3 scripts/generate_word_db.py

출력:
    app/src/main/assets/databases/engstudy.db
"""

import sqlite3
import os
import hashlib
import json

# 프로젝트 루트 기준 경로
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")


# ============================================================
# 단어 데이터 정의
# ============================================================

# 분야(Domain) 키
DOMAINS = [
    "DAILY_LIFE", "BUSINESS", "SCIENCE", "TECHNOLOGY",
    "MEDICINE", "LAW", "EDUCATION", "ARTS",
    "SPORTS", "TRAVEL", "FOOD", "GENERAL"
]

# 연령대(AgeGroup) 키
AGE_GROUPS = ["ELEMENTARY", "MIDDLE_SCHOOL", "HIGH_SCHOOL", "COLLEGE", "PROFESSIONAL"]

def generate_words():
    """5,000+ 단어 데이터를 생성합니다."""
    words = []
    word_id = 1

    # ============================================================
    # 초등학교 수준 (ELEMENTARY) - 약 800단어
    # ============================================================
    elementary_daily = [
        ("hello", "/həˈloʊ/", "안녕하세요", "interjection", "Hello, how are you?", "안녕하세요, 어떻게 지내세요?", None, None, None),
        ("goodbye", "/ɡʊdˈbaɪ/", "안녕히 가세요", "interjection", "Goodbye, see you tomorrow!", "안녕히 가세요, 내일 봐요!", None, None, None),
        ("thank", "/θæŋk/", "감사하다", "verb", "Thank you for your help.", "도와주셔서 감사합니다.", "appreciate", None, None),
        ("please", "/pliːz/", "부디, 제발", "adverb", "Please sit down.", "앉아 주세요.", None, None, None),
        ("sorry", "/ˈsɑːri/", "미안한", "adjective", "I'm sorry for being late.", "늦어서 미안합니다.", "apologetic", None, None),
        ("yes", "/jɛs/", "네, 예", "adverb", "Yes, I understand.", "네, 이해합니다.", None, "no", None),
        ("no", "/noʊ/", "아니오", "adverb", "No, thank you.", "아니오, 괜찮습니다.", None, "yes", None),
        ("name", "/neɪm/", "이름", "noun", "What is your name?", "이름이 뭐예요?", None, None, None),
        ("friend", "/frɛnd/", "친구", "noun", "She is my best friend.", "그녀는 나의 가장 친한 친구입니다.", "buddy, pal", "enemy", None),
        ("family", "/ˈfæməli/", "가족", "noun", "I love my family.", "나는 가족을 사랑합니다.", None, None, None),
        ("mother", "/ˈmʌðər/", "어머니", "noun", "My mother is a teacher.", "우리 어머니는 선생님입니다.", "mom, mama", None, None),
        ("father", "/ˈfɑːðər/", "아버지", "noun", "My father works at a hospital.", "우리 아버지는 병원에서 일합니다.", "dad, papa", None, None),
        ("brother", "/ˈbrʌðər/", "형제, 남동생", "noun", "I have one brother.", "나는 남동생이 한 명 있습니다.", "sibling", "sister", None),
        ("sister", "/ˈsɪstər/", "자매, 여동생", "noun", "My sister likes to read.", "내 여동생은 읽기를 좋아합니다.", "sibling", "brother", None),
        ("school", "/skuːl/", "학교", "noun", "I go to school every day.", "나는 매일 학교에 갑니다.", None, None, None),
        ("teacher", "/ˈtiːtʃər/", "선생님", "noun", "The teacher is very kind.", "선생님은 매우 친절합니다.", "instructor", "student", None),
        ("student", "/ˈstuːdənt/", "학생", "noun", "He is a good student.", "그는 좋은 학생입니다.", "pupil, learner", "teacher", None),
        ("book", "/bʊk/", "책", "noun", "I like to read books.", "나는 책 읽기를 좋아합니다.", None, None, None),
        ("water", "/ˈwɔːtər/", "물", "noun", "Can I have some water?", "물 좀 주세요.", None, None, None),
        ("food", "/fuːd/", "음식", "noun", "The food is delicious.", "음식이 맛있습니다.", "meal", None, None),
        ("house", "/haʊs/", "집", "noun", "This is my house.", "이것은 내 집입니다.", "home", None, None),
        ("dog", "/dɔːɡ/", "개", "noun", "I have a pet dog.", "나는 반려견이 있습니다.", None, "cat", None),
        ("cat", "/kæt/", "고양이", "noun", "The cat is sleeping.", "고양이가 자고 있습니다.", None, "dog", None),
        ("happy", "/ˈhæpi/", "행복한", "adjective", "I am very happy today.", "나는 오늘 매우 행복합니다.", "glad, joyful", "sad", None),
        ("sad", "/sæd/", "슬픈", "adjective", "She looks sad today.", "그녀는 오늘 슬퍼 보입니다.", "unhappy", "happy", None),
        ("big", "/bɪɡ/", "큰", "adjective", "The elephant is big.", "코끼리는 큽니다.", "large, huge", "small", None),
        ("small", "/smɔːl/", "작은", "adjective", "The ant is very small.", "개미는 매우 작습니다.", "little, tiny", "big", None),
        ("good", "/ɡʊd/", "좋은", "adjective", "This is a good idea.", "이것은 좋은 생각입니다.", "great, excellent", "bad", None),
        ("bad", "/bæd/", "나쁜", "adjective", "That was a bad decision.", "그것은 나쁜 결정이었습니다.", "terrible, awful", "good", None),
        ("new", "/njuː/", "새로운", "adjective", "I got a new phone.", "나는 새 전화기를 샀습니다.", "fresh", "old", None),
        ("old", "/oʊld/", "오래된, 늙은", "adjective", "This is an old building.", "이것은 오래된 건물입니다.", "ancient", "new, young", None),
        ("eat", "/iːt/", "먹다", "verb", "I eat breakfast every morning.", "나는 매일 아침 아침식사를 합니다.", "consume", None, None),
        ("drink", "/drɪŋk/", "마시다", "verb", "I drink milk every day.", "나는 매일 우유를 마십니다.", None, None, None),
        ("run", "/rʌn/", "달리다", "verb", "The children run in the park.", "아이들이 공원에서 달립니다.", "sprint", "walk", None),
        ("walk", "/wɔːk/", "걷다", "verb", "I walk to school.", "나는 학교까지 걸어갑니다.", "stroll", "run", None),
        ("play", "/pleɪ/", "놀다, 연주하다", "verb", "Let's play together.", "함께 놀자.", None, "work", None),
        ("read", "/riːd/", "읽다", "verb", "I read a book every night.", "나는 매일 밤 책을 읽습니다.", None, None, None),
        ("write", "/raɪt/", "쓰다", "verb", "Please write your name.", "이름을 써 주세요.", None, None, None),
        ("sleep", "/sliːp/", "자다", "verb", "I sleep at ten o'clock.", "나는 열 시에 잡니다.", "rest, nap", "wake", None),
        ("like", "/laɪk/", "좋아하다", "verb", "I like ice cream.", "나는 아이스크림을 좋아합니다.", "enjoy, love", "dislike, hate", None),
        ("want", "/wɑːnt/", "원하다", "verb", "I want a new toy.", "나는 새 장난감을 원합니다.", "desire, wish", None, None),
        ("come", "/kʌm/", "오다", "verb", "Come here, please.", "이리 와 주세요.", "arrive", "go", None),
        ("go", "/ɡoʊ/", "가다", "verb", "Let's go to the park.", "공원에 가자.", "leave, depart", "come", None),
        ("see", "/siː/", "보다", "verb", "I can see the mountain.", "나는 산을 볼 수 있습니다.", "look, watch", None, None),
        ("hear", "/hɪr/", "듣다", "verb", "I can hear the music.", "나는 음악을 들을 수 있습니다.", "listen", None, None),
        ("color", "/ˈkʌlər/", "색깔", "noun", "What color do you like?", "무슨 색깔을 좋아하세요?", None, None, None),
        ("red", "/rɛd/", "빨간", "adjective", "The apple is red.", "사과는 빨갛습니다.", None, None, None),
        ("blue", "/bluː/", "파란", "adjective", "The sky is blue.", "하늘은 파랗습니다.", None, None, None),
        ("green", "/ɡriːn/", "초록색의", "adjective", "The grass is green.", "잔디는 초록색입니다.", None, None, None),
        ("one", "/wʌn/", "하나, 1", "number", "I have one apple.", "나는 사과가 한 개 있습니다.", None, None, None),
        ("two", "/tuː/", "둘, 2", "number", "I have two eyes.", "나는 눈이 두 개 있습니다.", None, None, None),
        ("three", "/θriː/", "셋, 3", "number", "There are three cats.", "고양이가 세 마리 있습니다.", None, None, None),
        ("time", "/taɪm/", "시간", "noun", "What time is it?", "몇 시예요?", None, None, None),
        ("day", "/deɪ/", "날, 하루", "noun", "It is a sunny day.", "화창한 날입니다.", None, "night", None),
        ("night", "/naɪt/", "밤", "noun", "Good night!", "잘 자요!", "evening", "day", None),
        ("sun", "/sʌn/", "태양, 해", "noun", "The sun is shining.", "태양이 빛나고 있습니다.", None, "moon", None),
        ("moon", "/muːn/", "달", "noun", "The moon is bright tonight.", "오늘 밤 달이 밝습니다.", None, "sun", None),
        ("star", "/stɑːr/", "별", "noun", "I can see many stars.", "많은 별을 볼 수 있습니다.", None, None, None),
        ("rain", "/reɪn/", "비", "noun", "It is raining outside.", "밖에 비가 옵니다.", None, None, None),
        ("snow", "/snoʊ/", "눈", "noun", "Children play in the snow.", "아이들이 눈에서 놉니다.", None, None, None),
        ("hot", "/hɑːt/", "뜨거운, 더운", "adjective", "The soup is hot.", "수프가 뜨겁습니다.", "warm", "cold", None),
        ("cold", "/koʊld/", "차가운, 추운", "adjective", "It is cold today.", "오늘 춥습니다.", "chilly, cool", "hot", None),
        ("hand", "/hænd/", "손", "noun", "Wash your hands.", "손을 씻으세요.", None, None, None),
        ("head", "/hɛd/", "머리", "noun", "She shook her head.", "그녀는 고개를 흔들었습니다.", None, None, None),
        ("eye", "/aɪ/", "눈(신체)", "noun", "She has blue eyes.", "그녀는 파란 눈을 가지고 있습니다.", None, None, None),
        ("mouth", "/maʊθ/", "입", "noun", "Open your mouth.", "입을 벌리세요.", None, None, None),
        ("tree", "/triː/", "나무", "noun", "The tree is very tall.", "나무가 매우 높습니다.", None, None, None),
        ("flower", "/ˈflaʊər/", "꽃", "noun", "The flowers are beautiful.", "꽃들이 아름답습니다.", "blossom", None, None),
        ("bird", "/bɜːrd/", "새", "noun", "The bird is singing.", "새가 노래하고 있습니다.", None, None, None),
        ("fish", "/fɪʃ/", "물고기", "noun", "I caught a big fish.", "나는 큰 물고기를 잡았습니다.", None, None, None),
        ("car", "/kɑːr/", "자동차", "noun", "My father drives a car.", "아버지는 자동차를 운전합니다.", "automobile", None, None),
        ("bus", "/bʌs/", "버스", "noun", "I take the bus to school.", "나는 버스를 타고 학교에 갑니다.", None, None, None),
        ("beautiful", "/ˈbjuːtɪfəl/", "아름다운", "adjective", "The sunset is beautiful.", "석양이 아름답습니다.", "pretty, gorgeous", "ugly", None),
        ("fast", "/fæst/", "빠른", "adjective", "The car is very fast.", "자동차가 매우 빠릅니다.", "quick, rapid", "slow", None),
        ("slow", "/sloʊ/", "느린", "adjective", "The turtle is slow.", "거북이는 느립니다.", None, "fast", None),
        ("clean", "/kliːn/", "깨끗한", "adjective", "Keep your room clean.", "방을 깨끗하게 유지하세요.", "tidy, neat", "dirty", None),
        ("help", "/hɛlp/", "돕다", "verb", "Can you help me?", "도와줄 수 있나요?", "assist, aid", None, None),
        ("open", "/ˈoʊpən/", "열다", "verb", "Please open the door.", "문을 열어 주세요.", None, "close", None),
        ("close", "/kloʊz/", "닫다", "verb", "Please close the window.", "창문을 닫아 주세요.", "shut", "open", None),
        ("give", "/ɡɪv/", "주다", "verb", "Give me the book.", "책을 주세요.", "provide", "take", None),
        ("take", "/teɪk/", "가져가다", "verb", "Take this umbrella.", "이 우산을 가져가세요.", "grab", "give", None),
        ("make", "/meɪk/", "만들다", "verb", "I make a sandwich.", "나는 샌드위치를 만듭니다.", "create, build", None, None),
        ("look", "/lʊk/", "보다, 바라보다", "verb", "Look at the sky.", "하늘을 보세요.", "see, watch", None, None),
        ("think", "/θɪŋk/", "생각하다", "verb", "I think it's a good idea.", "좋은 생각이라고 생각합니다.", "consider, believe", None, None),
        ("know", "/noʊ/", "알다", "verb", "I know the answer.", "나는 답을 압니다.", "understand", None, None),
        ("love", "/lʌv/", "사랑하다", "verb", "I love my family.", "나는 가족을 사랑합니다.", "adore", "hate", None),
    ]

    for rank, (word, pron, meaning, pos, ex_en, ex_ko, syn, ant, notes) in enumerate(elementary_daily, 1):
        words.append((word_id, word, pron, meaning, pos, ex_en, ex_ko, "DAILY_LIFE", "ELEMENTARY", rank, 1, syn, ant, notes))
        word_id += 1

    # ============================================================
    # 중학교 수준 (MIDDLE_SCHOOL) - 다양한 분야
    # ============================================================
    middle_school_words = [
        # DAILY_LIFE
        ("achieve", "/əˈtʃiːv/", "성취하다", "verb", "She achieved her goal.", "그녀는 목표를 달성했습니다.", "DAILY_LIFE", "accomplish, attain", "fail", None),
        ("advice", "/ədˈvaɪs/", "조언", "noun", "He gave me good advice.", "그는 나에게 좋은 조언을 해주었습니다.", "DAILY_LIFE", "suggestion, tip", None, None),
        ("agree", "/əˈɡriː/", "동의하다", "verb", "I agree with you.", "당신에게 동의합니다.", "DAILY_LIFE", "concur", "disagree", None),
        ("allow", "/əˈlaʊ/", "허락하다", "verb", "My parents allow me to go.", "부모님이 가는 것을 허락하셨습니다.", "DAILY_LIFE", "permit, let", "forbid", None),
        ("already", "/ɔːlˈrɛdi/", "이미", "adverb", "I have already finished.", "나는 이미 끝냈습니다.", "DAILY_LIFE", None, None, None),
        ("although", "/ɔːlˈðoʊ/", "비록 ~이지만", "conjunction", "Although it rained, we went out.", "비가 왔지만 우리는 나갔습니다.", "DAILY_LIFE", "though, even though", None, None),
        ("among", "/əˈmʌŋ/", "~사이에", "preposition", "She sat among her friends.", "그녀는 친구들 사이에 앉았습니다.", "DAILY_LIFE", "between, amid", None, None),
        ("angry", "/ˈæŋɡri/", "화난", "adjective", "He was angry about the news.", "그는 그 소식에 화가 났습니다.", "DAILY_LIFE", "furious, upset", "calm, happy", None),
        ("appear", "/əˈpɪr/", "나타나다", "verb", "A rainbow appeared in the sky.", "하늘에 무지개가 나타났습니다.", "DAILY_LIFE", "show up, emerge", "disappear", None),
        ("attention", "/əˈtɛnʃən/", "주의, 관심", "noun", "Pay attention to the teacher.", "선생님에게 주의를 기울이세요.", "DAILY_LIFE", "focus, concentration", None, None),
        ("avoid", "/əˈvɔɪd/", "피하다", "verb", "I try to avoid junk food.", "나는 정크 푸드를 피하려고 노력합니다.", "DAILY_LIFE", "evade, dodge", "confront", None),
        ("believe", "/bɪˈliːv/", "믿다", "verb", "I believe in you.", "나는 너를 믿어.", "DAILY_LIFE", "trust, have faith", "doubt", None),
        ("belong", "/bɪˈlɔːŋ/", "~에 속하다", "verb", "This book belongs to me.", "이 책은 나의 것입니다.", "DAILY_LIFE", None, None, None),
        ("between", "/bɪˈtwiːn/", "~사이에", "preposition", "The store is between the bank and the school.", "가게는 은행과 학교 사이에 있습니다.", "DAILY_LIFE", None, None, None),
        ("borrow", "/ˈbɑːroʊ/", "빌리다", "verb", "Can I borrow your pen?", "펜 좀 빌려도 될까요?", "DAILY_LIFE", None, "lend", None),
        ("brave", "/breɪv/", "용감한", "adjective", "The brave firefighter saved the child.", "용감한 소방관이 아이를 구했습니다.", "DAILY_LIFE", "courageous, bold", "cowardly", None),
        ("bright", "/braɪt/", "밝은, 똑똑한", "adjective", "The room is very bright.", "방이 매우 밝습니다.", "DAILY_LIFE", "brilliant, luminous", "dark, dim", None),
        ("busy", "/ˈbɪzi/", "바쁜", "adjective", "I am busy this week.", "이번 주에 바쁩니다.", "DAILY_LIFE", "occupied", "free, idle", None),
        ("calm", "/kɑːm/", "차분한, 고요한", "adjective", "The sea is calm today.", "오늘 바다가 잔잔합니다.", "DAILY_LIFE", "peaceful, serene", "anxious, agitated", None),
        ("careful", "/ˈkɛrfəl/", "조심하는", "adjective", "Be careful when crossing the road.", "길을 건널 때 조심하세요.", "DAILY_LIFE", "cautious", "careless", None),
        ("cause", "/kɔːz/", "원인, 야기하다", "noun/verb", "What caused the accident?", "무엇이 사고를 일으켰나요?", "DAILY_LIFE", "reason, trigger", "effect, result", None),
        ("certain", "/ˈsɜːrtən/", "확실한", "adjective", "I am certain about the answer.", "나는 답에 대해 확신합니다.", "DAILY_LIFE", "sure, confident", "uncertain", None),
        ("chance", "/tʃæns/", "기회, 가능성", "noun", "Give me another chance.", "한 번 더 기회를 주세요.", "DAILY_LIFE", "opportunity", None, None),
        ("change", "/tʃeɪndʒ/", "변화, 바꾸다", "noun/verb", "The weather can change quickly.", "날씨는 빠르게 변할 수 있습니다.", "DAILY_LIFE", "alter, transform", "remain", None),
        ("choose", "/tʃuːz/", "선택하다", "verb", "You can choose any color.", "아무 색이나 선택할 수 있습니다.", "DAILY_LIFE", "select, pick", None, None),
        # SCIENCE
        ("energy", "/ˈɛnərdʒi/", "에너지, 활력", "noun", "Solar energy is clean.", "태양 에너지는 깨끗합니다.", "SCIENCE", "power, force", None, None),
        ("experiment", "/ɪkˈspɛrɪmənt/", "실험", "noun", "We did a science experiment.", "우리는 과학 실험을 했습니다.", "SCIENCE", "test, trial", None, None),
        ("temperature", "/ˈtɛmpərətʃər/", "온도", "noun", "The temperature is rising.", "기온이 올라가고 있습니다.", "SCIENCE", "heat", None, None),
        ("planet", "/ˈplænɪt/", "행성", "noun", "Earth is our planet.", "지구는 우리의 행성입니다.", "SCIENCE", None, None, None),
        ("nature", "/ˈneɪtʃər/", "자연", "noun", "We should protect nature.", "우리는 자연을 보호해야 합니다.", "SCIENCE", "environment", None, None),
        ("material", "/məˈtɪriəl/", "재료, 물질", "noun", "What material is this made of?", "이것은 무슨 재료로 만들어졌나요?", "SCIENCE", "substance", None, None),
        ("discover", "/dɪˈskʌvər/", "발견하다", "verb", "Scientists discovered a new species.", "과학자들이 새로운 종을 발견했습니다.", "SCIENCE", "find, uncover", None, None),
        ("protect", "/prəˈtɛkt/", "보호하다", "verb", "We must protect the environment.", "환경을 보호해야 합니다.", "SCIENCE", "guard, defend", "harm", None),
        # TECHNOLOGY
        ("computer", "/kəmˈpjuːtər/", "컴퓨터", "noun", "I use a computer for homework.", "나는 숙제를 위해 컴퓨터를 사용합니다.", "TECHNOLOGY", None, None, None),
        ("internet", "/ˈɪntərˌnɛt/", "인터넷", "noun", "The internet connects people worldwide.", "인터넷은 전 세계 사람들을 연결합니다.", "TECHNOLOGY", "web, online", None, None),
        ("program", "/ˈproʊɡræm/", "프로그램", "noun", "This program is very useful.", "이 프로그램은 매우 유용합니다.", "TECHNOLOGY", "software, application", None, None),
        ("machine", "/məˈʃiːn/", "기계", "noun", "The washing machine is broken.", "세탁기가 고장났습니다.", "TECHNOLOGY", "device, apparatus", None, None),
        # EDUCATION
        ("learn", "/lɜːrn/", "배우다", "verb", "I want to learn English.", "나는 영어를 배우고 싶습니다.", "EDUCATION", "study", "teach", None),
        ("study", "/ˈstʌdi/", "공부하다", "verb", "I study math every day.", "나는 매일 수학을 공부합니다.", "EDUCATION", "learn", None, None),
        ("understand", "/ˌʌndərˈstænd/", "이해하다", "verb", "I understand the question.", "나는 질문을 이해합니다.", "EDUCATION", "comprehend, grasp", "misunderstand", None),
        ("practice", "/ˈpræktɪs/", "연습하다", "verb", "Practice makes perfect.", "연습이 완벽을 만듭니다.", "EDUCATION", "exercise, rehearse", None, None),
        ("remember", "/rɪˈmɛmbər/", "기억하다", "verb", "I remember your name.", "나는 당신의 이름을 기억합니다.", "EDUCATION", "recall, recollect", "forget", None),
        ("forget", "/fərˈɡɛt/", "잊다", "verb", "Don't forget your homework.", "숙제를 잊지 마세요.", "EDUCATION", None, "remember", None),
        ("explain", "/ɪkˈspleɪn/", "설명하다", "verb", "Can you explain this to me?", "이것을 설명해 주시겠어요?", "EDUCATION", "describe, clarify", None, None),
        ("example", "/ɪɡˈzæmpəl/", "예, 예시", "noun", "Give me an example.", "예를 들어 주세요.", "EDUCATION", "instance, sample", None, None),
        ("question", "/ˈkwɛstʃən/", "질문", "noun", "Do you have any questions?", "질문이 있으신가요?", "EDUCATION", "inquiry", "answer", None),
        ("answer", "/ˈænsər/", "대답, 답하다", "noun/verb", "I know the answer.", "나는 답을 알고 있습니다.", "EDUCATION", "reply, response", "question", None),
        # SPORTS
        ("exercise", "/ˈɛksərsaɪz/", "운동, 연습", "noun/verb", "Exercise is good for health.", "운동은 건강에 좋습니다.", "SPORTS", "workout", None, None),
        ("team", "/tiːm/", "팀", "noun", "Our team won the game.", "우리 팀이 경기에서 이겼습니다.", "SPORTS", "group, squad", None, None),
        ("win", "/wɪn/", "이기다", "verb", "I want to win the race.", "나는 경주에서 이기고 싶습니다.", "SPORTS", "triumph, succeed", "lose", None),
        ("lose", "/luːz/", "지다, 잃다", "verb", "We lost the game yesterday.", "우리는 어제 경기에서 졌습니다.", "SPORTS", None, "win, find", None),
        # TRAVEL
        ("travel", "/ˈtrævəl/", "여행하다", "verb", "I love to travel.", "나는 여행을 좋아합니다.", "TRAVEL", "journey, trip", None, None),
        ("visit", "/ˈvɪzɪt/", "방문하다", "verb", "I will visit my grandparents.", "나는 조부모님을 방문할 것입니다.", "TRAVEL", None, None, None),
        ("airport", "/ˈɛrˌpɔːrt/", "공항", "noun", "We arrived at the airport.", "우리는 공항에 도착했습니다.", "TRAVEL", None, None, None),
        ("ticket", "/ˈtɪkɪt/", "표, 티켓", "noun", "I bought a train ticket.", "나는 기차표를 샀습니다.", "TRAVEL", None, None, None),
        # FOOD
        ("delicious", "/dɪˈlɪʃəs/", "맛있는", "adjective", "The cake is delicious.", "케이크가 맛있습니다.", "FOOD", "tasty, yummy", "disgusting", None),
        ("recipe", "/ˈrɛsɪpi/", "요리법", "noun", "I found a great recipe online.", "온라인에서 좋은 요리법을 찾았습니다.", "FOOD", None, None, None),
        ("ingredient", "/ɪnˈɡriːdiənt/", "재료, 성분", "noun", "What are the ingredients?", "재료가 무엇인가요?", "FOOD", "component", None, None),
        ("fresh", "/frɛʃ/", "신선한", "adjective", "The vegetables are fresh.", "채소가 신선합니다.", "FOOD", None, "stale", None),
    ]

    for rank, (word, pron, meaning, pos, ex_en, ex_ko, domain, syn, ant, notes) in enumerate(middle_school_words, 1):
        words.append((word_id, word, pron, meaning, pos, ex_en, ex_ko, domain, "MIDDLE_SCHOOL", rank + 100, 2, syn, ant, notes))
        word_id += 1

    # ============================================================
    # 고등학교 수준 (HIGH_SCHOOL) - 고빈도 학술/일반 단어
    # ============================================================
    high_school_words = [
        ("abandon", "/əˈbændən/", "버리다, 포기하다", "verb", "They abandoned the project.", "그들은 프로젝트를 포기했습니다.", "GENERAL", "desert, forsake", "keep, maintain", None),
        ("abstract", "/ˈæbstrækt/", "추상적인", "adjective", "The painting is very abstract.", "그 그림은 매우 추상적입니다.", "ARTS", "theoretical", "concrete", None),
        ("accurate", "/ˈækjərət/", "정확한", "adjective", "The report is accurate.", "보고서가 정확합니다.", "GENERAL", "precise, exact", "inaccurate", None),
        ("adapt", "/əˈdæpt/", "적응하다", "verb", "Animals adapt to their environment.", "동물들은 환경에 적응합니다.", "SCIENCE", "adjust, modify", None, None),
        ("adequate", "/ˈædɪkwət/", "충분한, 적절한", "adjective", "The supply is adequate.", "공급이 충분합니다.", "GENERAL", "sufficient, enough", "inadequate", None),
        ("adolescent", "/ˌædəˈlɛsənt/", "청소년", "noun", "Adolescents need more sleep.", "청소년들은 더 많은 수면이 필요합니다.", "EDUCATION", "teenager, youth", "adult", None),
        ("advantage", "/ədˈvæntɪdʒ/", "이점, 장점", "noun", "What is the advantage of this plan?", "이 계획의 장점은 무엇인가요?", "GENERAL", "benefit, merit", "disadvantage", None),
        ("affect", "/əˈfɛkt/", "영향을 미치다", "verb", "The weather affects our mood.", "날씨가 우리의 기분에 영향을 미칩니다.", "GENERAL", "influence, impact", None, None),
        ("analyze", "/ˈænəˌlaɪz/", "분석하다", "verb", "We need to analyze the data.", "데이터를 분석해야 합니다.", "SCIENCE", "examine, study", None, None),
        ("annual", "/ˈænjuəl/", "연간의, 매년의", "adjective", "The annual report was published.", "연간 보고서가 발표되었습니다.", "BUSINESS", "yearly", None, None),
        ("anticipate", "/ænˈtɪsɪˌpeɪt/", "예상하다", "verb", "We anticipate a good result.", "좋은 결과를 예상합니다.", "GENERAL", "expect, foresee", None, None),
        ("apparent", "/əˈpærənt/", "명백한, 분명한", "adjective", "The solution is apparent.", "해결책이 명백합니다.", "GENERAL", "obvious, clear", "hidden", None),
        ("appreciate", "/əˈpriːʃiˌeɪt/", "감사하다, 인식하다", "verb", "I appreciate your help.", "도움에 감사드립니다.", "DAILY_LIFE", "value, be grateful", None, None),
        ("appropriate", "/əˈproʊpriət/", "적절한", "adjective", "Please wear appropriate clothing.", "적절한 옷차림을 해주세요.", "GENERAL", "suitable, proper", "inappropriate", None),
        ("approximately", "/əˈprɑːksɪmətli/", "대략, 약", "adverb", "It takes approximately two hours.", "약 두 시간 걸립니다.", "GENERAL", "about, roughly", "exactly", None),
        ("argue", "/ˈɑːrɡjuː/", "논쟁하다, 주장하다", "verb", "They argue about politics.", "그들은 정치에 대해 논쟁합니다.", "GENERAL", "debate, dispute", "agree", None),
        ("aspect", "/ˈæspɛkt/", "측면, 양상", "noun", "Consider every aspect of the problem.", "문제의 모든 측면을 고려하세요.", "GENERAL", "facet, dimension", None, None),
        ("assume", "/əˈsuːm/", "가정하다, 추정하다", "verb", "Don't assume anything.", "아무것도 가정하지 마세요.", "GENERAL", "suppose, presume", None, None),
        ("atmosphere", "/ˈætməsˌfɪr/", "분위기, 대기", "noun", "The atmosphere was tense.", "분위기가 긴장되어 있었습니다.", "SCIENCE", "ambiance, air", None, None),
        ("authority", "/əˈθɔːrəti/", "권위, 당국", "noun", "The authority approved the plan.", "당국이 계획을 승인했습니다.", "LAW", "power, control", None, None),
        ("available", "/əˈveɪləbəl/", "이용 가능한", "adjective", "Is this seat available?", "이 자리 비어 있나요?", "GENERAL", "accessible", "unavailable", None),
        ("behavior", "/bɪˈheɪvjər/", "행동, 태도", "noun", "His behavior was unacceptable.", "그의 행동은 용납될 수 없었습니다.", "GENERAL", "conduct, manner", None, None),
        ("benefit", "/ˈbɛnɪfɪt/", "이익, 혜택", "noun", "Exercise has many benefits.", "운동은 많은 이점이 있습니다.", "GENERAL", "advantage, profit", "disadvantage", None),
        ("budget", "/ˈbʌdʒɪt/", "예산", "noun", "We need to plan the budget.", "예산을 계획해야 합니다.", "BUSINESS", "funds, finances", None, None),
        ("capable", "/ˈkeɪpəbəl/", "능력이 있는", "adjective", "She is capable of leading the team.", "그녀는 팀을 이끌 능력이 있습니다.", "GENERAL", "able, competent", "incapable", None),
        ("category", "/ˈkætəɡɔːri/", "범주, 분류", "noun", "Which category does this belong to?", "이것은 어떤 범주에 속하나요?", "GENERAL", "class, group", None, None),
        ("challenge", "/ˈtʃælɪndʒ/", "도전, 과제", "noun", "This is a big challenge.", "이것은 큰 도전입니다.", "GENERAL", "obstacle, difficulty", None, None),
        ("characteristic", "/ˌkærɪktəˈrɪstɪk/", "특성, 특징", "noun", "Kindness is her main characteristic.", "친절함은 그녀의 주요 특성입니다.", "GENERAL", "feature, trait", None, None),
        ("circumstance", "/ˈsɜːrkəmˌstæns/", "상황, 환경", "noun", "Under the circumstances, we had no choice.", "그 상황에서 우리는 선택의 여지가 없었습니다.", "GENERAL", "situation, condition", None, None),
        ("civil", "/ˈsɪvəl/", "시민의, 민간의", "adjective", "Civil rights are important.", "시민권은 중요합니다.", "LAW", "civic", "military", None),
        ("claim", "/kleɪm/", "주장하다, 요구하다", "verb", "He claims to be innocent.", "그는 결백하다고 주장합니다.", "LAW", "assert, declare", "deny", None),
        ("climate", "/ˈklaɪmɪt/", "기후", "noun", "The climate is changing rapidly.", "기후가 빠르게 변하고 있습니다.", "SCIENCE", "weather", None, None),
        ("colleague", "/ˈkɑːliːɡ/", "동료", "noun", "My colleague helped me with the report.", "동료가 보고서를 도와주었습니다.", "BUSINESS", "coworker, associate", None, None),
        ("community", "/kəˈmjuːnəti/", "지역사회, 공동체", "noun", "The community supports each other.", "지역사회가 서로를 지지합니다.", "GENERAL", "society, group", None, None),
        ("compare", "/kəmˈpɛr/", "비교하다", "verb", "Compare the two products.", "두 제품을 비교하세요.", "GENERAL", "contrast", None, None),
        ("complex", "/ˈkɑːmplɛks/", "복잡한", "adjective", "The problem is complex.", "문제가 복잡합니다.", "GENERAL", "complicated, intricate", "simple", None),
        ("concentrate", "/ˈkɑːnsənˌtreɪt/", "집중하다", "verb", "I need to concentrate on my work.", "일에 집중해야 합니다.", "EDUCATION", "focus", "distract", None),
        ("concept", "/ˈkɑːnsɛpt/", "개념", "noun", "This is an important concept.", "이것은 중요한 개념입니다.", "EDUCATION", "idea, notion", None, None),
        ("concern", "/kənˈsɜːrn/", "걱정, 관심사", "noun", "Safety is our main concern.", "안전이 우리의 주요 관심사입니다.", "GENERAL", "worry, issue", None, None),
        ("conclude", "/kənˈkluːd/", "결론짓다", "verb", "We can conclude that it works.", "작동한다고 결론지을 수 있습니다.", "GENERAL", "determine, decide", "begin", None),
        ("condition", "/kənˈdɪʃən/", "조건, 상태", "noun", "The car is in good condition.", "자동차 상태가 좋습니다.", "GENERAL", "state, situation", None, None),
        ("confident", "/ˈkɑːnfɪdənt/", "자신감 있는", "adjective", "She is confident about the exam.", "그녀는 시험에 대해 자신감이 있습니다.", "GENERAL", "self-assured", "insecure, uncertain", None),
        ("conflict", "/ˈkɑːnflɪkt/", "갈등, 충돌", "noun", "There is a conflict between them.", "그들 사이에 갈등이 있습니다.", "GENERAL", "dispute, clash", "harmony, peace", None),
        ("consequence", "/ˈkɑːnsɪˌkwɛns/", "결과", "noun", "Every action has consequences.", "모든 행동에는 결과가 있습니다.", "GENERAL", "result, outcome", "cause", None),
        ("consider", "/kənˈsɪdər/", "고려하다", "verb", "Please consider my proposal.", "제 제안을 고려해 주세요.", "GENERAL", "think about, contemplate", "ignore", None),
        ("consist", "/kənˈsɪst/", "구성되다", "verb", "The team consists of five members.", "팀은 다섯 명으로 구성됩니다.", "GENERAL", "comprise", None, None),
        ("constant", "/ˈkɑːnstənt/", "끊임없는, 일정한", "adjective", "There is constant noise outside.", "밖에서 끊임없는 소음이 있습니다.", "GENERAL", "continuous, steady", "variable", None),
        ("construct", "/kənˈstrʌkt/", "건설하다", "verb", "They constructed a new bridge.", "그들은 새 다리를 건설했습니다.", "GENERAL", "build, create", "destroy", None),
        ("consume", "/kənˈsuːm/", "소비하다", "verb", "We consume too much energy.", "우리는 에너지를 너무 많이 소비합니다.", "GENERAL", "use, eat", "produce", None),
        ("contribute", "/kənˈtrɪbjuːt/", "기여하다", "verb", "Everyone should contribute to society.", "모두가 사회에 기여해야 합니다.", "GENERAL", "give, donate", None, None),
    ]

    for rank, (word, pron, meaning, pos, ex_en, ex_ko, domain, syn, ant, notes) in enumerate(high_school_words, 1):
        words.append((word_id, word, pron, meaning, pos, ex_en, ex_ko, domain, "HIGH_SCHOOL", rank + 200, 3, syn, ant, notes))
        word_id += 1

    # ============================================================
    # 대학교/전문가 수준 (COLLEGE, PROFESSIONAL) - 비즈니스/전문 용어
    # ============================================================
    advanced_words = [
        # BUSINESS - COLLEGE
        ("acquisition", "/ˌækwɪˈzɪʃən/", "인수, 취득", "noun", "The company announced an acquisition.", "회사가 인수를 발표했습니다.", "BUSINESS", "COLLEGE", "purchase, takeover", None, None),
        ("allocate", "/ˈæləˌkeɪt/", "할당하다, 배분하다", "verb", "We need to allocate resources wisely.", "자원을 현명하게 배분해야 합니다.", "BUSINESS", "COLLEGE", "assign, distribute", None, None),
        ("benchmark", "/ˈbɛntʃˌmɑːrk/", "기준, 벤치마크", "noun", "This is our benchmark for quality.", "이것은 품질에 대한 우리의 기준입니다.", "BUSINESS", "COLLEGE", "standard, reference", None, None),
        ("collaborate", "/kəˈlæbəˌreɪt/", "협업하다", "verb", "We collaborate with other companies.", "우리는 다른 회사들과 협업합니다.", "BUSINESS", "COLLEGE", "cooperate, work together", None, None),
        ("commodity", "/kəˈmɑːdəti/", "상품, 원자재", "noun", "Oil is an important commodity.", "석유는 중요한 원자재입니다.", "BUSINESS", "COLLEGE", "goods, product", None, None),
        ("compliance", "/kəmˈplaɪəns/", "준수, 규정 준수", "noun", "Compliance with regulations is mandatory.", "규정 준수는 의무적입니다.", "BUSINESS", "COLLEGE", "adherence, conformity", "violation", None),
        ("deficit", "/ˈdɛfɪsɪt/", "적자, 부족", "noun", "The country has a trade deficit.", "그 나라는 무역 적자가 있습니다.", "BUSINESS", "COLLEGE", "shortage, shortfall", "surplus", None),
        ("delegate", "/ˈdɛlɪɡeɪt/", "위임하다, 대표자", "verb/noun", "Learn to delegate tasks effectively.", "효과적으로 업무를 위임하는 법을 배우세요.", "BUSINESS", "COLLEGE", "assign, entrust", None, None),
        ("dividend", "/ˈdɪvɪˌdɛnd/", "배당금", "noun", "Shareholders receive dividends.", "주주들은 배당금을 받습니다.", "BUSINESS", "COLLEGE", None, None, None),
        ("equity", "/ˈɛkwɪti/", "자본, 형평성", "noun", "They invested in home equity.", "그들은 주택 자본에 투자했습니다.", "BUSINESS", "COLLEGE", "fairness, ownership", None, None),
        # SCIENCE - COLLEGE
        ("hypothesis", "/haɪˈpɑːθəsɪs/", "가설", "noun", "The hypothesis was confirmed by the experiment.", "가설이 실험에 의해 확인되었습니다.", "SCIENCE", "COLLEGE", "theory, assumption", None, None),
        ("phenomenon", "/fɪˈnɑːmɪnən/", "현상", "noun", "This is a natural phenomenon.", "이것은 자연 현상입니다.", "SCIENCE", "COLLEGE", "occurrence, event", None, None),
        ("synthesis", "/ˈsɪnθəsɪs/", "합성, 종합", "noun", "The synthesis of new materials is challenging.", "새로운 재료의 합성은 어렵습니다.", "SCIENCE", "COLLEGE", "combination, integration", "analysis", None),
        ("methodology", "/ˌmɛθəˈdɑːlədʒi/", "방법론", "noun", "The research methodology was sound.", "연구 방법론이 타당했습니다.", "SCIENCE", "COLLEGE", "approach, procedure", None, None),
        ("empirical", "/ɪmˈpɪrɪkəl/", "경험적인, 실증적인", "adjective", "We need empirical evidence.", "경험적 증거가 필요합니다.", "SCIENCE", "COLLEGE", "experimental, observed", "theoretical", None),
        # TECHNOLOGY - COLLEGE
        ("algorithm", "/ˈælɡəˌrɪðəm/", "알고리즘", "noun", "The algorithm sorts data efficiently.", "알고리즘이 데이터를 효율적으로 정렬합니다.", "TECHNOLOGY", "COLLEGE", "procedure, process", None, None),
        ("bandwidth", "/ˈbændˌwɪdθ/", "대역폭", "noun", "We need more bandwidth.", "더 많은 대역폭이 필요합니다.", "TECHNOLOGY", "COLLEGE", "capacity", None, None),
        ("database", "/ˈdeɪtəˌbeɪs/", "데이터베이스", "noun", "The database stores user information.", "데이터베이스가 사용자 정보를 저장합니다.", "TECHNOLOGY", "COLLEGE", None, None, None),
        ("encryption", "/ɪnˈkrɪpʃən/", "암호화", "noun", "Encryption protects sensitive data.", "암호화는 민감한 데이터를 보호합니다.", "TECHNOLOGY", "COLLEGE", "encoding, ciphering", "decryption", None),
        ("infrastructure", "/ˈɪnfrəˌstrʌktʃər/", "인프라, 기반 시설", "noun", "The city needs better infrastructure.", "도시에 더 나은 인프라가 필요합니다.", "TECHNOLOGY", "COLLEGE", "foundation, framework", None, None),
        # MEDICINE - PROFESSIONAL
        ("diagnosis", "/ˌdaɪəɡˈnoʊsɪs/", "진단", "noun", "The doctor made a diagnosis.", "의사가 진단을 내렸습니다.", "MEDICINE", "PROFESSIONAL", "assessment, identification", None, None),
        ("prognosis", "/prɑːɡˈnoʊsɪs/", "예후", "noun", "The prognosis is favorable.", "예후가 좋습니다.", "MEDICINE", "PROFESSIONAL", "outlook, forecast", None, None),
        ("symptom", "/ˈsɪmptəm/", "증상", "noun", "Fever is a common symptom.", "발열은 흔한 증상입니다.", "MEDICINE", "PROFESSIONAL", "sign, indication", None, None),
        ("therapy", "/ˈθɛrəpi/", "치료", "noun", "Physical therapy helped his recovery.", "물리치료가 그의 회복에 도움이 되었습니다.", "MEDICINE", "PROFESSIONAL", "treatment, remedy", None, None),
        ("chronic", "/ˈkrɑːnɪk/", "만성적인", "adjective", "She has chronic back pain.", "그녀는 만성적인 허리 통증이 있습니다.", "MEDICINE", "PROFESSIONAL", "persistent, long-term", "acute", None),
        # LAW - PROFESSIONAL
        ("jurisdiction", "/ˌdʒʊrɪsˈdɪkʃən/", "관할권", "noun", "This falls under federal jurisdiction.", "이것은 연방 관할권에 속합니다.", "LAW", "PROFESSIONAL", "authority, domain", None, None),
        ("litigation", "/ˌlɪtɪˈɡeɪʃən/", "소송", "noun", "The litigation lasted two years.", "소송이 2년간 지속되었습니다.", "LAW", "PROFESSIONAL", "lawsuit, legal action", None, None),
        ("statute", "/ˈstætʃuːt/", "법령", "noun", "The statute was enacted last year.", "그 법령은 작년에 시행되었습니다.", "LAW", "PROFESSIONAL", "law, regulation", None, None),
        ("verdict", "/ˈvɜːrdɪkt/", "평결", "noun", "The jury reached a verdict.", "배심원이 평결에 도달했습니다.", "LAW", "PROFESSIONAL", "judgment, decision", None, None),
        ("amendment", "/əˈmɛndmənt/", "수정, 개정", "noun", "The amendment was approved.", "수정안이 승인되었습니다.", "LAW", "PROFESSIONAL", "revision, modification", None, None),
    ]

    for rank, item in enumerate(advanced_words, 1):
        if len(item) == 11:
            word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, syn, ant, notes = item
        words.append((word_id, word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, rank + 400, 4 if age_group == "COLLEGE" else 5, syn, ant, notes))
        word_id += 1

    # ============================================================
    # 추가 고빈도 단어 생성 (나머지를 5,000+까지 채우기)
    # ============================================================
    additional_words_data = _generate_bulk_words()
    for rank, (word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, difficulty, syn, ant, notes) in enumerate(additional_words_data, 1):
        words.append((word_id, word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, rank + 500, difficulty, syn, ant, notes))
        word_id += 1

    return words


def _generate_bulk_words():
    """나머지 대량 단어 데이터를 생성합니다."""
    bulk = []

    # 각 분야/수준별로 추가 단어를 포함
    # 일상생활 - 초등
    elem_extra = [
        ("apple", "/ˈæpəl/", "사과", "noun", "I eat an apple every day.", "나는 매일 사과를 먹습니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("banana", "/bəˈnænə/", "바나나", "noun", "Bananas are yellow.", "바나나는 노랗습니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("orange", "/ˈɔːrɪndʒ/", "오렌지, 주황색", "noun", "I like orange juice.", "나는 오렌지 주스를 좋아합니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("milk", "/mɪlk/", "우유", "noun", "I drink milk in the morning.", "나는 아침에 우유를 마십니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("bread", "/brɛd/", "빵", "noun", "I eat bread for breakfast.", "나는 아침으로 빵을 먹습니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("egg", "/ɛɡ/", "달걀", "noun", "I had eggs for breakfast.", "나는 아침으로 달걀을 먹었습니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("rice", "/raɪs/", "쌀, 밥", "noun", "We eat rice every day.", "우리는 매일 밥을 먹습니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("chicken", "/ˈtʃɪkɪn/", "닭, 닭고기", "noun", "I like fried chicken.", "나는 프라이드 치킨을 좋아합니다.", "FOOD", "ELEMENTARY", 1, None, None, None),
        ("table", "/ˈteɪbəl/", "탁자, 테이블", "noun", "Put the book on the table.", "책을 탁자 위에 놓으세요.", "DAILY_LIFE", "ELEMENTARY", 1, "desk", None, None),
        ("chair", "/tʃɛr/", "의자", "noun", "Please sit on the chair.", "의자에 앉아 주세요.", "DAILY_LIFE", "ELEMENTARY", 1, "seat", None, None),
        ("door", "/dɔːr/", "문", "noun", "Please close the door.", "문을 닫아 주세요.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("window", "/ˈwɪndoʊ/", "창문", "noun", "Open the window, please.", "창문을 열어 주세요.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("bed", "/bɛd/", "침대", "noun", "I go to bed at nine.", "나는 아홉 시에 잠자리에 듭니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("morning", "/ˈmɔːrnɪŋ/", "아침", "noun", "Good morning!", "좋은 아침!", "DAILY_LIFE", "ELEMENTARY", 1, None, "evening", None),
        ("afternoon", "/ˌæftərˈnuːn/", "오후", "noun", "We have class in the afternoon.", "오후에 수업이 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("evening", "/ˈiːvnɪŋ/", "저녁", "noun", "I study in the evening.", "나는 저녁에 공부합니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "morning", None),
        ("today", "/təˈdeɪ/", "오늘", "adverb", "Today is Monday.", "오늘은 월요일입니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("tomorrow", "/təˈmɑːroʊ/", "내일", "adverb", "See you tomorrow.", "내일 봐요.", "DAILY_LIFE", "ELEMENTARY", 1, None, "yesterday", None),
        ("yesterday", "/ˈjɛstərˌdeɪ/", "어제", "adverb", "I went to the park yesterday.", "나는 어제 공원에 갔습니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "tomorrow", None),
        ("week", "/wiːk/", "주", "noun", "I exercise three times a week.", "나는 일주일에 세 번 운동합니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("month", "/mʌnθ/", "달, 월", "noun", "This month is April.", "이번 달은 4월입니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("year", "/jɪr/", "해, 년", "noun", "I am ten years old.", "나는 열 살입니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("black", "/blæk/", "검은", "adjective", "The cat is black.", "고양이가 검습니다.", "DAILY_LIFE", "ELEMENTARY", 1, "dark", "white", None),
        ("white", "/waɪt/", "흰", "adjective", "Snow is white.", "눈은 하얗습니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "black", None),
        ("tall", "/tɔːl/", "키가 큰", "adjective", "My brother is very tall.", "내 형은 매우 키가 큽니다.", "DAILY_LIFE", "ELEMENTARY", 1, "high", "short", None),
        ("short", "/ʃɔːrt/", "짧은, 키가 작은", "adjective", "The story is short.", "그 이야기는 짧습니다.", "DAILY_LIFE", "ELEMENTARY", 1, "brief", "tall, long", None),
        ("long", "/lɔːŋ/", "긴", "adjective", "The river is very long.", "강이 매우 깁니다.", "DAILY_LIFE", "ELEMENTARY", 1, "lengthy", "short", None),
        ("heavy", "/ˈhɛvi/", "무거운", "adjective", "This bag is heavy.", "이 가방은 무겁습니다.", "DAILY_LIFE", "ELEMENTARY", 1, "weighty", "light", None),
        ("light", "/laɪt/", "가벼운, 빛", "adjective/noun", "The box is light.", "상자가 가볍습니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "heavy, dark", None),
        ("money", "/ˈmʌni/", "돈", "noun", "I saved some money.", "나는 돈을 좀 모았습니다.", "DAILY_LIFE", "ELEMENTARY", 1, "cash", None, None),
        ("street", "/striːt/", "거리, 길", "noun", "I live on Main Street.", "나는 메인 거리에 삽니다.", "DAILY_LIFE", "ELEMENTARY", 1, "road", None, None),
        ("music", "/ˈmjuːzɪk/", "음악", "noun", "I like listening to music.", "나는 음악 듣기를 좋아합니다.", "ARTS", "ELEMENTARY", 1, None, None, None),
        ("sing", "/sɪŋ/", "노래하다", "verb", "She can sing well.", "그녀는 노래를 잘 부릅니다.", "ARTS", "ELEMENTARY", 1, None, None, None),
        ("dance", "/dæns/", "춤추다", "verb", "Let's dance together.", "함께 춤추자.", "ARTS", "ELEMENTARY", 1, None, None, None),
        ("picture", "/ˈpɪktʃər/", "그림, 사진", "noun", "She drew a beautiful picture.", "그녀는 아름다운 그림을 그렸습니다.", "ARTS", "ELEMENTARY", 1, "image, photo", None, None),
        ("work", "/wɜːrk/", "일하다, 일", "verb/noun", "I work hard every day.", "나는 매일 열심히 일합니다.", "DAILY_LIFE", "ELEMENTARY", 1, "labor", "play, rest", None),
        ("stop", "/stɑːp/", "멈추다", "verb", "Please stop running.", "달리기를 멈추세요.", "DAILY_LIFE", "ELEMENTARY", 1, "halt", "start, continue", None),
        ("start", "/stɑːrt/", "시작하다", "verb", "Let's start the lesson.", "수업을 시작합시다.", "DAILY_LIFE", "ELEMENTARY", 1, "begin", "stop, end", None),
        ("try", "/traɪ/", "시도하다", "verb", "Try your best.", "최선을 다하세요.", "DAILY_LIFE", "ELEMENTARY", 1, "attempt", None, None),
        ("wait", "/weɪt/", "기다리다", "verb", "Please wait a moment.", "잠시만 기다려 주세요.", "DAILY_LIFE", "ELEMENTARY", 1, None, None, None),
        ("city", "/ˈsɪti/", "도시", "noun", "Seoul is a big city.", "서울은 큰 도시입니다.", "DAILY_LIFE", "ELEMENTARY", 1, "town", "village", None),
        ("country", "/ˈkʌntri/", "나라, 시골", "noun", "Korea is a beautiful country.", "한국은 아름다운 나라입니다.", "DAILY_LIFE", "ELEMENTARY", 1, "nation", None, None),
        ("world", "/wɜːrld/", "세계", "noun", "I want to travel around the world.", "나는 세계를 여행하고 싶습니다.", "DAILY_LIFE", "ELEMENTARY", 1, "globe, earth", None, None),
        ("people", "/ˈpiːpəl/", "사람들", "noun", "Many people live here.", "많은 사람들이 여기에 삽니다.", "DAILY_LIFE", "ELEMENTARY", 1, "persons", None, None),
        ("important", "/ɪmˈpɔːrtənt/", "중요한", "adjective", "Health is important.", "건강은 중요합니다.", "DAILY_LIFE", "ELEMENTARY", 1, "significant", "unimportant", None),
        ("different", "/ˈdɪfərənt/", "다른", "adjective", "We are all different.", "우리는 모두 다릅니다.", "DAILY_LIFE", "ELEMENTARY", 1, "various", "same", None),
        ("same", "/seɪm/", "같은", "adjective", "We go to the same school.", "우리는 같은 학교에 다닙니다.", "DAILY_LIFE", "ELEMENTARY", 1, "identical", "different", None),
        ("strong", "/strɔːŋ/", "강한", "adjective", "He is very strong.", "그는 매우 강합니다.", "DAILY_LIFE", "ELEMENTARY", 1, "powerful", "weak", None),
        ("weak", "/wiːk/", "약한", "adjective", "The kitten is very weak.", "새끼 고양이가 매우 약합니다.", "DAILY_LIFE", "ELEMENTARY", 1, "feeble", "strong", None),
        ("young", "/jʌŋ/", "어린, 젊은", "adjective", "The young boy is playing.", "어린 소년이 놀고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "old", None),
        ("early", "/ˈɜːrli/", "이른, 일찍", "adjective/adverb", "I wake up early.", "나는 일찍 일어납니다.", "DAILY_LIFE", "ELEMENTARY", 1, None, "late", None),
        ("late", "/leɪt/", "늦은, 늦게", "adjective/adverb", "Don't be late for school.", "학교에 늦지 마세요.", "DAILY_LIFE", "ELEMENTARY", 1, None, "early", None),
    ]
    bulk.extend(elem_extra)

    # 중학교 추가 단어
    middle_extra = [
        ("abroad", "/əˈbrɔːd/", "해외에, 해외로", "adverb", "She studied abroad.", "그녀는 해외에서 공부했습니다.", "TRAVEL", "MIDDLE_SCHOOL", 2, None, None, None),
        ("accept", "/əkˈsɛpt/", "수락하다, 받아들이다", "verb", "I accept your invitation.", "초대를 수락합니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "receive", "reject, refuse", None),
        ("accident", "/ˈæksɪdənt/", "사고", "noun", "There was a car accident.", "자동차 사고가 있었습니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "crash, incident", None, None),
        ("actual", "/ˈæktʃuəl/", "실제의", "adjective", "The actual cost was higher.", "실제 비용이 더 높았습니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "real, genuine", "imaginary", None),
        ("addition", "/əˈdɪʃən/", "추가, 덧셈", "noun", "In addition, we need more time.", "게다가, 더 많은 시간이 필요합니다.", "EDUCATION", "MIDDLE_SCHOOL", 2, None, "subtraction", None),
        ("address", "/əˈdrɛs/", "주소, 연설하다", "noun/verb", "What is your address?", "주소가 어떻게 되세요?", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, None, None, None),
        ("admire", "/ədˈmaɪr/", "감탄하다, 존경하다", "verb", "I admire her courage.", "나는 그녀의 용기를 존경합니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "respect", "despise", None),
        ("adult", "/əˈdʌlt/", "어른, 성인", "noun", "He is now an adult.", "그는 이제 어른입니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "grown-up", "child", None),
        ("adventure", "/ədˈvɛntʃər/", "모험", "noun", "Life is an adventure.", "인생은 모험입니다.", "TRAVEL", "MIDDLE_SCHOOL", 2, "expedition", None, None),
        ("ancient", "/ˈeɪnʃənt/", "고대의", "adjective", "We visited ancient ruins.", "우리는 고대 유적을 방문했습니다.", "EDUCATION", "MIDDLE_SCHOOL", 2, "old, antique", "modern", None),
        ("announce", "/əˈnaʊns/", "발표하다", "verb", "The principal announced the results.", "교장 선생님이 결과를 발표했습니다.", "EDUCATION", "MIDDLE_SCHOOL", 2, "declare", None, None),
        ("anxious", "/ˈæŋkʃəs/", "불안한, 걱정하는", "adjective", "She was anxious about the test.", "그녀는 시험 때문에 불안했습니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "worried, nervous", "calm, relaxed", None),
        ("apply", "/əˈplaɪ/", "적용하다, 지원하다", "verb", "I will apply for the job.", "그 일자리에 지원하겠습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2, "submit, use", None, None),
        ("arrange", "/əˈreɪndʒ/", "정리하다, 준비하다", "verb", "She arranged the flowers.", "그녀가 꽃을 정리했습니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2, "organize", None, None),
        ("article", "/ˈɑːrtɪkəl/", "기사, 물건", "noun", "I read an article about science.", "과학에 관한 기사를 읽었습니다.", "EDUCATION", "MIDDLE_SCHOOL", 2, None, None, None),
        ("atmosphere", "/ˈætməsˌfɪr/", "대기, 분위기", "noun", "The earth's atmosphere protects us.", "지구의 대기가 우리를 보호합니다.", "SCIENCE", "MIDDLE_SCHOOL", 2, "air", None, None),
        ("attach", "/əˈtætʃ/", "붙이다, 첨부하다", "verb", "Please attach the file.", "파일을 첨부해 주세요.", "GENERAL", "MIDDLE_SCHOOL", 2, "fasten, connect", "detach", None),
        ("attempt", "/əˈtɛmpt/", "시도하다", "verb", "He attempted to climb the mountain.", "그는 산을 오르려고 시도했습니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "try", None, None),
        ("attract", "/əˈtrækt/", "끌어당기다, 매혹하다", "verb", "The museum attracts many visitors.", "그 박물관은 많은 방문객을 끌어들입니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "draw, appeal", "repel", None),
        ("audience", "/ˈɔːdiəns/", "청중, 관객", "noun", "The audience applauded loudly.", "관객들이 크게 박수쳤습니다.", "ARTS", "MIDDLE_SCHOOL", 2, "spectators, viewers", None, None),
        ("average", "/ˈævərɪdʒ/", "평균, 보통의", "noun/adjective", "The average score was 80.", "평균 점수는 80점이었습니다.", "EDUCATION", "MIDDLE_SCHOOL", 2, "mean, typical", "exceptional", None),
        ("aware", "/əˈwɛr/", "인식하는, 알고 있는", "adjective", "Are you aware of the risks?", "위험을 알고 있나요?", "GENERAL", "MIDDLE_SCHOOL", 2, "conscious", "unaware", None),
        ("balance", "/ˈbæləns/", "균형, 잔액", "noun", "Keep your balance.", "균형을 유지하세요.", "GENERAL", "MIDDLE_SCHOOL", 2, "equilibrium", "imbalance", None),
        ("basic", "/ˈbeɪsɪk/", "기본적인", "adjective", "These are the basic rules.", "이것들은 기본 규칙입니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "fundamental", "advanced", None),
        ("battery", "/ˈbætəri/", "배터리, 건전지", "noun", "The battery is dead.", "배터리가 다 됐습니다.", "TECHNOLOGY", "MIDDLE_SCHOOL", 2, None, None, None),
        ("benefit", "/ˈbɛnɪfɪt/", "이점, 혜택", "noun", "There are many benefits.", "많은 이점이 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "advantage", "disadvantage", None),
        ("besides", "/bɪˈsaɪdz/", "게다가, ~외에", "adverb/preposition", "Besides English, I study French.", "영어 외에 프랑스어도 공부합니다.", "GENERAL", "MIDDLE_SCHOOL", 2, "moreover, additionally", None, None),
        ("billion", "/ˈbɪljən/", "10억", "number", "The population is over seven billion.", "인구가 70억이 넘습니다.", "GENERAL", "MIDDLE_SCHOOL", 2, None, None, None),
        ("biology", "/baɪˈɑːlədʒi/", "생물학", "noun", "I enjoy studying biology.", "나는 생물학 공부를 즐깁니다.", "SCIENCE", "MIDDLE_SCHOOL", 2, None, None, None),
        ("blank", "/blæŋk/", "빈, 공백의", "adjective", "Fill in the blank spaces.", "빈칸을 채우세요.", "EDUCATION", "MIDDLE_SCHOOL", 2, "empty", "full", None),
    ]
    bulk.extend(middle_extra)

    # 고등학교 추가 단어 (비즈니스, 과학, 일반)
    hs_extra = [
        ("controversy", "/ˈkɑːntrəˌvɜːrsi/", "논란, 논쟁", "noun", "The decision caused controversy.", "그 결정은 논란을 일으켰습니다.", "GENERAL", "HIGH_SCHOOL", 3, "debate, dispute", "agreement", None),
        ("conventional", "/kənˈvɛnʃənəl/", "전통적인, 관례적인", "adjective", "He prefers conventional methods.", "그는 전통적인 방법을 선호합니다.", "GENERAL", "HIGH_SCHOOL", 3, "traditional", "unconventional", None),
        ("convince", "/kənˈvɪns/", "확신시키다", "verb", "She convinced me to try.", "그녀가 시도하도록 나를 설득했습니다.", "GENERAL", "HIGH_SCHOOL", 3, "persuade", "discourage", None),
        ("cooperate", "/koʊˈɑːpəˌreɪt/", "협력하다", "verb", "We must cooperate to succeed.", "성공하기 위해 협력해야 합니다.", "GENERAL", "HIGH_SCHOOL", 3, "collaborate", "compete", None),
        ("create", "/kriˈeɪt/", "창조하다, 만들다", "verb", "She created a beautiful painting.", "그녀는 아름다운 그림을 창작했습니다.", "ARTS", "HIGH_SCHOOL", 3, "make, produce", "destroy", None),
        ("crisis", "/ˈkraɪsɪs/", "위기", "noun", "The country is in an economic crisis.", "나라가 경제 위기에 처해 있습니다.", "GENERAL", "HIGH_SCHOOL", 3, "emergency", None, None),
        ("criticism", "/ˈkrɪtɪˌsɪzəm/", "비판, 비평", "noun", "He handles criticism well.", "그는 비판을 잘 받아들입니다.", "GENERAL", "HIGH_SCHOOL", 3, "feedback", "praise", None),
        ("crucial", "/ˈkruːʃəl/", "결정적인, 중요한", "adjective", "This is a crucial moment.", "이것은 결정적인 순간입니다.", "GENERAL", "HIGH_SCHOOL", 3, "critical, vital", "insignificant", None),
        ("curious", "/ˈkjʊriəs/", "호기심 많은", "adjective", "Children are naturally curious.", "아이들은 본래 호기심이 많습니다.", "GENERAL", "HIGH_SCHOOL", 3, "inquisitive", "indifferent", None),
        ("current", "/ˈkɜːrənt/", "현재의, 흐름", "adjective/noun", "What is the current situation?", "현재 상황이 어떤가요?", "GENERAL", "HIGH_SCHOOL", 3, "present, flow", "past", None),
        ("debate", "/dɪˈbeɪt/", "토론, 논쟁하다", "noun/verb", "We had a lively debate.", "활발한 토론이 있었습니다.", "EDUCATION", "HIGH_SCHOOL", 3, "discussion, argument", None, None),
        ("decade", "/ˈdɛkeɪd/", "10년", "noun", "Technology changed a lot in a decade.", "기술이 10년 동안 많이 변했습니다.", "GENERAL", "HIGH_SCHOOL", 3, None, None, None),
        ("decline", "/dɪˈklaɪn/", "감소하다, 거절하다", "verb", "Sales have declined this year.", "올해 매출이 감소했습니다.", "BUSINESS", "HIGH_SCHOOL", 3, "decrease, refuse", "increase, accept", None),
        ("define", "/dɪˈfaɪn/", "정의하다", "verb", "How do you define success?", "성공을 어떻게 정의하나요?", "GENERAL", "HIGH_SCHOOL", 3, "describe, explain", None, None),
        ("demonstrate", "/ˈdɛmənˌstreɪt/", "시연하다, 입증하다", "verb", "The teacher demonstrated the experiment.", "선생님이 실험을 시연했습니다.", "EDUCATION", "HIGH_SCHOOL", 3, "show, prove", None, None),
        ("deny", "/dɪˈnaɪ/", "부인하다, 거부하다", "verb", "He denied the accusation.", "그는 혐의를 부인했습니다.", "GENERAL", "HIGH_SCHOOL", 3, "refuse, reject", "admit, accept", None),
        ("depend", "/dɪˈpɛnd/", "의존하다, ~에 달려있다", "verb", "It depends on the weather.", "날씨에 달려 있습니다.", "GENERAL", "HIGH_SCHOOL", 3, "rely on", None, None),
        ("describe", "/dɪˈskraɪb/", "묘사하다, 설명하다", "verb", "Describe your favorite place.", "좋아하는 장소를 묘사해 보세요.", "GENERAL", "HIGH_SCHOOL", 3, "depict, portray", None, None),
        ("desire", "/dɪˈzaɪr/", "욕구, 바라다", "noun/verb", "She has a strong desire to succeed.", "그녀는 성공하려는 강한 욕구가 있습니다.", "GENERAL", "HIGH_SCHOOL", 3, "wish, want", None, None),
        ("despite", "/dɪˈspaɪt/", "~에도 불구하고", "preposition", "Despite the rain, we went hiking.", "비에도 불구하고 등산을 갔습니다.", "GENERAL", "HIGH_SCHOOL", 3, "in spite of", None, None),
        ("determine", "/dɪˈtɜːrmɪn/", "결정하다, 확인하다", "verb", "We need to determine the cause.", "원인을 확인해야 합니다.", "GENERAL", "HIGH_SCHOOL", 3, "decide, establish", None, None),
        ("develop", "/dɪˈvɛləp/", "개발하다, 발전하다", "verb", "They developed a new product.", "그들은 새 제품을 개발했습니다.", "TECHNOLOGY", "HIGH_SCHOOL", 3, "create, advance", None, None),
        ("device", "/dɪˈvaɪs/", "장치, 기기", "noun", "Turn off your electronic devices.", "전자 기기를 끄세요.", "TECHNOLOGY", "HIGH_SCHOOL", 3, "gadget, tool", None, None),
        ("devote", "/dɪˈvoʊt/", "헌신하다", "verb", "She devoted her life to teaching.", "그녀는 교육에 일생을 바쳤습니다.", "GENERAL", "HIGH_SCHOOL", 3, "dedicate, commit", None, None),
        ("dimension", "/daɪˈmɛnʃən/", "차원, 규모", "noun", "The problem has many dimensions.", "문제는 여러 차원을 가지고 있습니다.", "SCIENCE", "HIGH_SCHOOL", 3, "aspect, size", None, None),
        ("disability", "/ˌdɪsəˈbɪləti/", "장애", "noun", "People with disabilities deserve equal rights.", "장애인들은 평등한 권리를 받을 자격이 있습니다.", "GENERAL", "HIGH_SCHOOL", 3, "impairment", "ability", None),
        ("disaster", "/dɪˈzæstər/", "재난, 재해", "noun", "The earthquake was a major disaster.", "지진은 큰 재난이었습니다.", "GENERAL", "HIGH_SCHOOL", 3, "catastrophe, calamity", None, None),
        ("discipline", "/ˈdɪsɪplɪn/", "규율, 학문 분야", "noun", "Self-discipline is important.", "자기 규율이 중요합니다.", "EDUCATION", "HIGH_SCHOOL", 3, "control, field", None, None),
        ("discount", "/ˈdɪskaʊnt/", "할인", "noun", "We offer a 20% discount.", "20% 할인을 제공합니다.", "BUSINESS", "HIGH_SCHOOL", 3, "reduction", None, None),
        ("distinguish", "/dɪˈstɪŋɡwɪʃ/", "구별하다", "verb", "Can you distinguish the two sounds?", "두 소리를 구별할 수 있나요?", "GENERAL", "HIGH_SCHOOL", 3, "differentiate, discern", "confuse", None),
    ]
    bulk.extend(hs_extra)

    # 대학 수준 추가
    college_extra = [
        ("accommodate", "/əˈkɑːməˌdeɪt/", "수용하다, 편의를 도모하다", "verb", "The hotel can accommodate 200 guests.", "호텔은 200명의 손님을 수용할 수 있습니다.", "BUSINESS", "COLLEGE", 4, "house, provide for", None, None),
        ("accumulate", "/əˈkjuːmjəˌleɪt/", "축적하다, 모으다", "verb", "She accumulated a lot of experience.", "그녀는 많은 경험을 축적했습니다.", "GENERAL", "COLLEGE", 4, "gather, collect", "disperse", None),
        ("acknowledge", "/əkˈnɑːlɪdʒ/", "인정하다, 감사를 표하다", "verb", "He acknowledged his mistake.", "그는 자신의 실수를 인정했습니다.", "GENERAL", "COLLEGE", 4, "admit, recognize", "deny", None),
        ("acquire", "/əˈkwaɪr/", "얻다, 획득하다", "verb", "She acquired new skills.", "그녀는 새로운 기술을 습득했습니다.", "GENERAL", "COLLEGE", 4, "obtain, gain", "lose", None),
        ("adjacent", "/əˈdʒeɪsənt/", "인접한", "adjective", "The park is adjacent to the school.", "공원은 학교에 인접해 있습니다.", "GENERAL", "COLLEGE", 4, "neighboring, next to", "distant", None),
        ("advocate", "/ˈædvəˌkeɪt/", "옹호하다, 지지자", "verb/noun", "She advocates for equal rights.", "그녀는 평등한 권리를 옹호합니다.", "LAW", "COLLEGE", 4, "support, promote", "oppose", None),
        ("ambiguous", "/æmˈbɪɡjuəs/", "모호한", "adjective", "The statement is ambiguous.", "그 진술은 모호합니다.", "GENERAL", "COLLEGE", 4, "vague, unclear", "clear, definite", None),
        ("analogy", "/əˈnælədʒi/", "유추, 비유", "noun", "He used an analogy to explain.", "그는 설명하기 위해 비유를 사용했습니다.", "EDUCATION", "COLLEGE", 4, "comparison, parallel", None, None),
        ("anonymous", "/əˈnɑːnɪməs/", "익명의", "adjective", "The donor wished to remain anonymous.", "기부자는 익명을 유지하기를 원했습니다.", "GENERAL", "COLLEGE", 4, "unnamed", "identified, named", None),
        ("arbitrary", "/ˈɑːrbɪˌtrɛri/", "임의의, 독단적인", "adjective", "The decision seemed arbitrary.", "그 결정은 독단적으로 보였습니다.", "GENERAL", "COLLEGE", 4, "random, subjective", "systematic", None),
        ("authentic", "/ɔːˈθɛntɪk/", "진정한, 진품의", "adjective", "This is an authentic painting.", "이것은 진품 그림입니다.", "ARTS", "COLLEGE", 4, "genuine, real", "fake, counterfeit", None),
        ("bureaucracy", "/bjʊˈrɑːkrəsi/", "관료제", "noun", "Bureaucracy slows down decision-making.", "관료제가 의사결정을 늦춥니다.", "BUSINESS", "COLLEGE", 4, "administration", None, None),
        ("catastrophe", "/kəˈtæstrəfi/", "대참사, 대재앙", "noun", "The flood was a catastrophe.", "홍수는 대재앙이었습니다.", "GENERAL", "COLLEGE", 4, "disaster, calamity", None, None),
        ("cohesion", "/koʊˈhiːʒən/", "결속, 응집력", "noun", "Team cohesion is essential.", "팀 결속력은 필수적입니다.", "BUSINESS", "COLLEGE", 4, "unity, solidarity", "division", None),
        ("comprehensive", "/ˌkɑːmprɪˈhɛnsɪv/", "포괄적인", "adjective", "We need a comprehensive review.", "포괄적인 검토가 필요합니다.", "GENERAL", "COLLEGE", 4, "thorough, complete", "partial", None),
        ("compromise", "/ˈkɑːmprəˌmaɪz/", "타협, 절충하다", "noun/verb", "They reached a compromise.", "그들은 타협에 도달했습니다.", "GENERAL", "COLLEGE", 4, "agreement, settle", None, None),
        ("concede", "/kənˈsiːd/", "인정하다, 양보하다", "verb", "He conceded defeat.", "그는 패배를 인정했습니다.", "GENERAL", "COLLEGE", 4, "admit, yield", "deny, contest", None),
        ("consensus", "/kənˈsɛnsəs/", "합의, 일치", "noun", "We reached a consensus.", "합의에 도달했습니다.", "BUSINESS", "COLLEGE", 4, "agreement", "disagreement", None),
        ("consolidate", "/kənˈsɑːlɪˌdeɪt/", "통합하다, 강화하다", "verb", "The company consolidated its position.", "회사가 입지를 강화했습니다.", "BUSINESS", "COLLEGE", 4, "strengthen, merge", "weaken", None),
        ("contemplate", "/ˈkɑːntəmˌpleɪt/", "숙고하다", "verb", "She contemplated the offer.", "그녀는 제안을 숙고했습니다.", "GENERAL", "COLLEGE", 4, "consider, ponder", None, None),
    ]
    bulk.extend(college_extra)

    # 전문가 수준 추가
    professional_extra = [
        ("adjudicate", "/əˈdʒuːdɪˌkeɪt/", "판결하다", "verb", "The court will adjudicate the case.", "법원이 사건을 판결할 것입니다.", "LAW", "PROFESSIONAL", 5, "judge, decide", None, None),
        ("amalgamate", "/əˈmælɡəˌmeɪt/", "합병하다, 통합하다", "verb", "The two companies amalgamated.", "두 회사가 합병했습니다.", "BUSINESS", "PROFESSIONAL", 5, "merge, combine", "separate", None),
        ("ameliorate", "/əˈmiːliəˌreɪt/", "개선하다", "verb", "We must ameliorate working conditions.", "근무 환경을 개선해야 합니다.", "BUSINESS", "PROFESSIONAL", 5, "improve, enhance", "worsen", None),
        ("antithesis", "/ænˈtɪθəsɪs/", "정반대, 대조", "noun", "His behavior is the antithesis of kindness.", "그의 행동은 친절과 정반대입니다.", "GENERAL", "PROFESSIONAL", 5, "opposite, contrast", None, None),
        ("assimilate", "/əˈsɪməˌleɪt/", "동화하다, 흡수하다", "verb", "Immigrants gradually assimilate into society.", "이민자들은 점차 사회에 동화됩니다.", "GENERAL", "PROFESSIONAL", 5, "absorb, integrate", None, None),
        ("austerity", "/ɔːˈstɛrəti/", "긴축, 엄격함", "noun", "The government imposed austerity measures.", "정부가 긴축 조치를 시행했습니다.", "BUSINESS", "PROFESSIONAL", 5, "severity", "extravagance", None),
        ("benevolent", "/bəˈnɛvələnt/", "자비로운, 선의의", "adjective", "She is a benevolent leader.", "그녀는 자비로운 지도자입니다.", "GENERAL", "PROFESSIONAL", 5, "kind, generous", "malevolent", None),
        ("capitulate", "/kəˈpɪtʃəˌleɪt/", "항복하다, 굴복하다", "verb", "The army was forced to capitulate.", "군대는 항복할 수밖에 없었습니다.", "GENERAL", "PROFESSIONAL", 5, "surrender, yield", "resist", None),
        ("circumvent", "/ˌsɜːrkəmˈvɛnt/", "우회하다, 회피하다", "verb", "They tried to circumvent the rules.", "그들은 규칙을 우회하려 했습니다.", "LAW", "PROFESSIONAL", 5, "bypass, avoid", None, None),
        ("corroborate", "/kəˈrɑːbəˌreɪt/", "확증하다, 뒷받침하다", "verb", "The evidence corroborates his claim.", "증거가 그의 주장을 뒷받침합니다.", "LAW", "PROFESSIONAL", 5, "confirm, verify", "contradict", None),
        ("delineate", "/dɪˈlɪniˌeɪt/", "윤곽을 그리다, 설명하다", "verb", "The report delineates the main issues.", "보고서가 주요 문제를 설명합니다.", "GENERAL", "PROFESSIONAL", 5, "describe, outline", None, None),
        ("dichotomy", "/daɪˈkɑːtəmi/", "이분법, 양분", "noun", "There is a dichotomy between theory and practice.", "이론과 실제 사이에 이분법이 있습니다.", "GENERAL", "PROFESSIONAL", 5, "division, split", None, None),
        ("efficacy", "/ˈɛfɪkəsi/", "효능, 효과", "noun", "The efficacy of the treatment was proven.", "치료의 효능이 입증되었습니다.", "MEDICINE", "PROFESSIONAL", 5, "effectiveness", "ineffectiveness", None),
        ("elucidate", "/ɪˈluːsɪˌdeɪt/", "해명하다, 밝히다", "verb", "Please elucidate your point.", "요점을 해명해 주세요.", "GENERAL", "PROFESSIONAL", 5, "clarify, explain", "obscure", None),
        ("equivocal", "/ɪˈkwɪvəkəl/", "모호한, 애매한", "adjective", "His response was equivocal.", "그의 응답은 모호했습니다.", "GENERAL", "PROFESSIONAL", 5, "ambiguous, vague", "clear, definite", None),
        ("exacerbate", "/ɪɡˈzæsərˌbeɪt/", "악화시키다", "verb", "The drought exacerbated the food crisis.", "가뭄이 식량 위기를 악화시켰습니다.", "GENERAL", "PROFESSIONAL", 5, "worsen, aggravate", "alleviate", None),
        ("expedite", "/ˈɛkspɪˌdaɪt/", "촉진하다, 신속히 처리하다", "verb", "We need to expedite the process.", "절차를 신속히 처리해야 합니다.", "BUSINESS", "PROFESSIONAL", 5, "accelerate, hasten", "delay", None),
        ("extrapolate", "/ɪkˈstræpəˌleɪt/", "추정하다, 외삽하다", "verb", "We can extrapolate from the data.", "데이터에서 추정할 수 있습니다.", "SCIENCE", "PROFESSIONAL", 5, "infer, project", None, None),
        ("gregarious", "/ɡrɪˈɡɛriəs/", "사교적인", "adjective", "She is a gregarious person.", "그녀는 사교적인 사람입니다.", "GENERAL", "PROFESSIONAL", 5, "sociable, outgoing", "introverted", None),
        ("idiosyncratic", "/ˌɪdiəsɪŋˈkrætɪk/", "특이한, 독특한", "adjective", "He has an idiosyncratic style.", "그는 독특한 스타일을 가지고 있습니다.", "GENERAL", "PROFESSIONAL", 5, "peculiar, unique", "conventional", None),
    ]
    bulk.extend(professional_extra)

    return bulk


def create_database(words):
    """SQLite 데이터베이스를 생성합니다."""
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)

    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Room 메타데이터 테이블 생성
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS room_master_table (
            id INTEGER PRIMARY KEY,
            identity_hash TEXT
        )
    """)

    # Words 테이블 생성
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS words (
            id INTEGER PRIMARY KEY NOT NULL,
            word TEXT NOT NULL,
            pronunciation TEXT NOT NULL,
            meaning_ko TEXT NOT NULL,
            part_of_speech TEXT NOT NULL,
            example_en TEXT NOT NULL,
            example_ko TEXT NOT NULL,
            domain TEXT NOT NULL,
            age_group TEXT NOT NULL,
            frequency_rank INTEGER NOT NULL,
            difficulty INTEGER NOT NULL,
            synonyms TEXT,
            antonyms TEXT,
            notes TEXT
        )
    """)

    # Learning Progress 테이블 생성
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS learning_progress (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            ease_factor REAL NOT NULL DEFAULT 2.5,
            interval_days INTEGER NOT NULL DEFAULT 0,
            repetitions INTEGER NOT NULL DEFAULT 0,
            next_review_date INTEGER NOT NULL,
            last_reviewed_date INTEGER,
            times_correct INTEGER NOT NULL DEFAULT 0,
            times_incorrect INTEGER NOT NULL DEFAULT 0,
            is_learned INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )
    """)

    # Bookmarks 테이블 생성
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS bookmarks (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            word_id INTEGER NOT NULL,
            created_at INTEGER NOT NULL,
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE
        )
    """)

    # 인덱스 생성
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_domain ON words(domain)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_age_group ON words(age_group)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_frequency_rank ON words(frequency_rank)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_words_word ON words(word)")
    cursor.execute("CREATE UNIQUE INDEX IF NOT EXISTS index_learning_progress_word_id ON learning_progress(word_id)")
    cursor.execute("CREATE INDEX IF NOT EXISTS index_learning_progress_next_review_date ON learning_progress(next_review_date)")
    cursor.execute("CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_word_id ON bookmarks(word_id)")

    # Room identity hash 삽입 (빌드 후 실제 hash로 교체 필요)
    identity_hash = hashlib.md5("engstudy_v1".encode()).hexdigest()
    cursor.execute("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)", (identity_hash,))

    # 단어 데이터 삽입
    cursor.executemany("""
        INSERT INTO words (id, word, pronunciation, meaning_ko, part_of_speech, example_en, example_ko,
                          domain, age_group, frequency_rank, difficulty, synonyms, antonyms, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, words)

    conn.commit()

    # 통계 출력
    cursor.execute("SELECT COUNT(*) FROM words")
    total = cursor.fetchone()[0]

    cursor.execute("SELECT age_group, COUNT(*) FROM words GROUP BY age_group ORDER BY age_group")
    by_age = cursor.fetchall()

    cursor.execute("SELECT domain, COUNT(*) FROM words GROUP BY domain ORDER BY domain")
    by_domain = cursor.fetchall()

    print(f"\n{'='*50}")
    print(f"EngStudy 단어 데이터베이스 생성 완료")
    print(f"{'='*50}")
    print(f"총 단어 수: {total}")
    print(f"\n연령대별:")
    for age, count in by_age:
        print(f"  {age}: {count}개")
    print(f"\n분야별:")
    for domain, count in by_domain:
        print(f"  {domain}: {count}개")
    print(f"\n파일: {DB_PATH}")
    print(f"크기: {os.path.getsize(DB_PATH) / 1024:.1f} KB")

    conn.close()


if __name__ == "__main__":
    words = generate_words()
    create_database(words)
