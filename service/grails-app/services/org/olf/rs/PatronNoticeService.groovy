package org.olf.rs

import org.hibernate.LockMode;
import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.templating.TemplateContainer;
import org.olf.templating.TemplatingService;

import com.k_int.web.toolkit.refdata.RefdataValue;

import groovy.json.JsonBuilder;

/**
 * This service adds generates and processes the notices events
 * @author Chas
 *
 */
public class PatronNoticeService {

    private static final String CATEGORY_DIRECTORY_ENTRY_STATUS = 'DirectoryEntry.Status';
    private static final String CATEGORY_DIRECTORY_ENTRY_TYPE   = 'DirectoryEntry.Type';

    private static final String DIRECTORY_ENTRY_STATUS_MANAGED   = 'Managed';
    private static final String DIRECTORY_ENTRY_TYPE_INSTITUTION = 'Institution';

    EmailService emailService
    TemplatingService templatingService

    public void triggerNotices(PatronRequest pr, RefdataValue trigger) {
        log.debug("triggerNotices(${pr.patronEmail}, ${trigger.value})")

        // The values from the request we are interested in for the notices
        Map values = requestValues(pr);
        triggerNotices(new JsonBuilder(values).toString(), trigger, pr);
    }

    public void triggerNotices(HostLMSPatronProfile hostLMSPatronProfile) {
        // We need to find the email address for the institution

        Map values = [
            email: getAdminEmail(),
            user: [
                patronProfile: hostLMSPatronProfile.name
            ]
        ];

        // There is only one type of notice that can be sent for patron profiles, so it is hard coded here
        triggerNotices(new JsonBuilder(values).toString(), RefdataValue.lookupOrCreate(RefdataValueData.VOCABULARY_NOTICE_TRIGGERS, RefdataValueData.NOTICE_TRIGGER_NEW_PATRON_PROFILE), null);
    }

    public void triggerNotices(HostLMSLocation hostLMSLocation) {
        // We need to find the email address for the institution

        Map values = [
            email: getAdminEmail(),
            item: [
                location: hostLMSLocation.name
            ]
        ];

        // There is only one type of notice that can be sent for patron profiles, so it is hard coded here
        triggerNotices(new JsonBuilder(values).toString(), RefdataValue.lookupOrCreate(RefdataValueData.VOCABULARY_NOTICE_TRIGGERS, RefdataValueData.NOTICE_TRIGGER_NEW_HOST_LMS_LOCATION), null);
    }

    public void triggerNotices(HostLMSShelvingLocation hostLMSShelvingLocation) {
        // We need to find the email address for the institution

        Map values = [
            email: getAdminEmail(),
            item: [
                shelvingLocation: hostLMSShelvingLocation.name
            ]
        ];

        // There is only one type of notice that can be sent for patron profiles, so it is hard coded here
        triggerNotices(new JsonBuilder(values).toString(), RefdataValue.lookupOrCreate(RefdataValueData.VOCABULARY_NOTICE_TRIGGERS, RefdataValueData.NOTICE_TRIGGER_NEW_HOST_LMS_SHELVING_LOCATION), null);
    }

    public void triggerNotices(String jsonData, RefdataValue trigger, PatronRequest pr) {
        NoticeEvent ne = new NoticeEvent(patronRequest: pr, jsonData: jsonData, trigger: trigger)
        ne.save(flush:true, failOnError:true)
    }

    public void processQueue() {
        log.debug("Processing patron notice queue")
        try {
            NoticeEvent.withSession { sess ->
                NoticeEvent.executeQuery('select ne, npn from NoticeEvent ne, NoticePolicyNotice npn where ne.sent = false and npn.trigger.id = ne.trigger.id').each { selectedObjects ->
                    NoticeEvent noticeEvent = selectedObjects[0];
                    // Now see if we can lock the NoticeEvent record
                    if (lockNoticeEvent(noticeEvent)) {
                        NoticePolicyNotice noticePolicyNotice = selectedObjects[1];
                        NoticePolicy noticePolicy = noticePolicyNotice.noticePolicy;
                        Map values = null;
                        if (noticeEvent.jsonData == null) {
                            // This shouldn't happen, it will be for those requests that occurred, just before the upgrade to when we started using jsonData
                            values = requestValues(noticeEvent.patronRequest);
                        } else {
                            // The new way of doing it
                            values = new groovy.json.JsonSlurper().parseText(noticeEvent.jsonData);
                        }

                        // If we do not have an email address or the policy is not active then do not attempt to send it
                        if ((values.email != null) && (noticePolicy.active == true)) {
                            TemplateContainer template = noticePolicyNotice.template;
                            log.debug("Generating patron notice corresponding to trigger ${noticeEvent.trigger.value} for policy ${noticePolicy.name} and template ${template.name}")
                            try {
                                Map tmplResult = templatingService.performTemplate(template, values, 'en');
                                if (tmplResult.result.body && tmplResult.result.body.trim()) {
                                    Map emailParams = [
                                        notificationId: noticePolicyNotice.id,
                                        to: values.email,
                                        header: tmplResult.result.header,
                                        body: tmplResult.result.body,
                                        outputFormat: 'text/html'
                                    ];
                                    emailService.sendEmail(emailParams);
                                }
                            } catch (Exception e) {
                                log.error("Problem sending notice", e);
                            }
                        }

                        // "sent" in this case is more like processed -- not all events necessarily result in notices
                        noticeEvent.sent = true;
                        noticeEvent.save(flush:true, failOnError:true);
                    }
                }
            }
            NoticeEvent.executeUpdate('delete NoticeEvent ne where ne.sent = true');
        } catch (Exception e) {
            log.error("Problem processing notice triggers", e);
        } finally {
            log.debug("Completed processing of patron notice triggers");
        }
    }

    private boolean lockNoticeEvent(NoticeEvent noticeEvent) {
        boolean lockObtained = false;
        try {
            NoticeEvent.withCriteria {
                eq "id", Long.valueOf(noticeEvent.id)
                delegate.criteria.lockMode = LockMode.UPGRADE_NOWAIT
            };

            // the noticeEvent record is now locked
            lockObtained = true;
        } catch (Exception e) {
            log.error("Failed to lock NoticeEvent record with id: " + noticeEvent.id);
        }

        return(lockObtained);
    }

    private Map requestValues(PatronRequest pr) {
        // The values from the request we are interested in for the notices
        Map values = [
            email: pr.patronEmail,
            user: [
                id: pr.patronIdentifier,
                givenName: pr?.patronGivenName ?: '',
                surname: pr.patronSurname,
                patronProfile: pr?.resolvedPatron?.userProfile ?: ''
            ],
            request: [
                id: pr.hrid,
                pickupLocation: pr?.pickupLocation ?: '',
                pickupURL: pr?.pickupURL ?: '',
                neededBy: pr?.neededBy?.toString() ?: '',
                cancellationReason: pr?.cancellationReason?.label ?: ''
            ],
            item: [
                barcode: pr?.selectedItemBarcode ?: '',
                title: pr.title,
                materialType: pr?.publicationType?.label ?: '',
                location: pr?.pickLocation?.name,
                shelvingLocation: pr?.pickShelvingLocation?.name
            ]
        ];
        return(values);
    }

    /**
     * returns the administrators email address for the managed institution
     * If there is more than 1 institution for the tenant, then it just returns the first one it finds for a managed directory entry
     * The institution itself dosn't necessarily need to be managed, as long a one of its children is as it will look up the hierarchy of all managed entries until it finds an institution record
     * @return The administrators email address or null if it does not find one
     */
    private String getAdminEmail() {
        String institutionEmail = null;

        // We need to look the institution directory entry, so find all the managed entries
        RefdataValue refdataManaged = RefdataValue.lookupOrCreate(CATEGORY_DIRECTORY_ENTRY_STATUS, DIRECTORY_ENTRY_STATUS_MANAGED, DIRECTORY_ENTRY_STATUS_MANAGED);
        DirectoryEntry[] allManaged = DirectoryEntry.findAllByStatus(refdataManaged);
        if (allManaged != null) {
            // Good start we have at least 1 managed, so loop through them to find an institution entry
            RefdataValue refdataInstitution = RefdataValue.lookupOrCreate(CATEGORY_DIRECTORY_ENTRY_TYPE, DIRECTORY_ENTRY_TYPE_INSTITUTION, DIRECTORY_ENTRY_TYPE_INSTITUTION);
            allManaged.each { directoryEntry ->
                // Only interested in the first institution we find
                DirectoryEntry currentEntry = directoryEntry;
                while ((currentEntry != null) && (institutionEmail == null)) {
                    if ((currentEntry.type != null) &&
                        (refdataInstitution.id == currentEntry.type.id) &&
                        (currentEntry.emailAddress != null)) {
                        // We have found the institution
                        institutionEmail = currentEntry.emailAddress;
                    } else {
                        // Take a look at the parent
                        currentEntry = currentEntry.parent;
                    }
                }
            }
        }

        // Return the email address of the institution to the caller
        return(institutionEmail);
    }
}
