# WEE_RemandAfterBailAppealByProsecutor — materialId `cdc70bcf-548a-4a8e-ab7a-4b534e42d7f5`

Artifacts for the same NOW document, collated for side-by-side comparison:

| File | What it is | Source |
|---|---|---|
| `01-hearingnows-nows_document_request-payload.json` | The `hearingnows.nows_document_request` payload for this materialId — what hearing-nows/the doc generator actually had to work with | Given verbatim. `justice.gov.uk`/`police.uk`/`geoamey.co.uk` email domains redacted to fabricated equivalents before committing (public repo — see HMCTS data-sharing standard) |
| `02-hrds-callback-request-payload.json` | The body of the HTTP call hearing-nows actually sends to HRDS for this materialId — derived by hand-applying `HRDSNotificationTask`/`HRDSNotificationMapper` to file 01 | Constructed from `cpp-context-hearing-nows` source; same redaction applied as file 01 |
| `WEE_RemandAfterBailAppealByProsecutorTemplate.docx` | The exact Docmosis template file 01 is rendered through, kept under its real filename — no case-specific data, generic field codes only | `cpp-context-system-doc-generator`, `templates/nows/WEE_RemandAfterBailAppealByProsecutorTemplate.docx` |
| `Prison - Remand Warrant After Bail Appeal by Prosecutor.pdf` | The actual rendered PDF for this materialId | `~/Downloads` |

Open the template in Word/LibreOffice with field codes visible (Alt+F9) to see which
`dp.value(...)`/`nt.value(...)` calls pull which fields out of 01, and compare against where those
values land in the PDF.
