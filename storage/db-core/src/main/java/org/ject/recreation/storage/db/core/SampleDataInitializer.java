package org.ject.recreation.storage.db.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final GameRepository gameRepository;

    private final QuestionRepository questionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이모지 게임이 이미 존재하는지 확인
        Optional<GameEntity> emojiGame = gameRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        
        // 줄줄이 말해요 게임이 이미 존재하는지 확인
        Optional<GameEntity> wordChainGame = gameRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));
        
        // 인물퀴즈 게임이 이미 존재하는지 확인
        Optional<GameEntity> personGame = gameRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"));
        
        // 명대사 퀴즈 게임이 이미 존재하는지 확인
        Optional<GameEntity> famousQuoteGame = gameRepository.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440003"));

        // admin 이미 존재하는지 확인
        Optional<UserEntity> admin = userRepository.findById("jectreation518@gmail.com");

        // 모든 게임이 이미 존재하면 샘플 데이터 생성을 건너뜀
        if(emojiGame.isPresent()
                && wordChainGame.isPresent()
                && personGame.isPresent()
                && famousQuoteGame.isPresent()
                && admin.isPresent()) {
            return;
        }

        // 사용자 샘플 데이터 생성
        UserEntity user = createSampleUsers();
        userRepository.save(user);

        // 게임 샘플 데이터 생성
        List<GameEntity> games = createSampleGames(user);
        gameRepository.saveAll(games);

        // 질문 샘플 데이터 생성
        List<QuestionEntity> questions = createSampleQuestions(games);
        questionRepository.saveAll(questions);
    }

    // TODO : 닉네임, profileImage 수정
    private UserEntity createSampleUsers() {
        return UserEntity.builder()
                .email("jectreation518@gmail.com")
                .platform("kakao")
                .profileImageUrl("https://example.com/profile1.jpg")
                .nickname("게임마스터")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private List<GameEntity> createSampleGames(UserEntity user) {
        List<GameEntity> games = new ArrayList<>();

        // 이모지 보고 속담 맞추기 (25문제)
        GameEntity emojiProverbQuiz = GameEntity.builder()
                .gameId(UUID.fromString("16f89e38-b86f-4cb3-b53d-8f0a6a78b8a9"))
                .gameCreator(user)
                .gameTitle("이모지 보고 속담 맞추기")
                .gameThumbnailUrl("games/16f89e38-b86f-4cb3-b53d-8f0a6a78b8a9/00_썸네일이미지.png")
                .isShared(true)
                .isDeleted(false)
                .questionCount(25)
                .playCount(0)
                .version(1)
                .build();
        games.add(emojiProverbQuiz);

        // 줄줄이 말해요 (20문제)
        GameEntity wordChainQuiz = GameEntity.builder()
                .gameId(UUID.fromString("d108c47d-df0d-4421-a5f0-513fa3d40b47"))
                .gameCreator(user)
                .gameTitle("줄줄이 말해요")
                .gameThumbnailUrl("games/d108c47d-df0d-4421-a5f0-513fa3d40b47/00_썸네일이미지.png")
                .isShared(true)
                .isDeleted(false)
                .questionCount(20)
                .playCount(0)
                .version(1)
                .build();
        games.add(wordChainQuiz);

        // 인물퀴즈 (50문제)
        GameEntity personQuiz = GameEntity.builder()
                .gameId(UUID.fromString("25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f"))
                .gameCreator(user)
                .gameTitle("인물퀴즈")
                .gameThumbnailUrl("games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/00_썸네일이미지.png")
                .isShared(true)
                .isDeleted(false)
                .questionCount(50)
                .playCount(0)
                .version(1)
                .build();
        games.add(personQuiz);

        // 명대사 퀴즈 (30문제)
        GameEntity famousQuoteQuiz = GameEntity.builder()
                .gameId(UUID.fromString("7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2"))
                .gameCreator(user)
                .gameTitle("명대사 퀴즈")
                .gameThumbnailUrl("games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/00_썸네일이미지.png")
                .isShared(true)
                .isDeleted(false)
                .questionCount(30)
                .playCount(0)
                .version(1)
                .build();
        games.add(famousQuoteQuiz);

        return games;
    }

    private List<QuestionEntity> createSampleQuestions(List<GameEntity> games) {
        List<QuestionEntity> questions = new ArrayList<>();

        // 이모지 보고 속담 맞추기 질문들 (25문제)
        GameEntity emojiProverbQuiz = games.get(0);
        String[][] emojiProverbData = {
            {"🫛🫛🫘🫘", "콩 심은 데 콩 나고 팥 심은 데 팥 난다"},
            {"🧎‍➡️📄🧎👍", "백지장도 맞들면 낫다"},
            {"🪨🌉✊👣", "돌다리도 두들겨 보고 건너라"},
            {"🙍🙍🙍🚤⛰️", "사공이 많으면 배가 산으로 간다"},
            {"🌧️☔👕❌", "가랑비에 옷 젖는 줄 모른다"},
            {"🪙🪙🪙➡️💰", "티끌 모아 태산"},
            {"🐋⚔️🦐💥", "고래 싸움에 새우 등 터진다"},
            {"🧍‍♂️➡️🛍️📅", "가는 날이 장날이다"},
            {"🥷🦶⚡⚡", "도둑이 제발 저린다"},
            {"🛋️⬇️🌑", "등잔 밑이 어둡다"},
            {"🐯💬🐅", "호랑이도 제 말하면 온다"},
            {"🌞🌞⚡⚡", "마른 하늘에 날벼락"},
            {"👄👂👂👄", "가는 말이 고와야 오는 말이 곱다"},
            {"👀👍🍡", "보기 좋은 떡이 먹기도 좋다"},
            {"😠↔️🌉😠", "원수는 외나무다리에서 만난다"},
            {"🐵🌳⛔", "원숭이도 나무에서 떨어진다"},
            {"🐶❓🐯👿", "하룻강아지 범 무서운 줄 모른다"},
            {"🛏️🍡😋", "누워서 떡 먹기"},
            {"❄️🍲👄", "식은 죽 먹기"},
            {"🏃⬆️🕊️", "뛰는 놈 위에 나는 놈 있다"},
            {"🌞🐦🌙🐭", "낮말은 새가 듣고 밤말은 쥐가 듣는다"},
            {"🐦🍐💥", "까마귀 날자 배 떨어진다"},
            {"🐂👂📖", "쇠귀에 경 읽기"},
            {"🍉👅👅", "수박 겉 핥기"},
            {"🚫🛢️💧💧💧", "밑 빠진 독에 물 붓기"}
        };

        for (int i = 0; i < emojiProverbData.length; i++) {
            questions.add(QuestionEntity.builder()
                    .questionOrder(i + 1)
                    .questionText(emojiProverbData[i][0])
                    .questionAnswer(emojiProverbData[i][1])
                    .imageUrl("games/16f89e38-b86f-4cb3-b53d-8f0a6a78b8a9/emoji" + (i + 1) + ".jpg")
                    .version(1)
                    .game(emojiProverbQuiz)
                    .build());
        }

        // 줄줄이 말해요 질문들 (20문제)
        GameEntity wordChainQuiz = games.get(1);
        String[][] wordChainData = {
            {"세 글자 나라 이름", "예) 벨기에, 프랑스"},
            {"받침 없는 단어", "예) 우유, 기차"},
            {"초성 ㄱㅅ 단어 말하기", "예) 공사, 감시"},
            {"과자 이름", "예) 웨하스, 홈런볼"},
            {"3글자 이상 동물 이름", "예) 호랑이, 원숭이"},
            {"귀신 이름", "예) 저승사자, 드라큘라"},
            {"'정'으로 끝나는 말", "예) 인정, 확정"},
            {"숫자 들어간 말", "예) 일주일, 삼겹살"},
            {"공으로 하는 스포츠", "예) 야구, 배구"},
            {"'사'로 끝나는 직업", "예) 의사, 변호사"},
            {"수도 이름", "예) 서울, 도쿄"},
            {"'시'로 끝나는 말", "예) 감시, 나시"},
            {"라면 이름", "예) 진라면, 신라면"},
            {"첫글자와 마지막 글자 같은 단어", "예) 기러기, 스위스"},
            {"생선 이름", "예) 도미, 갈치"},
            {"두 글자 이름 연예인", "예) 원빈, 송강"},
            {"'통'으로 끝나는 말", "예) 고통, 치통"},
            {"중국집 메뉴", "예) 짜장면, 유산슬"},
            {"동화 제목", "예) 콩쥐팥쥐, 콩나무"},
            {"숫자가 들어간 단어", "예) 일기장, 삼겹살"}
        };

        for (int i = 0; i < wordChainData.length; i++) {
            questions.add(QuestionEntity.builder()
                    .questionOrder(i + 1)
                    .questionText(wordChainData[i][0])
                    .questionAnswer(wordChainData[i][1])
                    .imageUrl("games/d108c47d-df0d-4421-a5f0-513fa3d40b47/wordchain" + (i + 1) + ".jpg")
                    .version(1)
                    .game(wordChainQuiz)
                    .build());
        }

        // 인물퀴즈 질문들 (50문제)
        GameEntity personQuiz = games.get(2);
        String[][] personData = {
            {"이미지 속 인물의 이름은?", "김지원 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/01_김지원.jpg"},
            {"이미지 속 인물의 이름은?", "코쿤 (음악 프로듀서)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/02_코쿤.jpg"},
            {"이미지 속 인물의 이름은?", "구교환 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/03_구교환.jpg"},
            {"이미지 속 인물의 이름은?", "최예나 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/04_최예나.jpg"},
            {"이미지 속 인물의 이름은?", "이용진 (개그맨)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/05_이용진.jpg"},
            {"이미지 속 인물의 이름은?", "변우석 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/06_변우석.jpg"},
            {"이미지 속 인물의 이름은?", "이무진 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/07_이무진.jpg"},
            {"이미지 속 인물의 이름은?", "고경표 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/08_고경표.jpg"},
            {"이미지 속 인물의 이름은?", "오해원 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/09_오해원.jpg"},
            {"이미지 속 인물의 이름은?", "주우재 (모델/방송인)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/10_주우재.jpg"},
            {"이미지 속 인물의 이름은?", "이영지 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/11_이영지.jpg"},
            {"이미지 속 인물의 이름은?", "손흥민 (축구선수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/12_손흥민.jpg"},
            {"이미지 속 인물의 이름은?", "고윤정 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/13_고윤정.jpg"},
            {"이미지 속 인물의 이름은?", "김성주 (방송인)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/14_김성주.jpg"},
            {"이미지 속 인물의 이름은?", "박은빈 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/15_박은빈.jpg"},
            {"이미지 속 인물의 이름은?", "김연경 (배구선수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/16_김연경.jpg"},
            {"이미지 속 인물의 이름은?", "장범준 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/17_장범준.jpg"},
            {"이미지 속 인물의 이름은?", "최민식 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/18_최민식.jpg"},
            {"이미지 속 인물의 이름은?", "혜인 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/19_혜인.jpg"},
            {"이미지 속 인물의 이름은?", "신하균 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/20_신하균.jpg"},
            {"이미지 속 인물의 이름은?", "황정민 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/21_황정민.jpg"},
            {"이미지 속 인물의 이름은?", "규현 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/22_규현.jpg"},
            {"이미지 속 인물의 이름은?", "미주 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/23_미주.jpg"},
            {"이미지 속 인물의 이름은?", "조정석 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/24_조정석.jpg"},
            {"이미지 속 인물의 이름은?", "박미선 (개그우먼)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/25_박미선.jpg"},
            {"이미지 속 인물의 이름은?", "송강 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/26_송강.jpg"},
            {"이미지 속 인물의 이름은?", "키 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/27_키.jpg"},
            {"이미지 속 인물의 이름은?", "톰 크루즈 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/28_톰크루즈.jpg"},
            {"이미지 속 인물의 이름은?", "웬디 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/29_웬디.jpg"},
            {"이미지 속 인물의 이름은?", "이순신 (역사 인물)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/30_이순신.jpg"},
            {"이미지 속 인물의 이름은?", "김태리 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/31_김태리.jpg"},
            {"이미지 속 인물의 이름은?", "장도연 (개그우먼)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/32_장도연.jpg"},
            {"이미지 속 인물의 이름은?", "임시완 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/33_임시완.jpg"},
            {"이미지 속 인물의 이름은?", "임지연 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/34_임지연.jpg"},
            {"이미지 속 인물의 이름은?", "박재범 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/35_박재범.jpg"},
            {"이미지 속 인물의 이름은?", "안유진 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/36_안유진.jpg"},
            {"이미지 속 인물의 이름은?", "유연석 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/37_유연석.jpg"},
            {"이미지 속 인물의 이름은?", "양세찬 (개그맨)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/38_양세찬.jpg"},
            {"이미지 속 인물의 이름은?", "나문희 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/39_나문희.jpg"},
            {"이미지 속 인물의 이름은?", "윤하 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/40_윤하.jpg"},
            {"이미지 속 인물의 이름은?", "손석구 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/41_손석구.jpg"},
            {"이미지 속 인물의 이름은?", "홍진경 (모델/방송인)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/42_홍진경.jpg"},
            {"이미지 속 인물의 이름은?", "태양 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/43_태양.jpg"},
            {"이미지 속 인물의 이름은?", "이은지 (개그우먼)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/44_이은지.jpg"},
            {"이미지 속 인물의 이름은?", "소유 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/45_소유.jpg"},
            {"이미지 속 인물의 이름은?", "이제훈 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/46_이제훈.jpg"},
            {"이미지 속 인물의 이름은?", "뷔 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/47_뷔.jpg"},
            {"이미지 속 인물의 이름은?", "윈터 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/48_윈터.jpg"},
            {"이미지 속 인물의 이름은?", "카더가든 (가수)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/49_카더가든.jpg"},
            {"이미지 속 인물의 이름은?", "조진웅 (배우)", "games/25cfc8db-4eb1-4f21-8e5b-f4b77189ec2f/50_조진웅.jpg"}
        };

        for (int i = 0; i < personData.length; i++) {
            questions.add(QuestionEntity.builder()
                    .questionOrder(i + 1)
                    .questionText(personData[i][0])
                    .questionAnswer(personData[i][1])
                    .imageUrl(personData[i][2])
                    .version(1)
                    .game(personQuiz)
                    .build());
        }

        // 명대사 퀴즈 질문들 (30문제)
        GameEntity famousQuoteQuiz = games.get(3);
        String[][] famousQuoteData = {
            {"다음 장면의 대사는?", "도깨비: \"사랑해요\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/01_도깨비.jpg"},
            {"다음 장면의 대사는?", "미안하다 사랑한다: \"밥 먹을래 나랑 같이 죽을래!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/02_미안하다사랑한다.jpg"},
            {"다음 장면의 대사는?", "상속자들: \"사탄들의 학교에 루시퍼의 등장이라\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/03_상속자들.jpg"},
            {"다음 장면의 대사는?", "거침없이 하이킥: \"호- 박- 고- 구- 마- 호- 박- 고- 구- 마!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/04_거침없이하이킥.jpg"},
            {"다음 장면의 대사는?", "극한직업: \"지금까지 이런 맛은 없었다. 이것은 갈비인가 통닭인가?\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/05_극한직업.jpg"},
            {"다음 장면의 대사는?", "스카이 캐슬: \"저를 전적으로 믿으셔야 합니다.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/06_스카이캐슬.jpg"},
            {"다음 장면의 대사는?", "타짜: \"묻고 떠블로 가!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/07_타짜.jpg"},
            {"다음 장면의 대사는?", "범죄도시: \"진실의 방으로.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/08_범죄도시.jpg"},
            {"다음 장면의 대사는?", "기생충: \"오 너는 다 계획이 있구나.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/09_기생충.jpg"},
            {"다음 장면의 대사는?", "지붕뚫고하이킥: \"엄청 커다란 모기가 나의 발을 물었어! 간지러웟어!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/10_지붕뚫고하이킥.jpg"},
            {"다음 장면의 대사는?", "파리의 연인: \"애기야 가자\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/11_파리의연인.jpg"},
            {"다음 장면의 대사는?", "응답하라 1988: \"내 신경은 온통 너였어.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/12_응답하라.jpg"},
            {"다음 장면의 대사는?", "천국의 계단: \"사랑은 돌아오는 거야.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/13_천국의계단.jpg"},
            {"다음 장면의 대사는?", "내 남편과 결혼해줘: \"내는 니 좋아했다꼬!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/14_내남편과결혼해줘.jpg"},
            {"다음 장면의 대사는?", "부부의 세계: \"사랑에 빠진 게 죄는 아니잖아!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/15_부부의세계.jpg"},
            {"다음 장면의 대사는?", "시크릿가든: \"길라임씨는 언제부터 그렇게 예뻤나, 작년부터?\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/16_시크릿가든.jpg"},
            {"다음 장면의 대사는?", "이상한 변호사 우영우: \"너는 봄날의 햇살 같아\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/17_우영우.jpg"},
            {"다음 장면의 대사는?", "사랑했나봐: \"예나 선정이 딸이에요\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/18_사랑했나봐.jpg"},
            {"다음 장면의 대사는?", "친절한 금자씨: \"너나 잘하세요.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/19_친절한금자씨.jpg"},
            {"다음 장면의 대사는?", "해를 품은 달: \"잊어달라 하였느냐. 잊어주길 바라느냐. 미안하구나 잊으려 하였으나 너를 잊지 못하였다.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/20_해를품은달.jpg"},
            {"다음 장면의 대사는?", "지붕뚫고 하이킥: \"이 빵꾸똥꾸야!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/21_지붕뚫고하이킥.jpg"},
            {"다음 장면의 대사는?", "대장금: \"어찌 홍시라 생각했느냐 하시면 그냥 홍시맛이나서 홍시라 생각한 것이온데.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/22_대장금.jpg"},
            {"다음 장면의 대사는?", "말아톤: \"초원이 다리는 백만불짜리 다리!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/23_말아톤.jpg"},
            {"다음 장면의 대사는?", "내부자들: \"모히또 가서 몰디브 한 잔 할까?\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/24_내부자들.jpg"},
            {"다음 장면의 대사는?", "야인시대: \"사딸라!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/25_야인시대.jpg"},
            {"다음 장면의 대사는?", "꽃보다 남자: \"시켜줘 그럼. 금잔디 명예 소방관\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/26_꽃보다남자.jpg"},
            {"다음 장면의 대사는?", "빠담빠담: \"사과해요 나한테!\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/27_빠담빠담.jpg"},
            {"다음 장면의 대사는?", "오징어 게임: \"우리는 깐부잖아\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/28_오징어게임.jpg"},
            {"다음 장면의 대사는?", "신데렐라 언니 : \"넌 꿈이 뭐니\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/29_신데렐라언니.jpg"},
            {"다음 장면의 대사는?", "태조 왕건: \"누구인가? 누가 기침소리를 내었어.\"", "games/7f9b3c8e-98b2-4cb1-909e-10a7ed3dc7a2/30_태조왕건.jpg"}
        };

        for (int i = 0; i < famousQuoteData.length; i++) {
            questions.add(QuestionEntity.builder()
                    .questionOrder(i)
                    .questionText(famousQuoteData[i][0])
                    .questionAnswer(famousQuoteData[i][1])
                    .imageUrl(famousQuoteData[i][2])
                    .version(1)
                    .game(famousQuoteQuiz)
                    .build());
        }

        return questions;
    }
} 