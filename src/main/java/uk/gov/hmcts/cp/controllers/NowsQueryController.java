package uk.gov.hmcts.cp.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.api.NowsApi;
import uk.gov.hmcts.cp.openapi.model.DefendantResult;
import uk.gov.hmcts.cp.services.query.NowsQueryService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class NowsQueryController implements NowsApi {

    private static final String CASE_URN_REGEX = "^[0-9a-zA-Z,]{1,100}$";

    private final NowsQueryService nowsQueryService;

    @Override
    public ResponseEntity<DefendantResult> getDefendantResult(final String caseURN, final UUID hearingId,
                                                                final UUID defendantId, final String eventType) {
        log.info("Received request for NOWs defendant result for caseURN:{} hearingId:{} defendantId:{} eventType:{}",
                Encode.forJava(caseURN), hearingId, defendantId, Encode.forJava(eventType));
        final DefendantResult result =
                nowsQueryService.getDefendantResult(validateCaseUrn(caseURN), hearingId, defendantId, eventType);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    private String validateCaseUrn(final String caseUrn) {
        if (caseUrn == null || !caseUrn.matches(CASE_URN_REGEX)) {
            log.warn("caseURN does not match expected pattern:{}", CASE_URN_REGEX);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Case URN must be between 1 and 100 alphanumerics");
        }
        return caseUrn;
    }
}