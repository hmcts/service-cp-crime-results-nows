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
public class NowMetadataResponse {

    private List<NowDefinition> nows;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NowDefinition {
        private String id;
        // Matched against the fixed, static allow-list (design doc §3e) before any requirement-tree
        // matching runs.
        private String name;
        private Boolean includeAllResults;
        // Template-wide static text — nt.value('key') resolves against this list, keyed by
        // nowReference.
        private List<NowTextEntry> nowTextList;
        private List<NowRequirement> nowRequirements;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NowRequirement {
        // Matched against JudicialResult.judicialResultTypeId, never cjsCode.
        private String resultDefinitionId;
        private Boolean primary;
        private String parentNowRequirementId;
        private String rootResultDefinitionId;
        // Requirement-scoped static text — nrt.value('key') resolves against this list, keyed by
        // nowReference.
        private List<NowTextEntry> nowRequirementText;
        // Nested children — flatten before matching (design doc §3c).
        private List<NowRequirement> nowRequirements;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class NowTextEntry {
        private String nowReference;
        private String text;
        private String welshText;
    }
}