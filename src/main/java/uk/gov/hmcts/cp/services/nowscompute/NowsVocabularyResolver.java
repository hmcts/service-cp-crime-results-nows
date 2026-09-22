package uk.gov.hmcts.cp.services.nowscompute;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.AttendanceDay;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.DefendantAttendance;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.HearingDetail;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResult;
import uk.gov.hmcts.cp.domain.HearingDetailsResponse.JudicialResultPrompt;
import uk.gov.hmcts.cp.domain.nowscompute.NowsVocabulary;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Component
public class NowsVocabularyResolver {

    private static final String PRISON_ORGANISATION_NAME_PROMPT = "prisonOrganisationName";
    private static final String CUSTODY_POLICE = "Police Station";
    private static final String CUSTODY_PRISON = "Prison";
    private static final String ATTENDANCE_IN_PERSON = "IN_PERSON";
    private static final String ATTENDANCE_BY_VIDEO = "BY_VIDEO";

    public NowsVocabulary resolve(final MergedDefendant defendant, final HearingDetail hearing) {
        final boolean custodyLocationIsPolice = CUSTODY_POLICE.equalsIgnoreCase(defendant.custody());
        final boolean custodyLocationIsPrison = CUSTODY_PRISON.equalsIgnoreCase(defendant.custody());

        final long totalResults = defendant.results().size();
        final long custodialResults = defendant.results().stream().filter(this::isCustodialResult).count();
        final boolean atleastOneCustodialResult = custodialResults > 0;
        final boolean allNonCustodialResults = totalResults > 0 && custodialResults == 0;
        final boolean atleastOneNonCustodialResult = totalResults > custodialResults;

        final boolean welshCourtHearing = hearing.getCourtCentre() != null
                && Boolean.TRUE.equals(hearing.getCourtCentre().getWelshCourtCentre());

        final Attendance attendance = resolveAttendance(defendant, hearing);

        return NowsVocabulary.builder()
                .custodyLocationIsPolice(custodyLocationIsPolice)
                .custodyLocationIsPrison(custodyLocationIsPrison)
                .inCustody(custodyLocationIsPolice || custodyLocationIsPrison)
                .atleastOneCustodialResult(atleastOneCustodialResult)
                .allNonCustodialResults(allNonCustodialResults)
                .atleastOneNonCustodialResult(atleastOneNonCustodialResult)
                .cpsProsecuted(defendant.cpsProsecuted())
                .youthDefendant(defendant.isYouth())
                .adultDefendant(!defendant.isYouth())
                .welshCourtHearing(welshCourtHearing)
                .englishCourtHearing(!welshCourtHearing)
                .appearedInPerson(attendance.inPerson)
                .appearedByVideoLink(attendance.byVideo)
                .anyAppearance(attendance.inPerson || attendance.byVideo)
                .prosecutorMajorCreditor(List.of())
                .nonProsecutorMajorCreditor(List.of())
                .build();
    }

    private boolean isCustodialResult(final JudicialResult result) {
        return result.getJudicialResultPrompts() != null
                && result.getJudicialResultPrompts().stream()
                        .map(JudicialResultPrompt::getPromptReference)
                        .anyMatch(PRISON_ORGANISATION_NAME_PROMPT::equals);
    }

    private Attendance resolveAttendance(final MergedDefendant defendant, final HearingDetail hearing) {
        boolean inPerson = false;
        boolean byVideo = false;
        if (hearing.getDefendantAttendance() != null) {
            for (final DefendantAttendance attendance : hearing.getDefendantAttendance()) {
                if (defendant.defendantIds().contains(attendance.getDefendantId())
                        && attendance.getAttendanceDays() != null) {
                    for (final AttendanceDay day : attendance.getAttendanceDays()) {
                        if (matchesAResultDate(defendant, day.getDay())) {
                            inPerson = inPerson || ATTENDANCE_IN_PERSON.equals(day.getAttendanceType());
                            byVideo = byVideo || ATTENDANCE_BY_VIDEO.equals(day.getAttendanceType());
                        }
                    }
                }
            }
        }
        return new Attendance(inPerson, byVideo);
    }

    private boolean matchesAResultDate(final MergedDefendant defendant, final String day) {
        final boolean matches;
        final Optional<LocalDate> attendanceDay = day == null ? Optional.empty() : parseDate(day);
        if (attendanceDay.isEmpty()) {
            matches = false;
        } else {
            matches = defendant.results().stream()
                    .map(JudicialResult::getOrderedDate)
                    .anyMatch(attendanceDay.get()::equals);
        }
        return matches;
    }

    private Optional<LocalDate> parseDate(final String day) {
        Optional<LocalDate> parsed;
        try {
            parsed = Optional.of(LocalDate.parse(day));
        } catch (DateTimeParseException e) {
            parsed = Optional.empty();
        }
        return parsed;
    }

    private record Attendance(boolean inPerson, boolean byVideo) {
    }
}