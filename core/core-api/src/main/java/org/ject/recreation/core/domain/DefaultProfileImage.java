package org.ject.recreation.core.domain;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public enum DefaultProfileImage {
    PROFILE_0("profiles/프로필01.png"),
    PROFILE_1("profiles/프로필02.png"),
    PROFILE_2("profiles/프로필03.png"),
    PROFILE_3("profiles/프로필04.png"),
    PROFILE_4("profiles/프로필05.png"),
    PROFILE_5("profiles/프로필06.png"),
    PROFILE_6("profiles/프로필07.png"),
    PROFILE_7("profiles/프로필08.png"),
    PROFILE_8("profiles/프로필09.png"),
    PROFILE_9("profiles/프로필10.png"),
    PROFILE_10("profiles/프로필11.png"),
    PROFILE_11("profiles/프로필12.png"),
    PROFILE_12("profiles/프로필13.png"),
    PROFILE_13("profiles/프로필14.png"),
    PROFILE_14("profiles/프로필15.png"),
    PROFILE_15("profiles/프로필16.png");

    private final String imagePath;

    DefaultProfileImage(String imagePath) {
        this.imagePath = imagePath;
    }

    public static String getRandomImagePath() {
        DefaultProfileImage[] values = values();
        DefaultProfileImage value = values[ThreadLocalRandom.current().nextInt(values.length)];
        return value.getImagePath();
    }
}
