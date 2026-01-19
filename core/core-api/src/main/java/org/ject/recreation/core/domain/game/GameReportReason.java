package org.ject.recreation.core.domain.game;

public enum GameReportReason {
    VIOLENT_OR_DISTURBING_CONTENT(
            "폭력적이거나 불편한 컨텐츠"
    ),

    SEXUAL_CONTENT(
            "성적인 컨텐츠"
    ),

    CYBERBULLYING_OR_HARASSMENT(
            "사이버 폭력 또는 괴롭힘"
    ),

    SUICIDE_OR_SELF_HARM(
            "자살 또는 자해"
    ),

    FRAUD_OR_MISINFORMATION(
            "사기 또는 거짓된 정보"
    ),

    SPAM_OR_PROMOTION(
            "스팸 또는 홍보"
    ),

    PRIVACY_VIOLATION(
            "개인정보 침해"
    ),

    INTELLECTUAL_PROPERTY_INFRINGEMENT(
            "지식재산권 침해"
    );

    private final String displayName;

    GameReportReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
