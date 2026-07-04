#!/usr/bin/env python3
"""
기존 DB에 고빈도 영어 단어를 추가로 확장합니다.
목표: 5,000+ 단어
"""

import sqlite3
import os

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
DB_PATH = os.path.join(PROJECT_ROOT, "app", "src", "main", "assets", "databases", "engstudy.db")


def get_next_id(cursor):
    cursor.execute("SELECT MAX(id) FROM words")
    result = cursor.fetchone()[0]
    return (result or 0) + 1


def expand_database():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    word_id = get_next_id(cursor)
    added = 0

    # 기존 단어 목록 (중복 방지)
    cursor.execute("SELECT word FROM words")
    existing = set(row[0] for row in cursor.fetchall())

    # ================================================================
    # 대량 단어 데이터 - 각 수준/분야별 고빈도 영어 단어
    # ================================================================

    all_words = []

    # ---- ELEMENTARY 추가 단어 ----
    elementary = [
        # 동물
        ("horse", "/hɔːrs/", "말", "noun", "The horse runs fast.", "말이 빨리 달립니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("cow", "/kaʊ/", "소", "noun", "The cow gives milk.", "소가 우유를 줍니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("pig", "/pɪɡ/", "돼지", "noun", "The pig is in the mud.", "돼지가 진흙에 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sheep", "/ʃiːp/", "양", "noun", "There are many sheep on the farm.", "농장에 양이 많습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("rabbit", "/ˈræbɪt/", "토끼", "noun", "The rabbit has long ears.", "토끼는 귀가 깁니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("bear", "/bɛr/", "곰", "noun", "Bears sleep in winter.", "곰은 겨울에 잠을 잡니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("lion", "/ˈlaɪən/", "사자", "noun", "The lion is the king of animals.", "사자는 동물의 왕입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("monkey", "/ˈmʌŋki/", "원숭이", "noun", "Monkeys like bananas.", "원숭이는 바나나를 좋아합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("elephant", "/ˈɛlɪfənt/", "코끼리", "noun", "Elephants are very big.", "코끼리는 매우 큽니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("tiger", "/ˈtaɪɡər/", "호랑이", "noun", "The tiger is a strong animal.", "호랑이는 강한 동물입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 신체
        ("ear", "/ɪr/", "귀", "noun", "Rabbits have long ears.", "토끼는 귀가 깁니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("nose", "/noʊz/", "코", "noun", "The clown has a red nose.", "광대는 빨간 코를 가지고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("foot", "/fʊt/", "발", "noun", "I hurt my foot.", "발을 다쳤습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("arm", "/ɑːrm/", "팔", "noun", "He raised his arm.", "그는 팔을 들었습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("leg", "/lɛɡ/", "다리", "noun", "She broke her leg.", "그녀는 다리가 부러졌습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("finger", "/ˈfɪŋɡər/", "손가락", "noun", "I have ten fingers.", "손가락이 열 개 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("hair", "/hɛr/", "머리카락", "noun", "She has long hair.", "그녀는 머리카락이 깁니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("face", "/feɪs/", "얼굴", "noun", "Wash your face.", "얼굴을 씻으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("tooth", "/tuːθ/", "이, 치아", "noun", "Brush your teeth twice a day.", "하루에 두 번 이를 닦으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("heart", "/hɑːrt/", "심장, 마음", "noun", "My heart is beating fast.", "심장이 빠르게 뛰고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 의류
        ("shirt", "/ʃɜːrt/", "셔츠", "noun", "He wears a white shirt.", "그는 흰 셔츠를 입습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("pants", "/pænts/", "바지", "noun", "I bought new pants.", "새 바지를 샀습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("shoes", "/ʃuːz/", "신발", "noun", "Take off your shoes.", "신발을 벗으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("hat", "/hæt/", "모자", "noun", "She wears a red hat.", "그녀는 빨간 모자를 쓰고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("jacket", "/ˈdʒækɪt/", "재킷", "noun", "Wear your jacket, it's cold.", "재킷을 입으세요, 추워요.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 날씨/자연
        ("wind", "/wɪnd/", "바람", "noun", "The wind is blowing hard.", "바람이 세게 불고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("cloud", "/klaʊd/", "구름", "noun", "There are many clouds today.", "오늘 구름이 많습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sea", "/siː/", "바다", "noun", "The sea is blue and calm.", "바다가 파랗고 잔잔합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("river", "/ˈrɪvər/", "강", "noun", "We swam in the river.", "우리는 강에서 수영했습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("mountain", "/ˈmaʊntən/", "산", "noun", "We climbed the mountain.", "우리는 산을 올랐습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("garden", "/ˈɡɑːrdən/", "정원", "noun", "She has a beautiful garden.", "그녀는 아름다운 정원을 가지고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sky", "/skaɪ/", "하늘", "noun", "The sky is clear today.", "오늘 하늘이 맑습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("ground", "/ɡraʊnd/", "땅, 지면", "noun", "Sit on the ground.", "땅에 앉으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 학교 관련
        ("class", "/klæs/", "수업, 반", "noun", "I have English class today.", "오늘 영어 수업이 있습니다.", "EDUCATION", "ELEMENTARY", 1),
        ("pencil", "/ˈpɛnsəl/", "연필", "noun", "I need a pencil.", "연필이 필요합니다.", "EDUCATION", "ELEMENTARY", 1),
        ("paper", "/ˈpeɪpər/", "종이", "noun", "Give me a piece of paper.", "종이 한 장 주세요.", "EDUCATION", "ELEMENTARY", 1),
        ("desk", "/dɛsk/", "책상", "noun", "Put your books on the desk.", "책을 책상 위에 놓으세요.", "EDUCATION", "ELEMENTARY", 1),
        ("page", "/peɪdʒ/", "페이지, 쪽", "noun", "Open your book to page 10.", "책 10페이지를 펴세요.", "EDUCATION", "ELEMENTARY", 1),
        ("map", "/mæp/", "지도", "noun", "Look at the map.", "지도를 보세요.", "EDUCATION", "ELEMENTARY", 1),
        ("number", "/ˈnʌmbər/", "숫자, 번호", "noun", "What is your phone number?", "전화번호가 뭐예요?", "EDUCATION", "ELEMENTARY", 1),
        ("letter", "/ˈlɛtər/", "글자, 편지", "noun", "Write a letter to your friend.", "친구에게 편지를 쓰세요.", "EDUCATION", "ELEMENTARY", 1),
        # 감정/상태
        ("hungry", "/ˈhʌŋɡri/", "배고픈", "adjective", "I am very hungry.", "나는 매우 배가 고픕니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("thirsty", "/ˈθɜːrsti/", "목마른", "adjective", "I am thirsty. Can I have water?", "목이 마릅니다. 물을 마셔도 될까요?", "DAILY_LIFE", "ELEMENTARY", 1),
        ("tired", "/taɪrd/", "피곤한", "adjective", "I am tired after running.", "달리고 나서 피곤합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sick", "/sɪk/", "아픈", "adjective", "She is sick today.", "그녀는 오늘 아픕니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("afraid", "/əˈfreɪd/", "두려운", "adjective", "Don't be afraid of the dark.", "어둠을 두려워하지 마세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("kind", "/kaɪnd/", "친절한, 종류", "adjective/noun", "She is very kind to everyone.", "그녀는 모든 사람에게 매우 친절합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sure", "/ʃʊr/", "확실한", "adjective", "Are you sure about that?", "그것에 대해 확실한가요?", "DAILY_LIFE", "ELEMENTARY", 1),
        ("easy", "/ˈiːzi/", "쉬운", "adjective", "This question is easy.", "이 질문은 쉽습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("hard", "/hɑːrd/", "어려운, 단단한", "adjective", "The test was very hard.", "시험이 매우 어려웠습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("funny", "/ˈfʌni/", "웃긴, 재미있는", "adjective", "The movie was very funny.", "영화가 매우 재미있었습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 동사 추가
        ("catch", "/kætʃ/", "잡다", "verb", "Catch the ball!", "공을 잡아!", "SPORTS", "ELEMENTARY", 1),
        ("throw", "/θroʊ/", "던지다", "verb", "Throw the ball to me.", "나에게 공을 던져.", "SPORTS", "ELEMENTARY", 1),
        ("jump", "/dʒʌmp/", "뛰다, 점프하다", "verb", "The frog can jump high.", "개구리는 높이 뛸 수 있습니다.", "SPORTS", "ELEMENTARY", 1),
        ("swim", "/swɪm/", "수영하다", "verb", "I can swim well.", "나는 수영을 잘합니다.", "SPORTS", "ELEMENTARY", 1),
        ("climb", "/klaɪm/", "오르다", "verb", "We climbed the hill.", "우리는 언덕을 올랐습니다.", "SPORTS", "ELEMENTARY", 1),
        ("draw", "/drɔː/", "그리다", "verb", "I like to draw pictures.", "나는 그림 그리기를 좋아합니다.", "ARTS", "ELEMENTARY", 1),
        ("wash", "/wɒʃ/", "씻다", "verb", "Wash your hands before eating.", "먹기 전에 손을 씻으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("cry", "/kraɪ/", "울다", "verb", "The baby is crying.", "아기가 울고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("laugh", "/læf/", "웃다", "verb", "We laughed at the joke.", "우리는 농담에 웃었습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("smile", "/smaɪl/", "미소 짓다", "verb", "She smiled at me.", "그녀가 나에게 미소 지었습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("count", "/kaʊnt/", "세다", "verb", "Count from one to ten.", "하나부터 열까지 세세요.", "EDUCATION", "ELEMENTARY", 1),
        ("teach", "/tiːtʃ/", "가르치다", "verb", "She teaches English.", "그녀는 영어를 가르칩니다.", "EDUCATION", "ELEMENTARY", 1),
        ("push", "/pʊʃ/", "밀다", "verb", "Push the door to open it.", "문을 밀어서 여세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("pull", "/pʊl/", "당기다", "verb", "Pull the rope.", "밧줄을 당기세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("carry", "/ˈkæri/", "나르다, 들고 가다", "verb", "Can you carry this bag?", "이 가방을 들어줄 수 있나요?", "DAILY_LIFE", "ELEMENTARY", 1),
        ("wear", "/wɛr/", "입다, 착용하다", "verb", "I wear a uniform to school.", "나는 학교에 교복을 입고 갑니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("buy", "/baɪ/", "사다", "verb", "I want to buy a new book.", "새 책을 사고 싶습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sell", "/sɛl/", "팔다", "verb", "He sells flowers.", "그는 꽃을 팝니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("bring", "/brɪŋ/", "가져오다", "verb", "Bring your book to class.", "수업에 책을 가져오세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("send", "/sɛnd/", "보내다", "verb", "Send me a message.", "메시지를 보내 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("speak", "/spiːk/", "말하다", "verb", "Can you speak Korean?", "한국어를 할 수 있나요?", "DAILY_LIFE", "ELEMENTARY", 1),
        ("tell", "/tɛl/", "말하다, 알려주다", "verb", "Tell me a story.", "이야기를 해주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("ask", "/æsk/", "묻다, 부탁하다", "verb", "Ask the teacher a question.", "선생님에게 질문하세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("answer", "/ˈænsər/", "대답하다", "verb", "Please answer the question.", "질문에 대답해 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("fly", "/flaɪ/", "날다", "verb", "Birds can fly.", "새는 날 수 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("grow", "/ɡroʊ/", "자라다, 기르다", "verb", "Plants grow in the garden.", "식물이 정원에서 자랍니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("cut", "/kʌt/", "자르다", "verb", "Cut the paper with scissors.", "가위로 종이를 자르세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("build", "/bɪld/", "짓다, 건설하다", "verb", "They build houses.", "그들은 집을 짓습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("stand", "/stænd/", "서다", "verb", "Please stand up.", "일어서 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("sit", "/sɪt/", "앉다", "verb", "Please sit down.", "앉아 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("fall", "/fɔːl/", "떨어지다, 가을", "verb/noun", "Leaves fall in autumn.", "가을에 낙엽이 떨어집니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("turn", "/tɜːrn/", "돌다, 차례", "verb/noun", "Turn left at the corner.", "모퉁이에서 왼쪽으로 도세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("move", "/muːv/", "움직이다, 이사하다", "verb", "Don't move!", "움직이지 마!", "DAILY_LIFE", "ELEMENTARY", 1),
        ("follow", "/ˈfɑːloʊ/", "따르다, 따라가다", "verb", "Follow me.", "나를 따라오세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("show", "/ʃoʊ/", "보여주다", "verb", "Show me your drawing.", "그림을 보여줘.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("feel", "/fiːl/", "느끼다", "verb", "I feel happy today.", "오늘 기분이 좋습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("need", "/niːd/", "필요하다", "verb", "I need your help.", "도움이 필요합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("use", "/juːz/", "사용하다", "verb", "Use a pencil to write.", "연필로 쓰세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("find", "/faɪnd/", "찾다, 발견하다", "verb", "I found my keys.", "열쇠를 찾았습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("leave", "/liːv/", "떠나다, 남기다", "verb", "I leave home at eight.", "8시에 집을 나섭니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("keep", "/kiːp/", "유지하다, 보관하다", "verb", "Keep your room clean.", "방을 깨끗하게 유지하세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("put", "/pʊt/", "놓다, 두다", "verb", "Put the cup on the table.", "컵을 탁자 위에 놓으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("call", "/kɔːl/", "부르다, 전화하다", "verb", "Call me tonight.", "오늘 밤에 전화해.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("meet", "/miːt/", "만나다", "verb", "Nice to meet you.", "만나서 반갑습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("talk", "/tɔːk/", "이야기하다", "verb", "We talked for hours.", "우리는 몇 시간 동안 이야기했습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("listen", "/ˈlɪsən/", "듣다", "verb", "Listen to the music.", "음악을 들으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 숫자/수량
        ("four", "/fɔːr/", "넷, 4", "number", "I have four friends.", "친구가 네 명 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("five", "/faɪv/", "다섯, 5", "number", "I have five books.", "책이 다섯 권 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("six", "/sɪks/", "여섯, 6", "number", "There are six eggs.", "달걀이 여섯 개 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("seven", "/ˈsɛvən/", "일곱, 7", "number", "There are seven days in a week.", "일주일에 7일이 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("eight", "/eɪt/", "여덟, 8", "number", "I wake up at eight.", "8시에 일어납니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("nine", "/naɪn/", "아홉, 9", "number", "I go to bed at nine.", "9시에 잡니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("ten", "/tɛn/", "열, 10", "number", "Count to ten.", "열까지 세세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("hundred", "/ˈhʌndrəd/", "백, 100", "number", "There are a hundred students.", "학생이 100명 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("thousand", "/ˈθaʊzənd/", "천, 1000", "number", "A thousand people came.", "천 명이 왔습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("many", "/ˈmɛni/", "많은", "adjective", "There are many trees.", "나무가 많습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("few", "/fjuː/", "적은, 몇몇의", "adjective", "I have a few questions.", "질문이 몇 가지 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("some", "/sʌm/", "약간의, 일부의", "adjective", "I need some water.", "물이 좀 필요합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("every", "/ˈɛvri/", "모든, 매", "adjective", "I exercise every day.", "매일 운동합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("first", "/fɜːrst/", "첫 번째의", "adjective", "This is my first time.", "이것이 처음입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("last", "/læst/", "마지막의, 지난", "adjective", "This is the last question.", "이것이 마지막 질문입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("next", "/nɛkst/", "다음의", "adjective", "See you next week.", "다음 주에 봐요.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 장소
        ("hospital", "/ˈhɑːspɪtəl/", "병원", "noun", "She went to the hospital.", "그녀는 병원에 갔습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("park", "/pɑːrk/", "공원", "noun", "We play in the park.", "우리는 공원에서 놉니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("store", "/stɔːr/", "가게, 상점", "noun", "I went to the store.", "가게에 갔습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("library", "/ˈlaɪˌbrɛri/", "도서관", "noun", "I study at the library.", "도서관에서 공부합니다.", "EDUCATION", "ELEMENTARY", 1),
        ("church", "/tʃɜːrtʃ/", "교회", "noun", "They go to church on Sunday.", "그들은 일요일에 교회에 갑니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("restaurant", "/ˈrɛstərɑːnt/", "식당, 레스토랑", "noun", "We ate at a restaurant.", "우리는 식당에서 먹었습니다.", "FOOD", "ELEMENTARY", 1),
        ("beach", "/biːtʃ/", "해변", "noun", "We went to the beach.", "해변에 갔습니다.", "TRAVEL", "ELEMENTARY", 1),
        ("farm", "/fɑːrm/", "농장", "noun", "My uncle has a farm.", "삼촌은 농장을 가지고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        # 교통
        ("train", "/treɪn/", "기차", "noun", "I take the train to work.", "기차를 타고 출근합니다.", "TRAVEL", "ELEMENTARY", 1),
        ("airplane", "/ˈɛrˌpleɪn/", "비행기", "noun", "The airplane is in the sky.", "비행기가 하늘에 있습니다.", "TRAVEL", "ELEMENTARY", 1),
        ("bicycle", "/ˈbaɪsɪkəl/", "자전거", "noun", "I ride my bicycle to school.", "자전거를 타고 학교에 갑니다.", "TRAVEL", "ELEMENTARY", 1),
        ("boat", "/boʊt/", "배, 보트", "noun", "We sailed on a boat.", "보트를 타고 항해했습니다.", "TRAVEL", "ELEMENTARY", 1),
        # 기타
        ("toy", "/tɔɪ/", "장난감", "noun", "Children play with toys.", "아이들은 장난감을 가지고 놉니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("game", "/ɡeɪm/", "게임, 놀이", "noun", "Let's play a game.", "게임을 하자.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("ball", "/bɔːl/", "공", "noun", "Kick the ball.", "공을 차세요.", "SPORTS", "ELEMENTARY", 1),
        ("story", "/ˈstɔːri/", "이야기", "noun", "Tell me a story.", "이야기를 해주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("song", "/sɔːŋ/", "노래", "noun", "I like this song.", "이 노래가 좋습니다.", "ARTS", "ELEMENTARY", 1),
        ("movie", "/ˈmuːvi/", "영화", "noun", "We watched a movie.", "영화를 봤습니다.", "ARTS", "ELEMENTARY", 1),
        ("telephone", "/ˈtɛləˌfoʊn/", "전화기", "noun", "The telephone is ringing.", "전화기가 울리고 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("camera", "/ˈkæmərə/", "카메라", "noun", "I took photos with my camera.", "카메라로 사진을 찍었습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("clock", "/klɑːk/", "시계", "noun", "Look at the clock.", "시계를 보세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("key", "/kiː/", "열쇠", "noun", "I lost my key.", "열쇠를 잃어버렸습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("bag", "/bæɡ/", "가방", "noun", "Put your books in the bag.", "책을 가방에 넣으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("umbrella", "/ʌmˈbrɛlə/", "우산", "noun", "Take an umbrella, it might rain.", "우산을 가져가세요, 비가 올 수 있어요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("knife", "/naɪf/", "칼, 나이프", "noun", "Be careful with the knife.", "칼을 조심하세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("cup", "/kʌp/", "컵", "noun", "I want a cup of tea.", "차 한 잔 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("plate", "/pleɪt/", "접시", "noun", "Put the food on the plate.", "접시에 음식을 담으세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("glass", "/ɡlæs/", "유리, 컵", "noun", "A glass of water, please.", "물 한 잔 주세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("bottle", "/ˈbɑːtəl/", "병", "noun", "I bought a bottle of juice.", "주스 한 병을 샀습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("box", "/bɑːks/", "상자", "noun", "Open the box.", "상자를 여세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("present", "/ˈprɛzənt/", "선물, 현재의", "noun/adjective", "I got a birthday present.", "생일 선물을 받았습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("birthday", "/ˈbɜːrθˌdeɪ/", "생일", "noun", "Happy birthday!", "생일 축하해!", "DAILY_LIFE", "ELEMENTARY", 1),
        ("holiday", "/ˈhɑːlɪˌdeɪ/", "휴일, 방학", "noun", "We are on holiday.", "우리는 휴가 중입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("party", "/ˈpɑːrti/", "파티, 모임", "noun", "I went to a birthday party.", "생일 파티에 갔습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("mistake", "/mɪˈsteɪk/", "실수", "noun", "I made a mistake.", "실수를 했습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("idea", "/aɪˈdiːə/", "생각, 아이디어", "noun", "That's a good idea!", "좋은 생각이야!", "DAILY_LIFE", "ELEMENTARY", 1),
        ("problem", "/ˈprɑːbləm/", "문제", "noun", "I have a problem.", "문제가 있습니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("reason", "/ˈriːzən/", "이유", "noun", "What is the reason?", "이유가 뭐예요?", "DAILY_LIFE", "ELEMENTARY", 1),
        ("part", "/pɑːrt/", "부분", "noun", "This is part of the plan.", "이것은 계획의 일부입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("place", "/pleɪs/", "장소", "noun", "This is a nice place.", "여기는 좋은 장소입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("room", "/ruːm/", "방", "noun", "My room is clean.", "내 방은 깨끗합니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("road", "/roʊd/", "도로, 길", "noun", "Cross the road carefully.", "도로를 조심히 건너세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("side", "/saɪd/", "측면, 옆", "noun", "Sit by my side.", "내 옆에 앉아.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("end", "/ɛnd/", "끝, 끝나다", "noun/verb", "This is the end of the story.", "이야기의 끝입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("point", "/pɔɪnt/", "점, 요점", "noun", "That's a good point.", "좋은 지적입니다.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("line", "/laɪn/", "줄, 선", "noun", "Stand in a line.", "줄을 서세요.", "DAILY_LIFE", "ELEMENTARY", 1),
        ("shape", "/ʃeɪp/", "모양", "noun", "What shape is it?", "무슨 모양인가요?", "EDUCATION", "ELEMENTARY", 1),
        ("round", "/raʊnd/", "둥근", "adjective", "The earth is round.", "지구는 둥급니다.", "EDUCATION", "ELEMENTARY", 1),
        ("square", "/skwɛr/", "정사각형, 광장", "noun", "Draw a square.", "정사각형을 그리세요.", "EDUCATION", "ELEMENTARY", 1),
        ("circle", "/ˈsɜːrkəl/", "원, 동그라미", "noun", "Draw a circle.", "원을 그리세요.", "EDUCATION", "ELEMENTARY", 1),
    ]
    all_words.extend(elementary)

    # ---- 중학교 ~ 전문가 수준 고빈도 단어 대량 추가 ----
    # 구조: (word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, difficulty)
    more_words = [
        # MIDDLE_SCHOOL
        ("communicate", "/kəˈmjuːnɪˌkeɪt/", "의사소통하다", "verb", "We communicate through email.", "이메일로 의사소통합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("compare", "/kəmˈpɛr/", "비교하다", "verb", "Compare the two answers.", "두 답을 비교하세요.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("complete", "/kəmˈpliːt/", "완료하다, 완전한", "verb/adj", "Please complete the form.", "양식을 작성해 주세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("concern", "/kənˈsɜːrn/", "걱정, 관련되다", "noun/verb", "This concerns everyone.", "이것은 모든 사람과 관련됩니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("connect", "/kəˈnɛkt/", "연결하다", "verb", "Connect the cable to the computer.", "케이블을 컴퓨터에 연결하세요.", "TECHNOLOGY", "MIDDLE_SCHOOL", 2),
        ("consider", "/kənˈsɪdər/", "고려하다", "verb", "Consider all the options.", "모든 선택지를 고려하세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("contain", "/kənˈteɪn/", "포함하다", "verb", "This box contains books.", "이 상자에는 책이 들어 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("continue", "/kənˈtɪnjuː/", "계속하다", "verb", "Please continue reading.", "계속 읽어 주세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("control", "/kənˈtroʊl/", "제어하다, 통제", "verb/noun", "You must control your temper.", "화를 다스려야 합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("create", "/kriˈeɪt/", "만들다, 창조하다", "verb", "She creates beautiful art.", "그녀는 아름다운 예술을 창작합니다.", "ARTS", "MIDDLE_SCHOOL", 2),
        ("culture", "/ˈkʌltʃər/", "문화", "noun", "Korean culture is fascinating.", "한국 문화는 매력적입니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("customer", "/ˈkʌstəmər/", "고객", "noun", "The customer is always right.", "고객은 항상 옳습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("damage", "/ˈdæmɪdʒ/", "손상, 피해", "noun/verb", "The storm caused damage.", "폭풍이 피해를 입혔습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("danger", "/ˈdeɪndʒər/", "위험", "noun", "There is danger ahead.", "앞에 위험이 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("decision", "/dɪˈsɪʒən/", "결정", "noun", "Make a decision now.", "지금 결정하세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("deliver", "/dɪˈlɪvər/", "배달하다, 전달하다", "verb", "They deliver food to your door.", "음식을 문앞까지 배달합니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("demand", "/dɪˈmænd/", "수요, 요구하다", "noun/verb", "There is a high demand for this product.", "이 제품에 대한 수요가 높습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("department", "/dɪˈpɑːrtmənt/", "부서, 학과", "noun", "She works in the marketing department.", "그녀는 마케팅 부서에서 일합니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("describe", "/dɪˈskraɪb/", "묘사하다", "verb", "Describe what you see.", "보이는 것을 묘사하세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("design", "/dɪˈzaɪn/", "디자인, 설계하다", "noun/verb", "She designs websites.", "그녀는 웹사이트를 디자인합니다.", "TECHNOLOGY", "MIDDLE_SCHOOL", 2),
        ("destroy", "/dɪˈstrɔɪ/", "파괴하다", "verb", "The fire destroyed the building.", "화재가 건물을 파괴했습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("develop", "/dɪˈvɛləp/", "개발하다, 발전하다", "verb", "We develop new software.", "새로운 소프트웨어를 개발합니다.", "TECHNOLOGY", "MIDDLE_SCHOOL", 2),
        ("direction", "/dɪˈrɛkʃən/", "방향", "noun", "Which direction should we go?", "어느 방향으로 가야 하나요?", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("discuss", "/dɪˈskʌs/", "토론하다, 논의하다", "verb", "Let's discuss the plan.", "계획을 논의합시다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("distance", "/ˈdɪstəns/", "거리", "noun", "The distance is ten kilometers.", "거리는 10킬로미터입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("divide", "/dɪˈvaɪd/", "나누다", "verb", "Divide the cake into eight pieces.", "케이크를 여덟 조각으로 나누세요.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("doubt", "/daʊt/", "의심, 의심하다", "noun/verb", "I doubt that's true.", "그것이 사실인지 의심합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("earn", "/ɜːrn/", "벌다, 얻다", "verb", "She earns a good salary.", "그녀는 좋은 급여를 벌고 있습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("economy", "/ɪˈkɑːnəmi/", "경제", "noun", "The economy is growing.", "경제가 성장하고 있습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("education", "/ˌɛdʒuˈkeɪʃən/", "교육", "noun", "Education is very important.", "교육은 매우 중요합니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("effect", "/ɪˈfɛkt/", "효과, 영향", "noun", "The medicine has side effects.", "약에 부작용이 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("effort", "/ˈɛfərt/", "노력", "noun", "He put a lot of effort into it.", "그는 많은 노력을 기울였습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("election", "/ɪˈlɛkʃən/", "선거", "noun", "The election is next month.", "선거가 다음 달입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("employ", "/ɪmˈplɔɪ/", "고용하다", "verb", "The company employs 500 people.", "회사는 500명을 고용합니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("encourage", "/ɪnˈkɜːrɪdʒ/", "격려하다", "verb", "Parents encourage their children.", "부모는 자녀를 격려합니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("environment", "/ɪnˈvaɪrənmənt/", "환경", "noun", "Protect the environment.", "환경을 보호하세요.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("equipment", "/ɪˈkwɪpmənt/", "장비", "noun", "We need new equipment.", "새 장비가 필요합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("escape", "/ɪˈskeɪp/", "탈출하다", "verb", "The prisoner escaped.", "죄수가 탈출했습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("essential", "/ɪˈsɛnʃəl/", "필수적인", "adjective", "Water is essential for life.", "물은 생명에 필수적입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("establish", "/ɪˈstæblɪʃ/", "설립하다", "verb", "The school was established in 1900.", "학교는 1900년에 설립되었습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("event", "/ɪˈvɛnt/", "행사, 사건", "noun", "The event was a success.", "행사가 성공적이었습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("evidence", "/ˈɛvɪdəns/", "증거", "noun", "There is no evidence.", "증거가 없습니다.", "LAW", "MIDDLE_SCHOOL", 2),
        ("exactly", "/ɪɡˈzæktli/", "정확히", "adverb", "That is exactly right.", "그것이 정확히 맞습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("examine", "/ɪɡˈzæmɪn/", "검사하다, 조사하다", "verb", "The doctor examined the patient.", "의사가 환자를 진찰했습니다.", "MEDICINE", "MIDDLE_SCHOOL", 2),
        ("excellent", "/ˈɛksələnt/", "훌륭한", "adjective", "The food was excellent.", "음식이 훌륭했습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("exchange", "/ɪksˈtʃeɪndʒ/", "교환, 환전", "noun/verb", "I want to exchange money.", "환전하고 싶습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("exist", "/ɪɡˈzɪst/", "존재하다", "verb", "Do aliens exist?", "외계인이 존재할까요?", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("expect", "/ɪkˈspɛkt/", "예상하다, 기대하다", "verb", "I expect good results.", "좋은 결과를 기대합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("expensive", "/ɪkˈspɛnsɪv/", "비싼", "adjective", "The car is expensive.", "자동차가 비쌉니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2),
        ("experience", "/ɪkˈspɪriəns/", "경험", "noun", "She has a lot of experience.", "그녀는 경험이 많습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("express", "/ɪkˈsprɛs/", "표현하다", "verb", "Express your feelings.", "감정을 표현하세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("fail", "/feɪl/", "실패하다", "verb", "Don't be afraid to fail.", "실패를 두려워하지 마세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("fair", "/fɛr/", "공정한, 박람회", "adjective/noun", "That's not fair!", "그건 공정하지 않아!", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("familiar", "/fəˈmɪliər/", "익숙한", "adjective", "This place looks familiar.", "이 장소가 익숙해 보입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("fashion", "/ˈfæʃən/", "유행, 패션", "noun", "She follows the latest fashion.", "그녀는 최신 유행을 따릅니다.", "ARTS", "MIDDLE_SCHOOL", 2),
        ("favor", "/ˈfeɪvər/", "호의, 부탁", "noun", "Can you do me a favor?", "부탁 하나 해도 될까요?", "DAILY_LIFE", "MIDDLE_SCHOOL", 2),
        ("feature", "/ˈfiːtʃər/", "특징, 기능", "noun", "This phone has many features.", "이 전화기는 기능이 많습니다.", "TECHNOLOGY", "MIDDLE_SCHOOL", 2),
        ("figure", "/ˈfɪɡjər/", "숫자, 인물, 모습", "noun", "The figure shows the results.", "그 도표가 결과를 보여줍니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("final", "/ˈfaɪnəl/", "마지막의, 최종의", "adjective", "This is the final exam.", "이것은 기말고사입니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("foreign", "/ˈfɔːrən/", "외국의", "adjective", "She speaks many foreign languages.", "그녀는 여러 외국어를 합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("fortunate", "/ˈfɔːrtʃənət/", "운이 좋은", "adjective", "We were fortunate to survive.", "생존한 것은 운이 좋았습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("freedom", "/ˈfriːdəm/", "자유", "noun", "Freedom is precious.", "자유는 소중합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("frequent", "/ˈfriːkwənt/", "빈번한", "adjective", "Buses are frequent here.", "여기는 버스가 자주 옵니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("general", "/ˈdʒɛnərəl/", "일반적인, 장군", "adjective/noun", "In general, this is correct.", "일반적으로, 이것은 맞습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("generate", "/ˈdʒɛnəˌreɪt/", "생성하다", "verb", "Solar panels generate electricity.", "태양 전지판이 전기를 생성합니다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("global", "/ˈɡloʊbəl/", "전 세계의", "adjective", "Climate change is a global issue.", "기후 변화는 세계적인 문제입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("government", "/ˈɡʌvərnmənt/", "정부", "noun", "The government made a new law.", "정부가 새 법을 만들었습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("gradually", "/ˈɡrædʒuəli/", "점차적으로", "adverb", "The weather gradually improved.", "날씨가 점차 좋아졌습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("guarantee", "/ˌɡærənˈtiː/", "보증, 보장하다", "noun/verb", "We guarantee quality.", "품질을 보장합니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("guess", "/ɡɛs/", "추측하다", "verb", "Can you guess the answer?", "답을 추측할 수 있나요?", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("guide", "/ɡaɪd/", "안내하다, 가이드", "verb/noun", "The guide showed us around.", "가이드가 우리를 안내했습니다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("habit", "/ˈhæbɪt/", "습관", "noun", "Reading is a good habit.", "독서는 좋은 습관입니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2),
        ("handle", "/ˈhændəl/", "다루다, 손잡이", "verb/noun", "She handles problems well.", "그녀는 문제를 잘 처리합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("hardly", "/ˈhɑːrdli/", "거의 ~않다", "adverb", "I hardly ever watch TV.", "나는 거의 TV를 보지 않습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("honor", "/ˈɑːnər/", "명예, 영광", "noun", "It's an honor to meet you.", "만나서 영광입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("huge", "/hjuːdʒ/", "거대한", "adjective", "The building is huge.", "건물이 거대합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("imagine", "/ɪˈmædʒɪn/", "상상하다", "verb", "Imagine a world without war.", "전쟁 없는 세상을 상상해 보세요.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("immediate", "/ɪˈmiːdiət/", "즉각적인", "adjective", "We need immediate action.", "즉각적인 조치가 필요합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("impact", "/ˈɪmpækt/", "영향, 충격", "noun", "Technology has a big impact.", "기술은 큰 영향을 미칩니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("import", "/ˈɪmpɔːrt/", "수입하다, 수입품", "verb/noun", "We import coffee from Brazil.", "브라질에서 커피를 수입합니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("improve", "/ɪmˈpruːv/", "개선하다", "verb", "I want to improve my English.", "영어를 향상시키고 싶습니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("include", "/ɪnˈkluːd/", "포함하다", "verb", "The price includes tax.", "가격에 세금이 포함됩니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("increase", "/ɪnˈkriːs/", "증가하다, 증가", "verb/noun", "Prices have increased.", "가격이 올랐습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("industry", "/ˈɪndəstri/", "산업", "noun", "The car industry is growing.", "자동차 산업이 성장하고 있습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("influence", "/ˈɪnfluəns/", "영향, 영향을 미치다", "noun/verb", "Music influences our mood.", "음악은 기분에 영향을 미칩니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("information", "/ˌɪnfərˈmeɪʃən/", "정보", "noun", "I need more information.", "더 많은 정보가 필요합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("injure", "/ˈɪndʒər/", "부상을 입히다", "verb", "He was injured in the accident.", "그는 사고에서 부상을 입었습니다.", "MEDICINE", "MIDDLE_SCHOOL", 2),
        ("insist", "/ɪnˈsɪst/", "주장하다, 고집하다", "verb", "She insists on going alone.", "그녀는 혼자 가겠다고 고집합니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("instead", "/ɪnˈstɛd/", "대신에", "adverb", "I'll have tea instead of coffee.", "커피 대신 차를 마시겠습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("interest", "/ˈɪntrɪst/", "관심, 이자", "noun", "I have an interest in science.", "과학에 관심이 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("international", "/ˌɪntərˈnæʃənəl/", "국제적인", "adjective", "This is an international event.", "이것은 국제 행사입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("introduce", "/ˌɪntrəˈduːs/", "소개하다", "verb", "Let me introduce myself.", "자기소개를 하겠습니다.", "DAILY_LIFE", "MIDDLE_SCHOOL", 2),
        ("invent", "/ɪnˈvɛnt/", "발명하다", "verb", "Who invented the telephone?", "누가 전화기를 발명했나요?", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("invest", "/ɪnˈvɛst/", "투자하다", "verb", "He invested in stocks.", "그는 주식에 투자했습니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("involve", "/ɪnˈvɑːlv/", "관련시키다, 포함하다", "verb", "The project involves many people.", "프로젝트에 많은 사람이 관련됩니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("issue", "/ˈɪʃuː/", "문제, 쟁점", "noun", "This is a serious issue.", "이것은 심각한 문제입니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("join", "/dʒɔɪn/", "참여하다, 합류하다", "verb", "Join our team!", "우리 팀에 합류하세요!", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("judge", "/dʒʌdʒ/", "판단하다, 판사", "verb/noun", "Don't judge people by appearance.", "외모로 사람을 판단하지 마세요.", "LAW", "MIDDLE_SCHOOL", 2),
        ("justice", "/ˈdʒʌstɪs/", "정의, 공정", "noun", "We fight for justice.", "우리는 정의를 위해 싸웁니다.", "LAW", "MIDDLE_SCHOOL", 2),
        ("knowledge", "/ˈnɑːlɪdʒ/", "지식", "noun", "Knowledge is power.", "지식이 힘입니다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("language", "/ˈlæŋɡwɪdʒ/", "언어", "noun", "How many languages do you speak?", "몇 개 언어를 하세요?", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("law", "/lɔː/", "법, 법률", "noun", "Everyone must follow the law.", "모든 사람은 법을 따라야 합니다.", "LAW", "MIDDLE_SCHOOL", 2),
        ("lead", "/liːd/", "이끌다, 납", "verb/noun", "She leads the project.", "그녀가 프로젝트를 이끕니다.", "BUSINESS", "MIDDLE_SCHOOL", 2),
        ("limit", "/ˈlɪmɪt/", "한계, 제한하다", "noun/verb", "There is a speed limit.", "속도 제한이 있습니다.", "GENERAL", "MIDDLE_SCHOOL", 2),
    ]
    all_words.extend(more_words)

    # 고등학교 추가
    hs_more = [
        ("elaborate", "/ɪˈlæbərət/", "정교한, 상세히 설명하다", "adjective/verb", "The plan is elaborate.", "계획이 정교합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("eliminate", "/ɪˈlɪmɪˌneɪt/", "제거하다", "verb", "We must eliminate errors.", "오류를 제거해야 합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("emerge", "/ɪˈmɜːrdʒ/", "나타나다, 부상하다", "verb", "New leaders emerged.", "새로운 지도자들이 나타났습니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("emphasis", "/ˈɛmfəsɪs/", "강조", "noun", "The emphasis is on quality.", "품질에 강조를 둡니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("enable", "/ɪˈneɪbəl/", "가능하게 하다", "verb", "Technology enables communication.", "기술이 의사소통을 가능하게 합니다.", "TECHNOLOGY", "HIGH_SCHOOL", 3),
        ("encounter", "/ɪnˈkaʊntər/", "만나다, 직면하다", "verb", "We encountered a problem.", "문제에 직면했습니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("enhance", "/ɪnˈhæns/", "향상시키다", "verb", "This will enhance performance.", "이것은 성능을 향상시킬 것입니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("enormous", "/ɪˈnɔːrməs/", "거대한", "adjective", "The task is enormous.", "과제가 거대합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("ensure", "/ɪnˈʃʊr/", "보장하다", "verb", "We ensure high quality.", "높은 품질을 보장합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("estimate", "/ˈɛstɪˌmeɪt/", "추정하다, 추정치", "verb/noun", "I estimate the cost at $500.", "비용을 500달러로 추정합니다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("evaluate", "/ɪˈvæljuˌeɪt/", "평가하다", "verb", "We evaluate student performance.", "학생 성과를 평가합니다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("evident", "/ˈɛvɪdənt/", "명백한", "adjective", "The answer is evident.", "답은 명백합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("evolve", "/ɪˈvɑːlv/", "진화하다, 발전하다", "verb", "Technology continues to evolve.", "기술은 계속 진화합니다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("exceed", "/ɪkˈsiːd/", "초과하다", "verb", "Do not exceed the speed limit.", "속도 제한을 초과하지 마세요.", "GENERAL", "HIGH_SCHOOL", 3),
        ("exclude", "/ɪkˈskluːd/", "배제하다", "verb", "Don't exclude anyone.", "누구도 배제하지 마세요.", "GENERAL", "HIGH_SCHOOL", 3),
        ("exhibit", "/ɪɡˈzɪbɪt/", "전시하다, 나타내다", "verb", "The museum exhibits ancient art.", "박물관이 고대 예술품을 전시합니다.", "ARTS", "HIGH_SCHOOL", 3),
        ("expand", "/ɪkˈspænd/", "확장하다", "verb", "The company plans to expand.", "회사가 확장할 계획입니다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("exploit", "/ɪkˈsplɔɪt/", "이용하다, 착취하다", "verb", "Don't exploit natural resources.", "천연자원을 남용하지 마세요.", "GENERAL", "HIGH_SCHOOL", 3),
        ("expose", "/ɪkˈspoʊz/", "노출시키다, 폭로하다", "verb", "The reporter exposed the scandal.", "기자가 스캔들을 폭로했습니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("extent", "/ɪkˈstɛnt/", "범위, 정도", "noun", "To what extent do you agree?", "어느 정도 동의하시나요?", "GENERAL", "HIGH_SCHOOL", 3),
        ("external", "/ɪkˈstɜːrnəl/", "외부의", "adjective", "External factors affect results.", "외부 요인이 결과에 영향을 미칩니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("facilitate", "/fəˈsɪlɪˌteɪt/", "촉진하다, 용이하게 하다", "verb", "This tool facilitates learning.", "이 도구는 학습을 용이하게 합니다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("factor", "/ˈfæktər/", "요인", "noun", "Many factors contribute to success.", "많은 요인이 성공에 기여합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("flexible", "/ˈflɛksɪbəl/", "유연한", "adjective", "Be flexible with your schedule.", "일정에 유연하게 대처하세요.", "GENERAL", "HIGH_SCHOOL", 3),
        ("focus", "/ˈfoʊkəs/", "집중하다, 초점", "verb/noun", "Focus on your work.", "일에 집중하세요.", "GENERAL", "HIGH_SCHOOL", 3),
        ("former", "/ˈfɔːrmər/", "이전의", "adjective", "The former president attended.", "전 대통령이 참석했습니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("foundation", "/faʊnˈdeɪʃən/", "기초, 재단", "noun", "Education is the foundation of success.", "교육은 성공의 기초입니다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("framework", "/ˈfreɪmˌwɜːrk/", "체계, 틀", "noun", "We need a clear framework.", "명확한 체계가 필요합니다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("function", "/ˈfʌŋkʃən/", "기능, 함수", "noun", "What is the function of this button?", "이 버튼의 기능은 무엇인가요?", "TECHNOLOGY", "HIGH_SCHOOL", 3),
        ("fundamental", "/ˌfʌndəˈmɛntəl/", "근본적인", "adjective", "This is a fundamental principle.", "이것은 근본적인 원칙입니다.", "GENERAL", "HIGH_SCHOOL", 3),
    ]
    all_words.extend(hs_more)

    # 대학교 추가
    college_more = [
        ("paradigm", "/ˈpærəˌdaɪm/", "패러다임, 모범", "noun", "A new paradigm has emerged.", "새로운 패러다임이 등장했습니다.", "GENERAL", "COLLEGE", 4),
        ("paradox", "/ˈpærəˌdɑːks/", "역설, 모순", "noun", "This is a logical paradox.", "이것은 논리적 역설입니다.", "GENERAL", "COLLEGE", 4),
        ("parameter", "/pəˈræmɪtər/", "매개변수, 기준", "noun", "Set the parameters for the test.", "테스트 매개변수를 설정하세요.", "TECHNOLOGY", "COLLEGE", 4),
        ("perceive", "/pərˈsiːv/", "인식하다, 감지하다", "verb", "People perceive the world differently.", "사람들은 세상을 다르게 인식합니다.", "GENERAL", "COLLEGE", 4),
        ("perspective", "/pərˈspɛktɪv/", "관점, 시각", "noun", "Consider it from a different perspective.", "다른 관점에서 생각해 보세요.", "GENERAL", "COLLEGE", 4),
        ("phenomenon", "/fɪˈnɑːmɪnən/", "현상", "noun", "This is a rare phenomenon.", "이것은 드문 현상입니다.", "SCIENCE", "COLLEGE", 4),
        ("philosophy", "/fɪˈlɑːsəfi/", "철학", "noun", "She studies philosophy.", "그녀는 철학을 공부합니다.", "EDUCATION", "COLLEGE", 4),
        ("pioneer", "/ˌpaɪəˈnɪr/", "개척자, 선구자", "noun", "She was a pioneer in science.", "그녀는 과학의 선구자였습니다.", "SCIENCE", "COLLEGE", 4),
        ("plausible", "/ˈplɔːzɪbəl/", "그럴듯한", "adjective", "The explanation is plausible.", "설명이 그럴듯합니다.", "GENERAL", "COLLEGE", 4),
        ("potential", "/pəˈtɛnʃəl/", "잠재력, 잠재적인", "noun/adjective", "She has great potential.", "그녀는 큰 잠재력을 가지고 있습니다.", "GENERAL", "COLLEGE", 4),
        ("predominant", "/prɪˈdɑːmɪnənt/", "지배적인, 우세한", "adjective", "English is the predominant language.", "영어가 지배적인 언어입니다.", "GENERAL", "COLLEGE", 4),
        ("preliminary", "/prɪˈlɪmɪˌnɛri/", "예비의, 서론의", "adjective", "These are preliminary results.", "이것은 예비 결과입니다.", "GENERAL", "COLLEGE", 4),
        ("prevalent", "/ˈprɛvələnt/", "만연한, 유행하는", "adjective", "The disease is prevalent in the region.", "그 질병이 지역에 만연합니다.", "MEDICINE", "COLLEGE", 4),
        ("principle", "/ˈprɪnsɪpəl/", "원칙, 원리", "noun", "Follow the basic principles.", "기본 원칙을 따르세요.", "GENERAL", "COLLEGE", 4),
        ("prior", "/ˈpraɪər/", "이전의, 사전의", "adjective", "Prior experience is required.", "사전 경험이 필요합니다.", "BUSINESS", "COLLEGE", 4),
        ("proceed", "/proʊˈsiːd/", "진행하다", "verb", "Please proceed with the plan.", "계획을 진행해 주세요.", "GENERAL", "COLLEGE", 4),
        ("prohibit", "/proʊˈhɪbɪt/", "금지하다", "verb", "Smoking is prohibited here.", "여기서는 흡연이 금지됩니다.", "LAW", "COLLEGE", 4),
        ("prominent", "/ˈprɑːmɪnənt/", "저명한, 두드러진", "adjective", "He is a prominent scientist.", "그는 저명한 과학자입니다.", "GENERAL", "COLLEGE", 4),
        ("proportion", "/prəˈpɔːrʃən/", "비율, 비례", "noun", "A large proportion of students passed.", "학생의 큰 비율이 통과했습니다.", "GENERAL", "COLLEGE", 4),
        ("pursue", "/pərˈsuː/", "추구하다, 뒤쫓다", "verb", "She pursues her dream.", "그녀는 꿈을 추구합니다.", "GENERAL", "COLLEGE", 4),
    ]
    all_words.extend(college_more)

    # 전문가 수준 추가
    prof_more = [
        ("meticulous", "/mɪˈtɪkjələs/", "꼼꼼한, 세심한", "adjective", "She is meticulous in her work.", "그녀는 일에 꼼꼼합니다.", "GENERAL", "PROFESSIONAL", 5),
        ("mitigate", "/ˈmɪtɪˌɡeɪt/", "완화하다", "verb", "We must mitigate the risks.", "위험을 완화해야 합니다.", "BUSINESS", "PROFESSIONAL", 5),
        ("nuance", "/ˈnjuːɑːns/", "뉘앙스, 미묘한 차이", "noun", "The nuances of language are important.", "언어의 미묘한 차이는 중요합니다.", "GENERAL", "PROFESSIONAL", 5),
        ("obsolete", "/ˌɑːbsəˈliːt/", "쓸모없는, 구식의", "adjective", "The technology is now obsolete.", "그 기술은 이제 구식입니다.", "TECHNOLOGY", "PROFESSIONAL", 5),
        ("paradigm", "/ˈpærəˌdaɪm/", "패러다임", "noun", "We need a paradigm shift.", "패러다임 전환이 필요합니다.", "GENERAL", "PROFESSIONAL", 5),
        ("paradoxical", "/ˌpærəˈdɑːksɪkəl/", "역설적인", "adjective", "The situation is paradoxical.", "상황이 역설적입니다.", "GENERAL", "PROFESSIONAL", 5),
        ("peripheral", "/pəˈrɪfərəl/", "주변부의, 부차적인", "adjective", "This is a peripheral issue.", "이것은 부차적인 문제입니다.", "GENERAL", "PROFESSIONAL", 5),
        ("perpetuate", "/pərˈpɛtʃuˌeɪt/", "영속시키다", "verb", "Don't perpetuate stereotypes.", "고정관념을 영속시키지 마세요.", "GENERAL", "PROFESSIONAL", 5),
        ("pragmatic", "/præɡˈmætɪk/", "실용적인", "adjective", "We need a pragmatic approach.", "실용적인 접근이 필요합니다.", "BUSINESS", "PROFESSIONAL", 5),
        ("precedent", "/ˈprɛsɪdənt/", "선례, 판례", "noun", "This sets a dangerous precedent.", "이것은 위험한 선례를 남깁니다.", "LAW", "PROFESSIONAL", 5),
        ("proliferate", "/proʊˈlɪfəˌreɪt/", "확산하다, 급증하다", "verb", "Technology has proliferated.", "기술이 급증했습니다.", "GENERAL", "PROFESSIONAL", 5),
        ("propensity", "/prəˈpɛnsɪti/", "경향, 성향", "noun", "He has a propensity for risk.", "그는 위험을 감수하는 성향이 있습니다.", "GENERAL", "PROFESSIONAL", 5),
        ("reconcile", "/ˈrɛkənˌsaɪl/", "화해시키다, 조정하다", "verb", "They reconciled their differences.", "그들은 차이를 조정했습니다.", "GENERAL", "PROFESSIONAL", 5),
        ("resilient", "/rɪˈzɪliənt/", "탄력 있는, 회복력 있는", "adjective", "She is a resilient person.", "그녀는 회복력 있는 사람입니다.", "GENERAL", "PROFESSIONAL", 5),
        ("scrutinize", "/ˈskruːtəˌnaɪz/", "면밀히 조사하다", "verb", "The committee will scrutinize the report.", "위원회가 보고서를 면밀히 조사할 것입니다.", "LAW", "PROFESSIONAL", 5),
        ("substantiate", "/səbˈstænʃiˌeɪt/", "입증하다, 실증하다", "verb", "Can you substantiate your claim?", "주장을 입증할 수 있나요?", "LAW", "PROFESSIONAL", 5),
        ("supersede", "/ˌsuːpərˈsiːd/", "대체하다", "verb", "New rules supersede the old ones.", "새 규칙이 기존 규칙을 대체합니다.", "LAW", "PROFESSIONAL", 5),
        ("synthesize", "/ˈsɪnθəˌsaɪz/", "합성하다, 종합하다", "verb", "The chemist synthesized a new compound.", "화학자가 새 화합물을 합성했습니다.", "SCIENCE", "PROFESSIONAL", 5),
        ("tangible", "/ˈtændʒɪbəl/", "유형의, 만질 수 있는", "adjective", "We need tangible results.", "유형의 결과가 필요합니다.", "BUSINESS", "PROFESSIONAL", 5),
        ("unprecedented", "/ʌnˈprɛsɪˌdɛntɪd/", "전례 없는", "adjective", "This is an unprecedented event.", "이것은 전례 없는 사건입니다.", "GENERAL", "PROFESSIONAL", 5),
    ]
    all_words.extend(prof_more)

    # DB에 삽입
    for item in all_words:
        word = item[0]
        if word in existing:
            continue
        existing.add(word)

        pron, meaning, pos, ex_en, ex_ko, domain, age_group, difficulty = item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8]

        cursor.execute("""
            INSERT INTO words (id, word, pronunciation, meaning_ko, part_of_speech, example_en, example_ko,
                              domain, age_group, frequency_rank, difficulty, synonyms, antonyms, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL)
        """, (word_id, word, pron, meaning, pos, ex_en, ex_ko, domain, age_group, word_id, difficulty))
        word_id += 1
        added += 1

    conn.commit()

    # 최종 통계
    cursor.execute("SELECT COUNT(*) FROM words")
    total = cursor.fetchone()[0]
    cursor.execute("SELECT age_group, COUNT(*) FROM words GROUP BY age_group ORDER BY age_group")
    by_age = cursor.fetchall()
    cursor.execute("SELECT domain, COUNT(*) FROM words GROUP BY domain ORDER BY domain")
    by_domain = cursor.fetchall()

    print(f"\n{'='*50}")
    print(f"단어 확장 완료: {added}개 추가")
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
    expand_database()
