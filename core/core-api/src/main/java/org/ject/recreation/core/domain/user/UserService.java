package org.ject.recreation.core.domain.user;

import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.ject.recreation.core.api.controller.response.AuthResponseDto;
import org.ject.recreation.core.api.controller.session.SessionUserInfoDto;
import org.ject.recreation.core.domain.game.Game;
import org.ject.recreation.core.domain.game.GameReader;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorData;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.storage.db.core.UserEntity;
import org.ject.recreation.storage.db.core.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.ject.recreation.core.support.error.ErrorType.GAME_FORBIDDEN;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GameReader gameReader;
    private final UserRepository userRepository;

    @Value("${prefix.image-prefix}")
    private String imagePrefix;

    @Transactional(readOnly = true)
    public MyGameListResult getMyGameList(String curUserEmail, MyGameListQuery myGameListQuery) {
        if (myGameListQuery.cursorGameId() != null) {
            Game cursorGame = gameReader.getGameByGameId(myGameListQuery.cursorGameId());

            if (!cursorGame.creatorEmail().equals(curUserEmail)) {
                throw new CoreException(GAME_FORBIDDEN, ErrorData.of("gameId", myGameListQuery.cursorGameId()));
            }
        }

        List<Game> myGames = gameReader.getMyGameList(
                myGameListQuery.toMyGameListCursor(),
                myGameListQuery.limit(),
                curUserEmail
        );

        return new MyGameListResult(myGames.stream()
                .map(game -> new MyGameListResult.MyGameResult(
                        game.gameId(),
                        imagePrefix + game.gameThumbnailUrl(),
                        game.gameTitle(),
                        game.questionCount(),
                        game.isShared(),
                        game.playCount(),
                        game.updatedAt()))
                .toList());
    }

    public AuthResponseDto getUserByEmail(SessionUserInfoDto userInfoDto){
        UserEntity byId = userRepository.findById(userInfoDto.getEmail())
                .orElseThrow(()->new CoreException(ErrorType.UNAUTHORIZED));
        return AuthResponseDto.createResponseDto(byId);

    }

}
