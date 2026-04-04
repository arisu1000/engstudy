#!/usr/bin/env python3
"""대량 단어 생성 - 기존 DB에 없는 단어들을 효율적으로 추가"""

import sys
import sqlite3
sys.path.insert(0, '.')
from batch_utils import get_connection, get_existing_words, get_next_id

def main():
    conn = get_connection()
    existing = get_existing_words(conn)
    next_id = get_next_id(conn)
    cursor = conn.cursor()

    # (word, pron, meaning, pos, ex_en, ex_ko, domain, age, diff)
    words = []

    # 1. 심리학/사회학
    words.extend([
        ("cognition", "/kɒɡˈnɪʃən/", "인지", "noun", "Cognition declines with aging.", "인지는 노화와 함께 감퇴한다.", "MEDICINE", "COLLEGE", 4),
        ("empathy", "/ˈempəθi/", "공감", "noun", "Empathy is essential for good relationships.", "공감은 좋은 관계에 필수적이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("stimulus", "/ˈstɪmjʊləs/", "자극", "noun", "The brain responds to external stimuli.", "뇌는 외부 자극에 반응한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("trauma", "/ˈtrɔːmə/", "트라우마", "noun", "Childhood trauma can have lasting effects.", "어린 시절의 트라우마는 지속적인 영향을 미칠 수 있다.", "MEDICINE", "HIGH_SCHOOL", 3),
        ("cognitive", "/ˈkɒɡnɪtɪv/", "인지적인", "adjective", "Cognitive development is rapid in early childhood.", "인지 발달은 유아기에 빠르다.", "MEDICINE", "COLLEGE", 4),
        ("subconscious", "/ˌsʌbˈkɒnʃəs/", "잠재의식의", "adjective", "Subconscious fears can influence behavior.", "잠재의식적 두려움은 행동에 영향을 미칠 수 있다.", "MEDICINE", "HIGH_SCHOOL", 4),
        ("temperament", "/ˈtemprəmənt/", "기질", "noun", "Each child has a unique temperament.", "각 아이는 독특한 기질을 가지고 있다.", "MEDICINE", "HIGH_SCHOOL", 4),
        ("conformity", "/kənˈfɔːrmɪti/", "순응", "noun", "Conformity to group norms is common.", "집단 규범에 대한 순응은 흔하다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("altruism", "/ˈæltruɪzəm/", "이타주의", "noun", "True altruism expects nothing in return.", "진정한 이타주의는 대가를 기대하지 않는다.", "GENERAL", "COLLEGE", 5),
        ("narcissism", "/ˈnɑːrsɪsɪzəm/", "자기도취", "noun", "Narcissism involves excessive self-admiration.", "자기도취는 과도한 자기 찬양을 포함한다.", "MEDICINE", "COLLEGE", 5),
        ("phobia", "/ˈfoʊbiə/", "공포증", "noun", "Claustrophobia is a common phobia.", "폐소공포증은 흔한 공포증이다.", "MEDICINE", "HIGH_SCHOOL", 3),
        ("paranoia", "/ˌpærəˈnɔɪə/", "편집증", "noun", "Paranoia can be a symptom of mental illness.", "편집증은 정신 질환의 증상일 수 있다.", "MEDICINE", "HIGH_SCHOOL", 4),
        ("obsession", "/əbˈseʃən/", "강박, 집착", "noun", "His obsession with perfection caused stress.", "완벽에 대한 그의 집착은 스트레스를 야기했다.", "MEDICINE", "HIGH_SCHOOL", 3),
        ("introverted", "/ˈɪntrəvɜːrtɪd/", "내성적인", "adjective", "Introverted people prefer quiet time alone.", "내성적인 사람들은 혼자만의 조용한 시간을 선호한다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("extroverted", "/ˈekstrəvɜːrtɪd/", "외향적인", "adjective", "Extroverted people enjoy social gatherings.", "외향적인 사람들은 사교 모임을 즐긴다.", "GENERAL", "HIGH_SCHOOL", 3),
    ])

    # 2. 자연과학 심화
    words.extend([
        ("photosynthesis", "/ˌfoʊtəˈsɪnθəsɪs/", "광합성", "noun", "Plants use photosynthesis to convert sunlight into energy.", "식물은 광합성을 사용하여 햇빛을 에너지로 변환한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("chromosome", "/ˈkroʊməsoʊm/", "염색체", "noun", "Humans have 23 pairs of chromosomes.", "인간은 23쌍의 염색체를 가지고 있다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("nucleus", "/ˈnjuːkliəs/", "핵", "noun", "The nucleus contains the cell's genetic material.", "핵은 세포의 유전 물질을 포함한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("enzyme", "/ˈenzaɪm/", "효소", "noun", "Enzymes speed up chemical reactions.", "효소는 화학 반응을 촉진한다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("bacteria", "/bækˈtɪəriə/", "박테리아", "noun", "Bacteria can be both helpful and harmful.", "박테리아는 유용할 수도 해로울 수도 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("cell", "/sel/", "세포", "noun", "The human body is made of trillions of cells.", "인체는 수조 개의 세포로 이루어져 있다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("gene", "/dʒiːn/", "유전자", "noun", "Genes determine many physical traits.", "유전자는 많은 신체적 특성을 결정한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("mutation", "/mjuːˈteɪʃən/", "돌연변이", "noun", "Genetic mutations can cause diseases.", "유전자 돌연변이는 질병을 유발할 수 있다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("gravity", "/ˈɡrævɪti/", "중력", "noun", "Gravity keeps us on the ground.", "중력은 우리를 땅 위에 있게 한다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("velocity", "/vəˈlɒsɪti/", "속도", "noun", "The velocity of light is constant.", "빛의 속도는 일정하다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("friction", "/ˈfrɪkʃən/", "마찰", "noun", "Friction slows down moving objects.", "마찰은 움직이는 물체를 느리게 한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("density", "/ˈdensɪti/", "밀도", "noun", "Oil has a lower density than water.", "기름은 물보다 밀도가 낮다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("evaporation", "/ɪˌvæpəˈreɪʃən/", "증발", "noun", "Evaporation cools the surface of water.", "증발은 물의 표면을 냉각시킨다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("condensation", "/ˌkɒndenˈseɪʃən/", "응결", "noun", "Condensation forms water droplets on cold surfaces.", "응결은 차가운 표면에 물방울을 형성한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("radiation", "/ˌreɪdiˈeɪʃən/", "방사선", "noun", "Radiation from the sun includes UV rays.", "태양의 복사에는 자외선이 포함된다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("wavelength", "/ˈweɪvleŋθ/", "파장", "noun", "Different colors have different wavelengths.", "다른 색상은 다른 파장을 가지고 있다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("theorem", "/ˈθɪərəm/", "정리", "noun", "Pythagoras's theorem is fundamental in geometry.", "피타고라스의 정리는 기하학에서 기본이다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("equation", "/ɪˈkweɪʒən/", "방정식", "noun", "Solve the equation for x.", "x에 대해 방정식을 풀어라.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("formula", "/ˈfɔːrmjʊlə/", "공식", "noun", "The formula for water is H2O.", "물의 화학식은 H2O이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("compound", "/ˈkɒmpaʊnd/", "화합물", "noun", "Water is a chemical compound.", "물은 화합물이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("element", "/ˈelɪmənt/", "원소", "noun", "Oxygen is a chemical element.", "산소는 화학 원소이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("acid", "/ˈæsɪd/", "산", "noun", "Lemon juice contains citric acid.", "레몬 주스에는 구연산이 포함되어 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("ion", "/ˈaɪɒn/", "이온", "noun", "An ion carries an electric charge.", "이온은 전하를 띤다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("electron", "/ɪˈlektrɒn/", "전자", "noun", "Electrons orbit the nucleus of an atom.", "전자는 원자의 핵을 공전한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("neutron", "/ˈnjuːtrɒn/", "중성자", "noun", "Neutrons have no electric charge.", "중성자는 전하가 없다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("proton", "/ˈproʊtɒn/", "양성자", "noun", "Protons have a positive charge.", "양성자는 양전하를 가지고 있다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("atom", "/ˈætəm/", "원자", "noun", "Everything is made up of atoms.", "모든 것은 원자로 이루어져 있다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("protein", "/ˈproʊtiːn/", "단백질", "noun", "Protein is essential for muscle growth.", "단백질은 근육 성장에 필수적이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("carbohydrate", "/ˌkɑːrboʊˈhaɪdreɪt/", "탄수화물", "noun", "Carbohydrates provide energy.", "탄수화물은 에너지를 제공한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("vitamin", "/ˈvɪtəmɪn/", "비타민", "noun", "Vitamin C boosts the immune system.", "비타민 C는 면역 체계를 강화한다.", "MEDICINE", "MIDDLE_SCHOOL", 2),
        ("mineral", "/ˈmɪnərəl/", "무기질", "noun", "Calcium is an important mineral.", "칼슘은 중요한 무기질이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("parasite", "/ˈpærəsaɪt/", "기생충", "noun", "Parasites live off other organisms.", "기생충은 다른 생물에 기생한다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("predator", "/ˈpredətər/", "포식자", "noun", "Lions are apex predators.", "사자는 최상위 포식자이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("prey", "/preɪ/", "먹이", "noun", "The owl hunted its prey at night.", "올빼미는 밤에 먹이를 사냥했다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("symbiosis", "/ˌsɪmbaɪˈoʊsɪs/", "공생", "noun", "Symbiosis benefits both organisms.", "공생은 두 생물 모두에게 이롭다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("neuron", "/ˈnjʊərɒn/", "뉴런", "noun", "Neurons transmit signals in the brain.", "뉴런은 뇌에서 신호를 전달한다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("hemisphere", "/ˈhemɪsfɪər/", "반구", "noun", "Australia is in the southern hemisphere.", "호주는 남반구에 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("latitude", "/ˈlætɪtjuːd/", "위도", "noun", "The equator is at zero degrees latitude.", "적도는 위도 0도에 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("longitude", "/ˈlɒndʒɪtjuːd/", "경도", "noun", "The prime meridian marks zero longitude.", "본초 자오선은 경도 0도를 나타낸다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("peninsula", "/pɪˈnɪnsjʊlə/", "반도", "noun", "Korea is located on a peninsula.", "한국은 반도에 위치해 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("continent", "/ˈkɒntɪnənt/", "대륙", "noun", "There are seven continents.", "대륙은 7개가 있다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("plateau", "/plæˈtoʊ/", "고원", "noun", "The plateau rises 3,000 meters.", "고원은 해발 3,000미터에 있다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("terrain", "/təˈreɪn/", "지형", "noun", "The rough terrain made hiking difficult.", "거친 지형이 하이킹을 어렵게 했다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("tectonic", "/tekˈtɒnɪk/", "지각의", "adjective", "Tectonic plate movement causes earthquakes.", "지각판 이동이 지진을 야기한다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("extinct", "/ɪkˈstɪŋkt/", "멸종한", "adjective", "Dinosaurs have been extinct for millions of years.", "공룡은 수백만 년 전에 멸종했다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("endangered", "/ɪnˈdeɪndʒərd/", "멸종 위기에 처한", "adjective", "Tigers are an endangered species.", "호랑이는 멸종 위기종이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("conservation", "/ˌkɒnsəˈveɪʃən/", "보존", "noun", "Wildlife conservation efforts are increasing.", "야생동물 보호 노력이 증가하고 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("petroleum", "/pəˈtroʊliəm/", "석유", "noun", "Petroleum is a non-renewable resource.", "석유는 재생 불가능한 자원이다.", "SCIENCE", "HIGH_SCHOOL", 3),
    ])

    # 3. 수학/통계
    words.extend([
        ("algebra", "/ˈældʒɪbrə/", "대수학", "noun", "Algebra uses symbols to represent numbers.", "대수학은 숫자를 나타내기 위해 기호를 사용한다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("geometry", "/dʒiˈɒmɪtri/", "기하학", "noun", "Geometry deals with shapes.", "기하학은 도형을 다룬다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("calculus", "/ˈkælkjʊləs/", "미적분학", "noun", "Calculus is essential for engineering.", "미적분학은 공학에 필수적이다.", "EDUCATION", "COLLEGE", 4),
        ("probability", "/ˌprɒbəˈbɪlɪti/", "확률", "noun", "The probability of winning is low.", "이기는 확률은 낮다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("fraction", "/ˈfrækʃən/", "분수", "noun", "Three-fourths is a fraction.", "3/4는 분수이다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("decimal", "/ˈdesɪməl/", "소수", "noun", "Convert the fraction to a decimal.", "분수를 소수로 변환하세요.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("diameter", "/daɪˈæmɪtər/", "지름", "noun", "The diameter of the circle is 10 cm.", "원의 지름은 10cm이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("circumference", "/sərˈkʌmfərəns/", "원주", "noun", "Calculate the circumference.", "둘레를 계산하세요.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("perpendicular", "/ˌpɜːrpənˈdɪkjʊlər/", "수직의", "adjective", "The two lines are perpendicular.", "두 선은 수직이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("parallel", "/ˈpærəlel/", "평행한", "adjective", "The roads run parallel.", "도로는 평행하다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("symmetry", "/ˈsɪmɪtri/", "대칭", "noun", "The building has perfect symmetry.", "건물은 완벽한 대칭을 가지고 있다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("ratio", "/ˈreɪʃioʊ/", "비율", "noun", "The ratio is 3 to 2.", "비율은 3대 2이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("correlation", "/ˌkɒrəˈleɪʃən/", "상관관계", "noun", "There is a strong correlation.", "강한 상관관계가 있다.", "SCIENCE", "COLLEGE", 4),
        ("logarithm", "/ˈlɒɡərɪðəm/", "로그", "noun", "Logarithms are the inverse of exponents.", "로그는 지수의 역이다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("exponent", "/ɪkˈspoʊnənt/", "지수", "noun", "In 2³, 3 is the exponent.", "2³에서 3이 지수이다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("integer", "/ˈɪntɪdʒər/", "정수", "noun", "Zero is an integer.", "0은 정수이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
    ])

    # 4. 건축/도시
    words.extend([
        ("architect", "/ˈɑːrkɪtekt/", "건축가", "noun", "The architect designed the museum.", "건축가가 박물관을 설계했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("facade", "/fəˈsɑːd/", "건물 정면", "noun", "The facade was renovated.", "정면이 개보수되었다.", "ARTS", "HIGH_SCHOOL", 4),
        ("column", "/ˈkɒləm/", "기둥", "noun", "The temple has marble columns.", "사원에는 대리석 기둥이 있다.", "ARTS", "HIGH_SCHOOL", 3),
        ("dome", "/doʊm/", "돔", "noun", "The dome of the Capitol is iconic.", "국회의사당의 돔은 상징적이다.", "ARTS", "HIGH_SCHOOL", 3),
        ("renovation", "/ˌrenəˈveɪʃən/", "개보수", "noun", "The building is under renovation.", "건물이 개보수 중이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("demolition", "/ˌdeməˈlɪʃən/", "철거", "noun", "Demolition of the old factory began.", "오래된 공장의 철거가 시작되었다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("urban", "/ˈɜːrbən/", "도시의", "adjective", "Urban areas have higher population density.", "도시 지역은 인구 밀도가 높다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("rural", "/ˈrʊərəl/", "시골의", "adjective", "Rural communities face unique challenges.", "시골 지역사회는 독특한 도전에 직면한다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("suburban", "/səˈbɜːrbən/", "교외의", "adjective", "Many families prefer suburban living.", "많은 가정이 교외 생활을 선호한다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("metropolitan", "/ˌmetrəˈpɒlɪtən/", "대도시의", "adjective", "Seoul is a major metropolitan area.", "서울은 주요 대도시 지역이다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("skyscraper", "/ˈskaɪskreɪpər/", "초고층 건물", "noun", "New York is famous for skyscrapers.", "뉴욕은 초고층 건물로 유명하다.", "GENERAL", "MIDDLE_SCHOOL", 2),
        ("pedestrian", "/pəˈdestriən/", "보행자", "noun", "Pedestrians have priority at crosswalks.", "보행자는 횡단보도에서 우선권이 있다.", "DAILY_LIFE", "HIGH_SCHOOL", 3),
        ("intersection", "/ˌɪntərˈsekʃən/", "교차로", "noun", "Turn right at the intersection.", "교차로에서 우회전하세요.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("monument", "/ˈmɒnjʊmənt/", "기념물", "noun", "The monument honors fallen soldiers.", "기념비는 전사한 군인들을 기린다.", "GENERAL", "MIDDLE_SCHOOL", 2),
    ])

    # 5. 음악/공연
    words.extend([
        ("tempo", "/ˈtempoʊ/", "템포", "noun", "The tempo was fast.", "템포가 빨랐다.", "ARTS", "HIGH_SCHOOL", 3),
        ("crescendo", "/krɪˈʃendoʊ/", "크레셴도", "noun", "The music reached a dramatic crescendo.", "음악이 극적인 크레셴도에 도달했다.", "ARTS", "HIGH_SCHOOL", 4),
        ("chord", "/kɔːrd/", "화음", "noun", "She played a minor chord.", "그녀는 단조 화음을 연주했다.", "ARTS", "HIGH_SCHOOL", 3),
        ("lyric", "/ˈlɪrɪk/", "가사", "noun", "The lyrics are meaningful.", "가사는 의미가 있다.", "ARTS", "MIDDLE_SCHOOL", 2),
        ("genre", "/ˈʒɒnrə/", "장르", "noun", "What genre do you prefer?", "어떤 장르를 선호하세요?", "ARTS", "HIGH_SCHOOL", 3),
        ("soprano", "/səˈprænoʊ/", "소프라노", "noun", "The soprano hit the high note.", "소프라노가 높은 음을 냈다.", "ARTS", "HIGH_SCHOOL", 3),
        ("symphony", "/ˈsɪmfəni/", "교향곡", "noun", "Beethoven composed nine symphonies.", "베토벤은 9개의 교향곡을 작곡했다.", "ARTS", "HIGH_SCHOOL", 3),
        ("conductor", "/kənˈdʌktər/", "지휘자", "noun", "The conductor raised his baton.", "지휘자가 지휘봉을 들었다.", "ARTS", "HIGH_SCHOOL", 3),
        ("encore", "/ˈɒŋkɔːr/", "앙코르", "noun", "The audience demanded an encore.", "관객이 앙코르를 요구했다.", "ARTS", "HIGH_SCHOOL", 3),
        ("acoustic", "/əˈkuːstɪk/", "어쿠스틱의", "adjective", "She prefers acoustic guitar.", "그녀는 어쿠스틱 기타를 선호한다.", "ARTS", "HIGH_SCHOOL", 3),
        ("debut", "/deɪˈbjuː/", "데뷔", "noun", "Her acting debut was impressive.", "그녀의 연기 데뷔는 인상적이었다.", "ARTS", "HIGH_SCHOOL", 3),
        ("audition", "/ɔːˈdɪʃən/", "오디션", "noun", "She passed the audition.", "그녀는 오디션에 합격했다.", "ARTS", "HIGH_SCHOOL", 3),
    ])

    # 6. 농업/식품
    words.extend([
        ("harvest", "/ˈhɑːrvɪst/", "수확", "noun", "Harvest season begins in autumn.", "수확 시즌은 가을에 시작된다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("cultivate", "/ˈkʌltɪveɪt/", "재배하다", "verb", "Farmers cultivate rice.", "농부들은 쌀을 재배한다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("irrigation", "/ˌɪrɪˈɡeɪʃən/", "관개", "noun", "Irrigation systems water the crops.", "관개 시스템이 농작물에 물을 준다.", "SCIENCE", "HIGH_SCHOOL", 4),
        ("organic", "/ɔːrˈɡænɪk/", "유기농의", "adjective", "Organic farming avoids synthetic chemicals.", "유기농 농업은 합성 화학 물질을 피한다.", "FOOD", "HIGH_SCHOOL", 3),
        ("pesticide", "/ˈpestɪsaɪd/", "살충제", "noun", "Excessive pesticide use harms the environment.", "과도한 살충제 사용은 환경을 해친다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("fertilizer", "/ˈfɜːrtɪlaɪzər/", "비료", "noun", "Natural fertilizers improve soil.", "천연 비료는 토양을 향상시킨다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("crop", "/krɒp/", "농작물", "noun", "Rice is the main crop.", "쌀은 주요 농작물이다.", "SCIENCE", "MIDDLE_SCHOOL", 2),
        ("livestock", "/ˈlaɪvstɒk/", "가축", "noun", "The farm raises livestock.", "농장은 가축을 기른다.", "SCIENCE", "HIGH_SCHOOL", 3),
        ("poultry", "/ˈpoʊltri/", "가금류", "noun", "Poultry includes chickens.", "가금류에는 닭이 포함된다.", "FOOD", "HIGH_SCHOOL", 3),
        ("grain", "/ɡreɪn/", "곡물", "noun", "Wheat is a common grain.", "밀은 흔한 곡물이다.", "FOOD", "MIDDLE_SCHOOL", 2),
        ("dairy", "/ˈdeəri/", "유제품의", "adjective", "Dairy products include milk.", "유제품에는 우유가 포함된다.", "FOOD", "MIDDLE_SCHOOL", 2),
        ("fermentation", "/ˌfɜːrmenˈteɪʃən/", "발효", "noun", "Fermentation makes yogurt.", "발효로 요거트를 만든다.", "FOOD", "HIGH_SCHOOL", 4),
        ("gluten", "/ˈɡluːtən/", "글루텐", "noun", "Some people are intolerant to gluten.", "일부 사람들은 글루텐에 불내성이 있다.", "FOOD", "HIGH_SCHOOL", 3),
        ("cuisine", "/kwɪˈziːn/", "요리 문화", "noun", "Korean cuisine is famous worldwide.", "한국 요리는 전 세계적으로 유명하다.", "FOOD", "HIGH_SCHOOL", 3),
        ("appetizer", "/ˈæpɪtaɪzər/", "전채", "noun", "The appetizer was a fresh salad.", "전채는 신선한 샐러드였다.", "FOOD", "HIGH_SCHOOL", 3),
        ("beverage", "/ˈbevərɪdʒ/", "음료", "noun", "What beverage would you like?", "어떤 음료를 원하시나요?", "FOOD", "HIGH_SCHOOL", 3),
        ("ingredient", "/ɪnˈɡriːdiənt/", "재료", "noun", "Fresh ingredients make a difference.", "신선한 재료가 차이를 만든다.", "FOOD", "MIDDLE_SCHOOL", 2),
        ("seasoning", "/ˈsiːzənɪŋ/", "양념", "noun", "Add seasoning to taste.", "입맛에 맞게 양념을 추가하세요.", "FOOD", "MIDDLE_SCHOOL", 2),
        ("marinate", "/ˈmærɪneɪt/", "재우다", "verb", "Marinate the chicken overnight.", "치킨을 하룻밤 재우세요.", "FOOD", "HIGH_SCHOOL", 3),
        ("simmer", "/ˈsɪmər/", "약한 불로 끓이다", "verb", "Simmer the sauce for 20 minutes.", "소스를 20분 약한 불로 끓이세요.", "FOOD", "HIGH_SCHOOL", 3),
    ])

    # 7. 스포츠
    words.extend([
        ("championship", "/ˈtʃæmpiənʃɪp/", "선수권 대회", "noun", "The team won the championship.", "팀이 선수권 대회에서 우승했다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("tournament", "/ˈtʊərnəmənt/", "토너먼트", "noun", "The tennis tournament attracted top players.", "테니스 토너먼트는 최고의 선수들을 끌어들였다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("referee", "/ˌrefəˈriː/", "심판", "noun", "The referee made a controversial call.", "심판이 논란이 되는 판정을 내렸다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("spectator", "/ˈspekteɪtər/", "관중", "noun", "Thousands of spectators filled the stadium.", "수천 명의 관중이 경기장을 가득 채웠다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("sprint", "/sprɪnt/", "단거리 달리기", "noun", "She won the 100-meter sprint.", "그녀는 100미터 단거리 경주에서 우승했다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("marathon", "/ˈmærəθɒn/", "마라톤", "noun", "Running a marathon requires months of training.", "마라톤은 몇 달간의 훈련이 필요하다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("stamina", "/ˈstæmɪnə/", "체력", "noun", "Building stamina takes consistent training.", "체력을 키우려면 꾸준한 훈련이 필요하다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("opponent", "/əˈpoʊnənt/", "상대", "noun", "She defeated her opponent.", "그녀는 상대를 이겼다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("dribble", "/ˈdrɪbəl/", "드리블하다", "verb", "She dribbled past defenders.", "그녀는 수비수를 드리블로 지나갔다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("tackle", "/ˈtækəl/", "태클하다", "verb", "The defender tackled the attacker.", "수비수가 공격수를 태클했다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("gymnasium", "/dʒɪmˈneɪziəm/", "체육관", "noun", "Students exercise in the gymnasium.", "학생들은 체육관에서 운동한다.", "SPORTS", "MIDDLE_SCHOOL", 2),
        ("amateur", "/ˈæmətər/", "아마추어", "noun", "He started as an amateur.", "그는 아마추어로 시작했다.", "SPORTS", "HIGH_SCHOOL", 3),
        ("veteran", "/ˈvetərən/", "베테랑", "noun", "The veteran player led the team.", "베테랑 선수가 팀을 이끌었다.", "SPORTS", "HIGH_SCHOOL", 3),
    ])

    # 8. 여행/교통
    words.extend([
        ("itinerary", "/aɪˈtɪnərəri/", "여행 일정", "noun", "Our itinerary includes three cities.", "일정에는 세 도시가 포함된다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("accommodation", "/əˌkɒməˈdeɪʃən/", "숙소", "noun", "Book accommodation in advance.", "숙소를 미리 예약하세요.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("departure", "/dɪˈpɑːrtʃər/", "출발", "noun", "Check the departure time.", "출발 시간을 확인하세요.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("arrival", "/əˈraɪvəl/", "도착", "noun", "The arrival time is 3:30 PM.", "도착 시간은 오후 3시 30분이다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("customs", "/ˈkʌstəmz/", "세관", "noun", "We went through customs.", "우리는 세관을 통과했다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("passport", "/ˈpɑːspɔːrt/", "여권", "noun", "Make sure your passport is valid.", "여권이 유효한지 확인하세요.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("visa", "/ˈviːzə/", "비자", "noun", "You need a visa.", "비자가 필요하다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("luggage", "/ˈlʌɡɪdʒ/", "수하물", "noun", "Don't forget your luggage.", "수하물을 잊지 마세요.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("souvenir", "/ˌsuːvəˈnɪər/", "기념품", "noun", "She bought souvenirs.", "그녀는 기념품을 샀다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("excursion", "/ɪkˈskɜːrʃən/", "소풍", "noun", "We went on an excursion.", "우리는 소풍을 갔다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("commute", "/kəˈmjuːt/", "통근하다", "verb", "He commutes by train.", "그는 기차로 통근한다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("transit", "/ˈtrænzɪt/", "통과", "noun", "The package is in transit.", "택배가 운송 중이다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("terminal", "/ˈtɜːrmɪnəl/", "터미널", "noun", "Meet me at Terminal 3.", "터미널 3에서 만나요.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("cruise", "/kruːz/", "유람선 여행", "noun", "They went on a cruise.", "그들은 유람선 여행을 갔다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("expedition", "/ˌekspɪˈdɪʃən/", "탐험", "noun", "The expedition reached the pole.", "탐험대는 극지에 도달했다.", "TRAVEL", "HIGH_SCHOOL", 4),
        ("navigation", "/ˌnævɪˈɡeɪʃən/", "내비게이션", "noun", "GPS navigation makes traveling easier.", "GPS 내비게이션은 여행을 쉽게 만든다.", "TRAVEL", "HIGH_SCHOOL", 3),
        ("reservation", "/ˌrezərˈveɪʃən/", "예약", "noun", "I made a reservation for dinner.", "저녁 식사를 예약했다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("boarding", "/ˈbɔːrdɪŋ/", "탑승", "noun", "Boarding begins in 30 minutes.", "탑승이 30분 후 시작됩니다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
        ("turbulence", "/ˈtɜːrbjʊləns/", "난기류", "noun", "The plane hit turbulence.", "비행기가 난기류를 만났다.", "TRAVEL", "HIGH_SCHOOL", 4),
        ("landmark", "/ˈlændmɑːrk/", "랜드마크", "noun", "The Eiffel Tower is a famous landmark.", "에펠탑은 유명한 랜드마크이다.", "TRAVEL", "MIDDLE_SCHOOL", 2),
    ])

    # 9. 경제/금융
    words.extend([
        ("inflation", "/ɪnˈfleɪʃən/", "인플레이션", "noun", "Inflation erodes purchasing power.", "인플레이션은 구매력을 약화시킨다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("mortgage", "/ˈmɔːrɡɪdʒ/", "주택 담보 대출", "noun", "They took out a mortgage.", "그들은 담보 대출을 받았다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("currency", "/ˈkɜːrənsi/", "통화", "noun", "The local currency is the won.", "현지 통화는 원이다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("tariff", "/ˈtærɪf/", "관세", "noun", "New tariffs were imposed.", "새로운 관세가 부과되었다.", "BUSINESS", "HIGH_SCHOOL", 4),
        ("stock", "/stɒk/", "주식", "noun", "He invested in stocks.", "그는 주식에 투자했다.", "BUSINESS", "HIGH_SCHOOL", 3),
        ("bond", "/bɒnd/", "채권", "noun", "Government bonds are safe.", "국채는 안전하다.", "BUSINESS", "HIGH_SCHOOL", 4),
        ("bankruptcy", "/ˈbæŋkrʌptsi/", "파산", "noun", "The company filed for bankruptcy.", "회사가 파산 신청을 했다.", "BUSINESS", "HIGH_SCHOOL", 4),
        ("monopoly", "/məˈnɒpəli/", "독점", "noun", "The company held a monopoly.", "회사는 독점을 유지했다.", "BUSINESS", "HIGH_SCHOOL", 4),
        ("capitalism", "/ˈkæpɪtəlɪzəm/", "자본주의", "noun", "Capitalism is based on private ownership.", "자본주의는 사적 소유에 기반한다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("socialism", "/ˈsoʊʃəlɪzəm/", "사회주의", "noun", "Socialism emphasizes collective ownership.", "사회주의는 공동 소유를 강조한다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("collateral", "/kəˈlætərəl/", "담보물", "noun", "The house was used as collateral.", "집이 담보물로 사용되었다.", "BUSINESS", "COLLEGE", 5),
        ("expenditure", "/ɪkˈspendɪtʃər/", "지출", "noun", "Government expenditure increased.", "정부 지출이 증가했다.", "BUSINESS", "HIGH_SCHOOL", 4),
    ])

    # 10. 문학/언어
    words.extend([
        ("metaphor", "/ˈmetəfɔːr/", "은유", "noun", "'Life is a journey' is a metaphor.", "'인생은 여정이다'는 은유이다.", "ARTS", "HIGH_SCHOOL", 3),
        ("simile", "/ˈsɪmɪli/", "직유", "noun", "'Brave as a lion' is a simile.", "'사자처럼 용감하다'는 직유이다.", "ARTS", "HIGH_SCHOOL", 3),
        ("irony", "/ˈaɪrəni/", "아이러니", "noun", "The irony was that the fire station burned.", "소방서가 불탔다는 것은 아이러니였다.", "ARTS", "HIGH_SCHOOL", 3),
        ("satire", "/ˈsætaɪər/", "풍자", "noun", "The novel is a political satire.", "그 소설은 정치적 풍자이다.", "ARTS", "HIGH_SCHOOL", 4),
        ("protagonist", "/proʊˈtæɡənɪst/", "주인공", "noun", "The protagonist faces challenges.", "주인공은 도전에 직면한다.", "ARTS", "HIGH_SCHOOL", 3),
        ("antagonist", "/ænˈtæɡənɪst/", "적대자", "noun", "The antagonist is complex.", "적대자는 복잡한 캐릭터이다.", "ARTS", "HIGH_SCHOOL", 3),
        ("plot", "/plɒt/", "줄거리", "noun", "The plot was unpredictable.", "줄거리는 예측할 수 없었다.", "ARTS", "MIDDLE_SCHOOL", 2),
        ("fiction", "/ˈfɪkʃən/", "소설", "noun", "She prefers fiction.", "그녀는 소설을 선호한다.", "ARTS", "MIDDLE_SCHOOL", 2),
        ("autobiography", "/ˌɔːtəbaɪˈɒɡrəfi/", "자서전", "noun", "He wrote an autobiography.", "그는 자서전을 썼다.", "ARTS", "HIGH_SCHOOL", 3),
        ("biography", "/baɪˈɒɡrəfi/", "전기", "noun", "The biography of Einstein was fascinating.", "아인슈타인의 전기는 매력적이었다.", "ARTS", "HIGH_SCHOOL", 3),
        ("paragraph", "/ˈpærəɡrɑːf/", "단락", "noun", "Begin each paragraph with a topic sentence.", "각 단락은 주제문으로 시작하세요.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("syllable", "/ˈsɪləbəl/", "음절", "noun", "'Computer' has three syllables.", "'computer'는 세 음절이 있다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("vowel", "/ˈvaʊəl/", "모음", "noun", "English has five vowel letters.", "영어에는 5개의 모음이 있다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("consonant", "/ˈkɒnsənənt/", "자음", "noun", "B, C, D are consonants.", "B, C, D는 자음이다.", "EDUCATION", "MIDDLE_SCHOOL", 2),
        ("dialect", "/ˈdaɪəlekt/", "방언", "noun", "Regional dialects vary.", "지역 방언은 다양하다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("bilingual", "/baɪˈlɪŋɡwəl/", "이중 언어의", "adjective", "Bilingual children switch languages easily.", "이중 언어 어린이들은 쉽게 언어를 전환한다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("fluent", "/ˈfluːənt/", "유창한", "adjective", "She is fluent in English.", "그녀는 영어에 유창하다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("proverb", "/ˈprɒvɜːrb/", "속담", "noun", "'Actions speak louder than words' is a proverb.", "'행동은 말보다 크다'는 속담이다.", "EDUCATION", "HIGH_SCHOOL", 3),
        ("rhetoric", "/ˈretərɪk/", "수사학", "noun", "Political rhetoric can be persuasive.", "정치적 수사학은 설득력이 있다.", "EDUCATION", "COLLEGE", 4),
    ])

    # 11. 역사/정치
    words.extend([
        ("dynasty", "/ˈdaɪnəsti/", "왕조", "noun", "The Ming Dynasty ruled for centuries.", "명나라는 수세기 동안 통치했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("empire", "/ˈempaɪər/", "제국", "noun", "The Roman Empire was vast.", "로마 제국은 광대했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("medieval", "/ˌmediˈiːvəl/", "중세의", "adjective", "Medieval castles dot Europe.", "중세 성들이 유럽에 있다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("renaissance", "/ˈrenəsɑːns/", "르네상스", "noun", "The Renaissance was a cultural revolution.", "르네상스는 문화적 혁명이었다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("revolt", "/rɪˈvoʊlt/", "반란", "noun", "The peasants staged a revolt.", "농민들이 반란을 일으켰다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("treaty", "/ˈtriːti/", "조약", "noun", "They signed a peace treaty.", "평화 조약에 서명했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("conquest", "/ˈkɒŋkwest/", "정복", "noun", "The conquest expanded the empire.", "정복이 제국을 확장했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("alliance", "/əˈlaɪəns/", "동맹", "noun", "Military alliances protect nations.", "군사 동맹은 국가를 보호한다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("tyrant", "/ˈtaɪrənt/", "폭군", "noun", "The tyrant ruled with an iron fist.", "폭군은 철권으로 통치했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("monarchy", "/ˈmɒnərki/", "군주제", "noun", "The country has a constitutional monarchy.", "그 나라는 입헌 군주제이다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("republic", "/rɪˈpʌblɪk/", "공화국", "noun", "France is a republic.", "프랑스는 공화국이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("aristocracy", "/ˌærɪˈstɒkrəsi/", "귀족 계급", "noun", "The aristocracy held most wealth.", "귀족 계급이 대부분의 부를 소유했다.", "GENERAL", "COLLEGE", 4),
        ("patriot", "/ˈpeɪtriət/", "애국자", "noun", "Patriots fought for freedom.", "애국자들은 자유를 위해 싸웠다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("dictator", "/ˈdɪkteɪtər/", "독재자", "noun", "The dictator suppressed opposition.", "독재자는 반대를 탄압했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("regime", "/reɪˈʒiːm/", "정권", "noun", "The regime was overthrown.", "정권이 전복되었다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("communism", "/ˈkɒmjʊnɪzəm/", "공산주의", "noun", "Communism advocates collective ownership.", "공산주의는 공동 소유를 옹호한다.", "GENERAL", "HIGH_SCHOOL", 4),
    ])

    # 12. 철학/종교/심리
    words.extend([
        ("morality", "/məˈrælɪti/", "도덕성", "noun", "Questions of morality are complex.", "도덕성 질문은 복잡하다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("virtue", "/ˈvɜːrtʃuː/", "미덕", "noun", "Patience is a virtue.", "인내는 미덕이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("conscience", "/ˈkɒnʃəns/", "양심", "noun", "His conscience told him it was wrong.", "양심이 잘못되었다고 말했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("existence", "/ɪɡˈzɪstəns/", "존재", "noun", "The existence of aliens is debated.", "외계인의 존재는 논란이 된다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("paradox", "/ˈpærədɒks/", "역설", "noun", "Less is sometimes more is a paradox.", "적은 것이 때로 더 많다는 것은 역설이다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("dilemma", "/dɪˈlemə/", "딜레마", "noun", "She faced a moral dilemma.", "그녀는 도덕적 딜레마에 직면했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("utopia", "/juːˈtoʊpiə/", "유토피아", "noun", "A perfect utopia may be impossible.", "완벽한 유토피아는 불가능할 수 있다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("dystopia", "/dɪsˈtoʊpiə/", "디스토피아", "noun", "The novel depicts a dystopia.", "소설은 디스토피아를 묘사한다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("secular", "/ˈsekjʊlər/", "세속적인", "adjective", "A secular society separates religion from government.", "세속적 사회는 종교와 정부를 분리한다.", "GENERAL", "HIGH_SCHOOL", 4),
        ("sacred", "/ˈseɪkrɪd/", "신성한", "adjective", "The temple is a sacred place.", "사원은 신성한 장소이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("ritual", "/ˈrɪtʃuəl/", "의식", "noun", "Morning coffee is a daily ritual.", "아침 커피는 일상 의식이다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("meditation", "/ˌmedɪˈteɪʃən/", "명상", "noun", "Meditation reduces stress.", "명상은 스트레스를 줄인다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("destiny", "/ˈdestɪni/", "운명", "noun", "She believed it was her destiny.", "그녀는 그것이 운명이라 믿었다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("fate", "/feɪt/", "운명", "noun", "Fate brought them together.", "운명이 그들을 만나게 했다.", "GENERAL", "HIGH_SCHOOL", 3),
        ("wisdom", "/ˈwɪzdəm/", "지혜", "noun", "Wisdom comes with experience.", "지혜는 경험과 함께 온다.", "GENERAL", "HIGH_SCHOOL", 3),
    ])

    # 중복 체크 후 삽입
    added = 0
    for w in words:
        word_lower = w[0].lower()
        if word_lower not in existing:
            cursor.execute("""
                INSERT INTO words (id, word, pronunciation, meaning_ko, part_of_speech,
                    example_en, example_ko, domain, age_group, frequency_rank, difficulty,
                    synonyms, antonyms, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL)
            """, (next_id, w[0], w[1], w[2], w[3], w[4], w[5], w[6], w[7], next_id, w[8]))
            existing.add(word_lower)
            next_id += 1
            added += 1

    conn.commit()
    cursor.execute("SELECT COUNT(*) FROM words")
    total = cursor.fetchone()[0]
    conn.close()

    print(f"gen_bulk: {added}개 단어 추가")
    print(f"총 단어 수: {total}")

if __name__ == "__main__":
    main()
