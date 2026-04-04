#!/usr/bin/env python3
"""JSON 단어 데이터 파일 대량 생성 - word_data/ 디렉토리에 저장"""

import json, os

DATA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "word_data")

def w(word, meaning, pos="noun", pron="", ex_en="", ex_ko="", domain="GENERAL", age="HIGH_SCHOOL", notes=None):
    d = {"word": word, "meaning": meaning, "pos": pos, "pron": pron,
         "ex_en": ex_en, "ex_ko": ex_ko, "domain": domain, "age_group": age}
    if notes: d["notes"] = notes
    return d

def save(filename, data):
    path = os.path.join(DATA_DIR, filename)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=1)
    print(f"  저장: {filename} ({len(data)}개)")

def main():
    os.makedirs(DATA_DIR, exist_ok=True)

    # 05: 신체/건강
    save("05_body_health.json", [
        w("ankle", "발목", "noun", "/ˈæŋkəl/", "She twisted her ankle.", "그녀는 발목을 삐었다.", "MEDICINE", "ELEMENTARY"),
        w("wrist", "손목", "noun", "/rɪst/", "He wore a watch on his wrist.", "그는 손목에 시계를 착용했다.", "MEDICINE", "ELEMENTARY"),
        w("elbow", "팔꿈치", "noun", "/ˈelboʊ/", "Don't put elbows on the table.", "식탁에 팔꿈치를 올리지 마세요.", "MEDICINE", "ELEMENTARY"),
        w("shoulder", "어깨", "noun", "/ˈʃoʊldər/", "He put his arm around her shoulder.", "그는 어깨에 팔을 둘렀다.", "MEDICINE", "ELEMENTARY"),
        w("chest", "가슴", "noun", "/tʃest/", "He felt pain in his chest.", "가슴에 통증을 느꼈다.", "MEDICINE", "ELEMENTARY"),
        w("forehead", "이마", "noun", "/ˈfɔːrhed/", "She wiped sweat from her forehead.", "이마에서 땀을 닦았다.", "MEDICINE", "ELEMENTARY"),
        w("thumb", "엄지손가락", "noun", "/θʌm/", "She gave a thumbs up.", "엄지를 들었다.", "MEDICINE", "ELEMENTARY"),
        w("jaw", "턱", "noun", "/dʒɔː/", "He broke his jaw.", "턱이 부러졌다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("spine", "척추", "noun", "/spaɪn/", "Protect your spine when lifting.", "들 때 척추를 보호하세요.", "MEDICINE", "HIGH_SCHOOL"),
        w("rib", "갈비뼈", "noun", "/rɪb/", "He cracked a rib.", "갈비뼈에 금이 갔다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("kidney", "신장", "noun", "/ˈkɪdni/", "Kidneys filter blood.", "신장은 혈액을 걸러준다.", "MEDICINE", "HIGH_SCHOOL"),
        w("liver", "간", "noun", "/ˈlɪvər/", "The liver detoxifies the body.", "간은 독소를 제거한다.", "MEDICINE", "HIGH_SCHOOL"),
        w("lung", "폐", "noun", "/lʌŋ/", "Smoking damages the lungs.", "흡연은 폐를 손상시킨다.", "MEDICINE", "HIGH_SCHOOL"),
        w("skull", "두개골", "noun", "/skʌl/", "The helmet protected his skull.", "헬멧이 두개골을 보호했다.", "MEDICINE", "HIGH_SCHOOL"),
        w("muscle", "근육", "noun", "/ˈmʌsəl/", "Exercise builds muscle.", "운동은 근육을 키운다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("bone", "뼈", "noun", "/boʊn/", "Calcium strengthens bones.", "칼슘은 뼈를 튼튼하게 한다.", "MEDICINE", "ELEMENTARY"),
        w("skin", "피부", "noun", "/skɪn/", "Apply sunscreen to protect skin.", "피부 보호를 위해 자외선 차단제를 바르세요.", "MEDICINE", "ELEMENTARY"),
        w("nerve", "신경", "noun", "/nɜːrv/", "The dentist hit a nerve.", "치과의사가 신경을 건드렸다.", "MEDICINE", "HIGH_SCHOOL"),
        w("joint", "관절", "noun", "/dʒɔɪnt/", "Joint pain is common in elderly.", "관절 통증은 노인에게 흔하다.", "MEDICINE", "HIGH_SCHOOL"),
        w("symptom", "증상", "noun", "/ˈsɪmptəm/", "Fever is a common symptom.", "열은 흔한 증상이다.", "MEDICINE", "HIGH_SCHOOL"),
        w("surgery", "수술", "noun", "/ˈsɜːrdʒəri/", "The surgery was successful.", "수술은 성공적이었다.", "MEDICINE", "HIGH_SCHOOL"),
        w("bandage", "붕대", "noun", "/ˈbændɪdʒ/", "She wrapped the wound.", "상처에 붕대를 감았다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("bruise", "멍", "noun", "/bruːz/", "He had a bruise on his arm.", "팔에 멍이 있었다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("fracture", "골절", "noun", "/ˈfræktʃər/", "The X-ray showed a fracture.", "엑스레이에 골절이 보였다.", "MEDICINE", "HIGH_SCHOOL"),
        w("infection", "감염", "noun", "/ɪnˈfekʃən/", "The wound became infected.", "상처가 감염되었다.", "MEDICINE", "HIGH_SCHOOL"),
        w("fever", "열", "noun", "/ˈfiːvər/", "She has a high fever.", "고열이 있다.", "MEDICINE", "ELEMENTARY"),
        w("cough", "기침", "noun", "/kɒf/", "He has a bad cough.", "심한 기침이 있다.", "MEDICINE", "ELEMENTARY"),
        w("sneeze", "재채기하다", "verb", "/sniːz/", "She sneezed three times.", "세 번 재채기했다.", "MEDICINE", "ELEMENTARY"),
        w("swollen", "부어오른", "adjective", "/ˈswoʊlən/", "Her ankle was swollen.", "발목이 부어올랐다.", "MEDICINE", "MIDDLE_SCHOOL"),
        w("dizzy", "어지러운", "adjective", "/ˈdɪzi/", "She felt dizzy.", "어지러움을 느꼈다.", "MEDICINE", "ELEMENTARY"),
    ])

    # 06: 동물
    save("06_animals.json", [
        w("dolphin", "돌고래", "noun", "/ˈdɒlfɪn/", "Dolphins are intelligent.", "돌고래는 지능이 높다.", "SCIENCE", "ELEMENTARY"),
        w("whale", "고래", "noun", "/weɪl/", "Blue whales are the largest animals.", "대왕고래는 가장 큰 동물이다.", "SCIENCE", "ELEMENTARY"),
        w("eagle", "독수리", "noun", "/ˈiːɡəl/", "The eagle soared above.", "독수리가 위를 날았다.", "SCIENCE", "ELEMENTARY"),
        w("penguin", "펭귄", "noun", "/ˈpeŋɡwɪn/", "Penguins live in Antarctica.", "펭귄은 남극에 산다.", "SCIENCE", "ELEMENTARY"),
        w("parrot", "앵무새", "noun", "/ˈpærət/", "Parrots mimic human speech.", "앵무새는 사람 말을 따라한다.", "SCIENCE", "ELEMENTARY"),
        w("squirrel", "다람쥐", "noun", "/ˈskwɜːrəl/", "The squirrel collected acorns.", "다람쥐가 도토리를 모았다.", "SCIENCE", "ELEMENTARY"),
        w("deer", "사슴", "noun", "/dɪər/", "A deer ran across the road.", "사슴이 도로를 건넜다.", "SCIENCE", "ELEMENTARY"),
        w("fox", "여우", "noun", "/fɒks/", "The fox is clever.", "여우는 영리하다.", "SCIENCE", "ELEMENTARY"),
        w("wolf", "늑대", "noun", "/wʊlf/", "Wolves hunt in packs.", "늑대는 무리로 사냥한다.", "SCIENCE", "ELEMENTARY"),
        w("shark", "상어", "noun", "/ʃɑːrk/", "Sharks are feared predators.", "상어는 두려운 포식자이다.", "SCIENCE", "ELEMENTARY"),
        w("turtle", "거북", "noun", "/ˈtɜːrtəl/", "Sea turtles lay eggs on beaches.", "바다거북은 해변에 알을 낳는다.", "SCIENCE", "ELEMENTARY"),
        w("butterfly", "나비", "noun", "/ˈbʌtərflaɪ/", "Butterflies love colorful flowers.", "나비는 화려한 꽃을 좋아한다.", "SCIENCE", "ELEMENTARY"),
        w("insect", "곤충", "noun", "/ˈɪnsekt/", "Insects have six legs.", "곤충은 다리가 6개이다.", "SCIENCE", "ELEMENTARY"),
        w("spider", "거미", "noun", "/ˈspaɪdər/", "Spiders spin webs.", "거미는 거미줄을 친다.", "SCIENCE", "ELEMENTARY"),
        w("bee", "벌", "noun", "/biː/", "Bees make honey.", "벌은 꿀을 만든다.", "SCIENCE", "ELEMENTARY"),
        w("ant", "개미", "noun", "/ænt/", "Ants work together.", "개미는 함께 일한다.", "SCIENCE", "ELEMENTARY"),
        w("frog", "개구리", "noun", "/frɒɡ/", "Frogs live near water.", "개구리는 물 근처에 산다.", "SCIENCE", "ELEMENTARY"),
        w("snake", "뱀", "noun", "/sneɪk/", "Some snakes are poisonous.", "일부 뱀은 독이 있다.", "SCIENCE", "ELEMENTARY"),
        w("octopus", "문어", "noun", "/ˈɒktəpəs/", "An octopus has eight arms.", "문어는 팔이 8개이다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("giraffe", "기린", "noun", "/dʒɪˈrɑːf/", "Giraffes are the tallest animals.", "기린은 가장 키가 크다.", "SCIENCE", "ELEMENTARY"),
        w("camel", "낙타", "noun", "/ˈkæməl/", "Camels survive without water.", "낙타는 물 없이 생존한다.", "SCIENCE", "ELEMENTARY"),
        w("owl", "올빼미", "noun", "/aʊl/", "Owls are active at night.", "올빼미는 밤에 활동한다.", "SCIENCE", "ELEMENTARY"),
        w("pigeon", "비둘기", "noun", "/ˈpɪdʒɪn/", "Pigeons are found in cities.", "비둘기는 도시에 있다.", "SCIENCE", "ELEMENTARY"),
        w("crow", "까마귀", "noun", "/kroʊ/", "Crows are intelligent birds.", "까마귀는 지능이 높은 새이다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("leopard", "표범", "noun", "/ˈlepərd/", "Leopards are excellent climbers.", "표범은 뛰어난 등반가이다.", "SCIENCE", "MIDDLE_SCHOOL"),
    ])

    # 07: 직업
    save("07_jobs.json", [
        w("surgeon", "외과의사", "noun", "/ˈsɜːrdʒən/", "The surgeon performed the operation.", "외과의사가 수술을 시행했다.", "MEDICINE", "HIGH_SCHOOL"),
        w("pharmacist", "약사", "noun", "/ˈfɑːrməsɪst/", "The pharmacist dispensed medicine.", "약사가 약을 조제했다.", "MEDICINE", "HIGH_SCHOOL"),
        w("therapist", "치료사", "noun", "/ˈθerəpɪst/", "She sees a therapist weekly.", "매주 치료사를 만난다.", "MEDICINE", "HIGH_SCHOOL"),
        w("attorney", "변호사", "noun", "/əˈtɜːrni/", "The attorney represented the client.", "변호사가 의뢰인을 대리했다.", "LAW", "HIGH_SCHOOL"),
        w("journalist", "기자", "noun", "/ˈdʒɜːrnəlɪst/", "The journalist reported from the scene.", "기자가 현장에서 보도했다.", "GENERAL", "HIGH_SCHOOL"),
        w("accountant", "회계사", "noun", "/əˈkaʊntənt/", "The accountant prepared tax returns.", "회계사가 세금 신고서를 작성했다.", "BUSINESS", "HIGH_SCHOOL"),
        w("engineer", "엔지니어", "noun", "/ˌendʒɪˈnɪər/", "The engineer designed the bridge.", "엔지니어가 다리를 설계했다.", "TECHNOLOGY", "MIDDLE_SCHOOL"),
        w("electrician", "전기 기사", "noun", "/ɪˌlekˈtrɪʃən/", "The electrician fixed the wiring.", "전기 기사가 배선을 수리했다.", "TECHNOLOGY", "HIGH_SCHOOL"),
        w("plumber", "배관공", "noun", "/ˈplʌmər/", "Call a plumber to fix the leak.", "누수 수리를 위해 배관공을 부르세요.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("carpenter", "목수", "noun", "/ˈkɑːrpəntər/", "The carpenter built a cabinet.", "목수가 캐비넷을 만들었다.", "GENERAL", "HIGH_SCHOOL"),
        w("mechanic", "정비사", "noun", "/mɪˈkænɪk/", "The mechanic repaired the engine.", "정비사가 엔진을 수리했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("astronaut", "우주비행사", "noun", "/ˈæstrənɔːt/", "The astronaut floated in zero gravity.", "우주비행사가 무중력에서 떠다녔다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("veterinarian", "수의사", "noun", "/ˌvetərɪˈnɛəriən/", "The vet treated the sick dog.", "수의사가 아픈 개를 치료했다.", "SCIENCE", "HIGH_SCHOOL"),
        w("curator", "큐레이터", "noun", "/kjʊˈreɪtər/", "The curator organized the exhibition.", "큐레이터가 전시를 기획했다.", "ARTS", "HIGH_SCHOOL"),
        w("executive", "경영자", "noun", "/ɪɡˈzekjʊtɪv/", "The executive made the decision.", "경영진이 결정을 내렸다.", "BUSINESS", "HIGH_SCHOOL"),
        w("receptionist", "접수원", "noun", "/rɪˈsepʃənɪst/", "The receptionist answered the phone.", "접수원이 전화를 받았다.", "BUSINESS", "HIGH_SCHOOL"),
        w("paramedic", "응급 구조사", "noun", "/ˌpærəˈmedɪk/", "Paramedics arrived quickly.", "응급 구조사들이 빠르게 도착했다.", "MEDICINE", "HIGH_SCHOOL"),
        w("firefighter", "소방관", "noun", "/ˈfaɪərfaɪtər/", "Firefighters rescued people.", "소방관들이 사람들을 구출했다.", "DAILY_LIFE", "ELEMENTARY"),
        w("pilot", "조종사", "noun", "/ˈpaɪlət/", "The pilot landed safely.", "조종사가 안전하게 착륙했다.", "TRAVEL", "ELEMENTARY"),
        w("chef", "주방장", "noun", "/ʃef/", "The chef prepared a gourmet meal.", "주방장이 미식 요리를 준비했다.", "FOOD", "MIDDLE_SCHOOL"),
        w("librarian", "사서", "noun", "/laɪˈbrɛəriən/", "The librarian helped me.", "사서가 도와줬다.", "EDUCATION", "MIDDLE_SCHOOL"),
        w("translator", "번역가", "noun", "/trænsˈleɪtər/", "The translator converted the text.", "번역가가 텍스트를 번역했다.", "EDUCATION", "HIGH_SCHOOL"),
        w("broker", "중개인", "noun", "/ˈbroʊkər/", "The broker handled trades.", "중개인이 거래를 처리했다.", "BUSINESS", "HIGH_SCHOOL"),
        w("detective", "형사", "noun", "/dɪˈtektɪv/", "The detective solved the case.", "형사가 사건을 해결했다.", "LAW", "MIDDLE_SCHOOL"),
        w("cashier", "계산원", "noun", "/kæˈʃɪər/", "The cashier scanned the items.", "계산원이 물건을 스캔했다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
    ])

    # 08: 감정/성격
    save("08_emotions.json", [
        w("anxious", "불안한", "adjective", "/ˈæŋkʃəs/", "She felt anxious.", "그녀는 불안했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("cheerful", "쾌활한", "adjective", "/ˈtʃɪərfəl/", "She has a cheerful personality.", "쾌활한 성격이다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("confident", "자신감 있는", "adjective", "/ˈkɒnfɪdənt/", "He is confident.", "그는 자신감이 있다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("courageous", "용감한", "adjective", "/kəˈreɪdʒəs/", "The courageous firefighter saved lives.", "용감한 소방관이 생명을 구했다.", "GENERAL", "HIGH_SCHOOL"),
        w("curious", "호기심 많은", "adjective", "/ˈkjʊəriəs/", "Children are naturally curious.", "아이들은 호기심이 많다.", "GENERAL", "ELEMENTARY"),
        w("depressed", "우울한", "adjective", "/dɪˈprest/", "He felt depressed.", "그는 우울했다.", "MEDICINE", "HIGH_SCHOOL"),
        w("desperate", "절박한", "adjective", "/ˈdespərɪt/", "She was desperate for help.", "그녀는 절박하게 도움을 구했다.", "GENERAL", "HIGH_SCHOOL"),
        w("embarrassed", "당황한", "adjective", "/ɪmˈbærəst/", "She was embarrassed.", "그녀는 당황했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("enthusiastic", "열정적인", "adjective", "/ɪnˌθjuːziˈæstɪk/", "The team was enthusiastic.", "팀은 열정적이었다.", "GENERAL", "HIGH_SCHOOL"),
        w("exhausted", "극도로 피곤한", "adjective", "/ɪɡˈzɔːstɪd/", "I was exhausted.", "극도로 피곤했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("frustrated", "좌절한", "adjective", "/frʌˈstreɪtɪd/", "He felt frustrated.", "좌절했다.", "GENERAL", "HIGH_SCHOOL"),
        w("generous", "관대한", "adjective", "/ˈdʒenərəs/", "She is generous.", "그녀는 관대하다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("grateful", "감사하는", "adjective", "/ˈɡreɪtfəl/", "I'm grateful for your help.", "도움에 감사합니다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("guilty", "죄의식이 드는", "adjective", "/ˈɡɪlti/", "He felt guilty.", "죄의식을 느꼈다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("humble", "겸손한", "adjective", "/ˈhʌmbəl/", "She remained humble.", "그녀는 겸손했다.", "GENERAL", "HIGH_SCHOOL"),
        w("jealous", "질투하는", "adjective", "/ˈdʒeləs/", "He was jealous.", "그는 질투했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("miserable", "비참한", "adjective", "/ˈmɪzrəbəl/", "The weather was miserable.", "날씨가 비참했다.", "GENERAL", "HIGH_SCHOOL"),
        w("optimistic", "낙관적인", "adjective", "/ˌɒptɪˈmɪstɪk/", "She is optimistic.", "그녀는 낙관적이다.", "GENERAL", "HIGH_SCHOOL"),
        w("pessimistic", "비관적인", "adjective", "/ˌpesɪˈmɪstɪk/", "He is pessimistic.", "그는 비관적이다.", "GENERAL", "HIGH_SCHOOL"),
        w("passionate", "열정적인", "adjective", "/ˈpæʃənɪt/", "She is passionate about music.", "음악에 열정적이다.", "GENERAL", "HIGH_SCHOOL"),
        w("relieved", "안도하는", "adjective", "/rɪˈliːvd/", "She was relieved.", "안도했다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("sincere", "진실된", "adjective", "/sɪnˈsɪər/", "She offered a sincere apology.", "진심 어린 사과를 했다.", "GENERAL", "HIGH_SCHOOL"),
        w("stubborn", "완고한", "adjective", "/ˈstʌbərn/", "He is stubborn.", "그는 완고하다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("suspicious", "의심스러운", "adjective", "/səˈspɪʃəs/", "Police were suspicious.", "경찰은 의심했다.", "GENERAL", "HIGH_SCHOOL"),
        w("terrified", "겁에 질린", "adjective", "/ˈterɪfaɪd/", "She was terrified.", "그녀는 겁에 질렸다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("thoughtful", "사려 깊은", "adjective", "/ˈθɔːtfəl/", "It was a thoughtful gift.", "사려 깊은 선물이었다.", "GENERAL", "MIDDLE_SCHOOL"),
        w("tolerant", "관용적인", "adjective", "/ˈtɒlərənt/", "Be tolerant of others.", "다른 사람에게 관용적이세요.", "GENERAL", "HIGH_SCHOOL"),
        w("selfish", "이기적인", "adjective", "/ˈselfɪʃ/", "Don't be selfish.", "이기적이지 마세요.", "GENERAL", "MIDDLE_SCHOOL"),
        w("modest", "겸손한, 적당한", "adjective", "/ˈmɒdɪst/", "He is modest about his achievements.", "그는 성취에 대해 겸손하다.", "GENERAL", "HIGH_SCHOOL"),
        w("ambitious", "야심 있는", "adjective", "/æmˈbɪʃəs/", "She is ambitious.", "그녀는 야심이 있다.", "GENERAL", "HIGH_SCHOOL"),
    ])

    # 09: 의류
    save("09_clothing.json", [
        w("sleeve", "소매", "noun", "/sliːv/", "Roll up your sleeves.", "소매를 걷으세요.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("collar", "깃", "noun", "/ˈkɒlər/", "His shirt has a stiff collar.", "뻣뻣한 깃이 있다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("zipper", "지퍼", "noun", "/ˈzɪpər/", "The zipper is stuck.", "지퍼가 걸렸다.", "DAILY_LIFE", "ELEMENTARY"),
        w("fabric", "직물", "noun", "/ˈfæbrɪk/", "Cotton is a natural fabric.", "면은 천연 직물이다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("garment", "의복", "noun", "/ˈɡɑːrmənt/", "This garment needs dry cleaning.", "이 의복은 드라이클리닝이 필요하다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("accessory", "액세서리", "noun", "/əkˈsesəri/", "She chose matching accessories.", "어울리는 액세서리를 골랐다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("wardrobe", "옷장", "noun", "/ˈwɔːrdroʊb/", "She updated her wardrobe.", "옷장을 업데이트했다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("outfit", "옷 한 벌", "noun", "/ˈaʊtfɪt/", "She wore a stylish outfit.", "세련된 옷을 입었다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("apron", "앞치마", "noun", "/ˈeɪprən/", "Wear an apron while cooking.", "요리할 때 앞치마를 입으세요.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("costume", "의상", "noun", "/ˈkɒstjuːm/", "Children wore Halloween costumes.", "할로윈 의상을 입었다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("sandal", "샌들", "noun", "/ˈsændəl/", "She wore sandals to the beach.", "해변에 샌들을 신었다.", "DAILY_LIFE", "ELEMENTARY"),
        w("sneaker", "운동화", "noun", "/ˈsniːkər/", "He bought new sneakers.", "새 운동화를 샀다.", "DAILY_LIFE", "ELEMENTARY"),
        w("leather", "가죽", "noun", "/ˈleðər/", "The bag is made of leather.", "가죽 가방이다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("silk", "실크", "noun", "/sɪlk/", "The dress was silk.", "실크 드레스였다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("wool", "양모", "noun", "/wʊl/", "Wool keeps you warm.", "양모는 따뜻하다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("cotton", "면", "noun", "/ˈkɒtən/", "Cotton is breathable.", "면은 통기성이 좋다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("denim", "데님", "noun", "/ˈdenɪm/", "Jeans are denim.", "청바지는 데님이다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("linen", "리넨", "noun", "/ˈlɪnɪn/", "Linen is perfect for summer.", "리넨은 여름에 좋다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("satin", "새틴", "noun", "/ˈsætɪn/", "The dress was made of satin.", "드레스는 새틴으로 만들어졌다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("velvet", "벨벳", "noun", "/ˈvelvɪt/", "The curtains were made of velvet.", "커튼은 벨벳으로 만들어졌다.", "DAILY_LIFE", "HIGH_SCHOOL"),
    ])

    # 10: 가정/날씨/기타 보충
    save("10_home_weather.json", [
        w("furniture", "가구", "noun", "/ˈfɜːrnɪtʃər/", "They bought new furniture.", "새 가구를 샀다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("curtain", "커튼", "noun", "/ˈkɜːrtən/", "Close the curtains.", "커튼을 치세요.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("pillow", "베개", "noun", "/ˈpɪloʊ/", "She fluffed the pillows.", "베개를 부풀렸다.", "DAILY_LIFE", "ELEMENTARY"),
        w("blanket", "담요", "noun", "/ˈblæŋkɪt/", "Cover with a blanket.", "담요를 덮으세요.", "DAILY_LIFE", "ELEMENTARY"),
        w("towel", "수건", "noun", "/ˈtaʊəl/", "Hand me a towel.", "수건을 건네주세요.", "DAILY_LIFE", "ELEMENTARY"),
        w("carpet", "카펫", "noun", "/ˈkɑːrpɪt/", "The carpet was cleaned.", "카펫이 청소되었다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("drawer", "서랍", "noun", "/drɔːr/", "Put socks in the drawer.", "양말을 서랍에 넣으세요.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("shelf", "선반", "noun", "/ʃelf/", "Put books on the shelf.", "선반에 책을 놓으세요.", "DAILY_LIFE", "ELEMENTARY"),
        w("faucet", "수도꼭지", "noun", "/ˈfɔːsɪt/", "The faucet is leaking.", "수도꼭지가 새고 있다.", "DAILY_LIFE", "HIGH_SCHOOL"),
        w("ceiling", "천장", "noun", "/ˈsiːlɪŋ/", "The ceiling is white.", "천장은 흰색이다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("basement", "지하실", "noun", "/ˈbeɪsmənt/", "Boxes are in the basement.", "상자가 지하실에 있다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("garage", "차고", "noun", "/ɡəˈrɑːʒ/", "Park in the garage.", "차고에 주차하세요.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("balcony", "발코니", "noun", "/ˈbælkəni/", "They had coffee on the balcony.", "발코니에서 커피를 마셨다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("fence", "울타리", "noun", "/fens/", "They built a fence.", "울타리를 쳤다.", "DAILY_LIFE", "ELEMENTARY"),
        w("chimney", "굴뚝", "noun", "/ˈtʃɪmni/", "Smoke rose from the chimney.", "굴뚝에서 연기가 올라갔다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("blizzard", "눈보라", "noun", "/ˈblɪzərd/", "The blizzard closed roads.", "눈보라가 도로를 폐쇄했다.", "SCIENCE", "HIGH_SCHOOL"),
        w("hail", "우박", "noun", "/heɪl/", "Hail damaged crops.", "우박이 농작물을 손상시켰다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("breeze", "산들바람", "noun", "/briːz/", "A cool breeze blew.", "시원한 산들바람이 불었다.", "DAILY_LIFE", "MIDDLE_SCHOOL"),
        w("humidity", "습도", "noun", "/hjuːˈmɪdɪti/", "Humidity was unbearable.", "습도가 견딜 수 없었다.", "SCIENCE", "HIGH_SCHOOL"),
        w("fog", "안개", "noun", "/fɒɡ/", "Dense fog reduced visibility.", "짙은 안개가 가시성을 줄였다.", "SCIENCE", "ELEMENTARY"),
        w("frost", "서리", "noun", "/frɒst/", "Frost covered the grass.", "서리가 잔디를 덮었다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("thunder", "천둥", "noun", "/ˈθʌndər/", "Thunder rumbled.", "천둥이 울렸다.", "SCIENCE", "ELEMENTARY"),
        w("lightning", "번개", "noun", "/ˈlaɪtnɪŋ/", "Lightning struck the tree.", "번개가 나무를 쳤다.", "SCIENCE", "ELEMENTARY"),
        w("flood", "홍수", "noun", "/flʌd/", "Heavy rain caused flooding.", "폭우가 홍수를 일으켰다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("tornado", "토네이도", "noun", "/tɔːrˈneɪdoʊ/", "The tornado destroyed homes.", "토네이도가 집을 파괴했다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("volcano", "화산", "noun", "/vɒlˈkeɪnoʊ/", "The volcano erupted.", "화산이 폭발했다.", "SCIENCE", "MIDDLE_SCHOOL"),
        w("tide", "조수", "noun", "/taɪd/", "The tide comes in twice daily.", "조수는 매일 두 번 들어온다.", "SCIENCE", "HIGH_SCHOOL"),
        w("horizon", "수평선", "noun", "/həˈraɪzən/", "The sun set below the horizon.", "태양이 수평선 아래로 졌다.", "SCIENCE", "HIGH_SCHOOL"),
        w("rainbow", "무지개", "noun", "/ˈreɪnboʊ/", "A rainbow appeared.", "무지개가 나타났다.", "SCIENCE", "ELEMENTARY"),
        w("earthquake", "지진", "noun", "/ˈɜːrθkweɪk/", "The earthquake was strong.", "지진이 강했다.", "SCIENCE", "MIDDLE_SCHOOL"),
    ])

    print(f"\n총 생성된 JSON 파일: 6개")

if __name__ == "__main__":
    main()
