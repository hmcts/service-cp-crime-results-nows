package uk.gov.hmcts.cp.domain.nowscompute;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NowsSubscriptionsResponse {

    private List<NowsSubscription> nowSubscriptions;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NowsSubscription {
        private Boolean isNowSubscription;
        private Boolean isEDTSubscription;
        private Boolean applySubscriptionRules;
        private NowsSubscriptionVocabulary subscriptionVocabulary;
    }

    // Boolean, not boolean — a subscription omits keys it doesn't configure; null means "not
    // configured" (fail-closed), same convention as the legacy source's own vocabulary flags.
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NowsSubscriptionVocabulary {
        private Boolean isCpsProsecuted;

        private Boolean anyAppearance;
        private Boolean appearedInPerson;
        private Boolean appearedByVideoLink;

        private Boolean anyMajorCreditor;
        private Boolean requiresProsecutorMajorCreditor;
        private Boolean requiresNonProsecutorMajorCreditor;

        private Boolean anyCourtHearing;
        private Boolean englishCourtHearing;
        private Boolean welshCourtHearing;

        private Boolean adultOrYouthDefendant;
        private Boolean youthDefendant;
        private Boolean adultDefendant;

        private Boolean ignoreCustody;
        private Boolean inCustody;
        private Boolean custodyLocationIsPolice;
        private Boolean custodyLocationIsPrison;

        private Boolean ignoreResults;
        private Boolean allNonCustodialResults;
        private Boolean atleastOneCustodialResult;
        private Boolean atleastOneNonCustodialResult;

        // Prompts are objects on the real API, not bare strings — matched on resultPromptReference.
        private List<ResultPrompt> includedPrompts;
        private List<ResultPrompt> excludedPrompts;
        // Matched by exact judicialResultTypeId value.
        private List<String> includedResults;
        private List<String> excludedResults;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class ResultPrompt {
        private String resultPromptId;
        private String resultPromptReference;
    }
}