package org.olf.rs.referenceData

import org.olf.rs.NoticePolicy;
import org.olf.rs.NoticePolicyNotice;
import org.olf.rs.PredefinedId;
import org.olf.templating.LocalizedTemplate;
import org.olf.templating.Template;
import org.olf.templating.TemplateContainer;

import com.k_int.web.toolkit.refdata.RefdataValue;

import groovy.util.logging.Slf4j

/**
 * Class that reads and creates notice templates
 * @author Chas
 *
 */
@Slf4j
public class TemplateData {

    public static final String VOCABULARY_TEMPLATE_RESOLVER = 'TemplateContainer.TemplateResolver';
    public static final String TEMPLATE_RESOLVER_HANDLEBARS = 'Handlebars';

    public static void loadAll() {
        (new TemplateData()).load();
    }

    public void load() {
        log.info('Adding predefined templates to the database');

        // The newPatronProfile
        loadTemplate('templates/newPatronProfile.json');
    }

    private void loadTemplate(String resourcePath) {
        URL resource = this.class.classLoader.getResource(resourcePath);
        if (resource == null) {
            log.error('Unable to find resource: ' + resourcePath);
        } else {
            InputStream stream = resource.openStream();
            try {
                Map parsedJson = (new groovy.json.JsonSlurper()).parse(stream);

                // We now have a map in our hand that we will map into a template container
                if (parsedJson.template == null) {
                    // Can't do anything without a template
                    log.error('No template supplied for resource: ' + resourcePath);
                } else {
                    // Excellent start let us see if we already have this template
                    String localizedTemplateId = getReferencedId('localized_template', parsedJson.template.predefinedId);
                    LocalizedTemplate localizedTemplate;
                    if (localizedTemplateId == null) {
                        // didn't previously exist, so we need to create a new template
                        localizedTemplate = new LocalizedTemplate();
                        localizedTemplate.locality = 'en';
                    } else {
                        // It does already exist
                        localizedTemplate = LocalizedTemplate.get(localizedTemplateId);
                    }

                    // Ensure the localized template has a template
                    if (localizedTemplate.template == null) {
                        localizedTemplate.template = new Template();
                    }

                    // Now update the template
                    localizedTemplate.template.header = parsedJson.template.header;
                    localizedTemplate.template.templateBody = parsedJson.template.templateBody;

                    // save the template
                    localizedTemplate.template.save(flush:true, failOnError:true);

                    // Now look to see if the container exists
                    String templateContainerId = getReferencedId('template_container', parsedJson.predefinedId);
                    TemplateContainer  templateContainer;
                    if (templateContainerId == null) {
                        // We need to create a new template container
                        templateContainer = new TemplateContainer();
                        templateContainer.context = 'noticeTemplate';

                        // This appears to have a default, but dosn't seem to kick in before validation
                        templateContainer.templateResolver = RefdataValue.lookupOrCreate(VOCABULARY_TEMPLATE_RESOLVER, TEMPLATE_RESOLVER_HANDLEBARS);
                    } else {
                        // It does already exist
                        templateContainer = TemplateContainer.get(templateContainerId);
                    }

                    // Set the name and description
                    templateContainer.name = parsedJson.name;
                    templateContainer.description = parsedJson.description;

                    // Save the template container
                    templateContainer.save(flush:true, failOnError:true);

                    // Create the mapping with the predefined id
                    PredefinedId.ensureExists('template_container', parsedJson.predefinedId, templateContainer.id);

                    // Now add the  localized template
                    if (localizedTemplate.owner == null) {
                        localizedTemplate.owner = templateContainer;
                        templateContainer.addToLocalizedTemplates(localizedTemplate);
                    }

                    // save the container and localized template
                    localizedTemplate.save(flush:true, failOnError:true);
                    templateContainer.save(flush:true, failOnError:true);

                    // Create the mapping with the predefined id
                    PredefinedId.ensureExists('localized_template', parsedJson.template.predefinedId, localizedTemplate.id);

                    // Now we have a template we now need to create a notice policy and notice policy notice records but we only do this once
                    String noticePolicyId = getReferencedId('notice_policy', parsedJson.predefinedId);
                    if (noticePolicyId == null) {
                        NoticePolicy noticePolicy = new NoticePolicy();
                        noticePolicy.name = parsedJson.name;
                        noticePolicy.description = parsedJson.description;
                        noticePolicy.active = false;
                        noticePolicy.save(flush:true, failOnError:true);

                        // Create the mapping with the predefined id
                        PredefinedId.ensureExists('notice_policy', parsedJson.predefinedId, noticePolicy.id);

                        // Now for the NoticePolicyNotice
                        NoticePolicyNotice noticePolicyNotice = new NoticePolicyNotice();
                        noticePolicyNotice.template = templateContainer;
                        noticePolicyNotice.realTime = true;
                        noticePolicyNotice.format = RefdataValue.lookupOrCreate(Settings.VOCABULARY_NOTICE_FORMATS, 'E-mail', 'email');
                        noticePolicyNotice.trigger = RefdataValue.lookupOrCreate(Settings.VOCABULARY_NOTICE_TRIGGERS, Settings.NOTICE_TRIGGER_NEW_PATRON_PROFILE);
                        noticePolicyNotice.noticePolicy = noticePolicy;
                        noticePolicyNotice.save(flush:true, failOnError:true);
                        noticePolicy.addToNotices(noticePolicyNotice);
                        noticePolicy.save(flush:true, failOnError:true);
                    }
                }
            } catch (Exception e) {
                log.error('Exception thrown while loading template from resource: ' + resourcePath, e);
            } finally {
                // Close all the resources associated with the stream, may not need to do this ...
                stream.close();
            }
        }
    }

    private String getReferencedId(String namespace, String predefinedId) {
        return(PredefinedId.lookupReferenceId(namespace, predefinedId));
    }
}
