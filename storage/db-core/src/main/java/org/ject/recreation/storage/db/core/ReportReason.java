package org.ject.recreation.storage.db.core;

public enum ReportReason {
    VIOLENT_OR_DISTURBING_CONTENT,
    SEXUAL_CONTENT,
    CYBERBULLYING_OR_HARASSMENT,
    SUICIDE_OR_SELF_HARM,
    FRAUD_OR_MISINFORMATION,
    SPAM_OR_PROMOTION,
    PRIVACY_VIOLATION,
    INTELLECTUAL_PROPERTY_INFRINGEMENT;

    public static ReportReason getReportReason(String reason) {
        if (reason == null) return null;
        return ReportReason.valueOf(reason);
    }

    public boolean isBlank() {
        return false;
    }
}