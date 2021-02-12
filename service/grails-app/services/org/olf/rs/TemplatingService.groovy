package org.olf.templating

import grails.gorm.transactions.Transactional

import uk.co.cacoethes.handlebars.HandlebarsTemplateEngine
import com.github.jknack.handlebars.Handlebars

import com.github.jknack.handlebars.helper.StringHelpers
import com.github.jknack.handlebars.EscapingStrategy

import java.time.LocalDateTime
import com.k_int.web.toolkit.settings.AppSetting

import groovy.util.logging.Slf4j;

@Slf4j
@Transactional
public class TemplatingService {

  public static Map performTemplate(TemplateContainer templateContainer, Map binding, String locality) {

    String resolver = templateContainer.templateResolver.value
    switch (resolver) {
      case 'handlebars':
        return performHandlebarsTemplate(templateContainer, binding, locality)
        break;
      default:
        log.warn("No method defined for template resolver (${resolver})")
        break;
    }
  }

  public static String performHandlebarsTemplateString(String template, Map binding) {
      // Set up handlebars configuration
      EscapingStrategy noEscaping = new EscapingStrategy() {
        public String escape(final CharSequence value) {
          return value.toString()
        }
      };

      def handlebars = new Handlebars().with(noEscaping)
      
      handlebars.registerHelpers(StringHelpers)
      def engine = new HandlebarsTemplateEngine()
      engine.handlebars = handlebars


      String outputString = ''
      def boundTemplate = engine.createTemplate(template).make(binding)
      StringWriter sw = new StringWriter()
      boundTemplate.writeTo(sw)
      outputString = sw.toString()

      return outputString
  }

  public static Map performHandlebarsTemplate(TemplateContainer templateContainer, Map binding, String locality) {
    Map output = [:]
    Map result = [:]
    Map meta = [:]

    try {
      // TODO for now we hardcode text/html format
      meta.outputFormat = "text/html"
      LocalizedTemplate lt = LocalizedTemplate.executeQuery("""
        SELECT lt FROM LocalizedTemplate AS lt
        WHERE lt.owner.id = :ownerId
        AND
        lt.locality = :locality
      """,[ownerId: templateContainer.id, locality: locality])[0]
      if (lt == null) {
        throw new Exception("No localized template exists with owner: ${templateContainer.id} and locality: ${locality}")
      }
      
      try {
        String body = performHandlebarsTemplateString(lt.template.templateBody, binding)
        result.header = performHandlebarsTemplateString(lt.template.header, binding)
        result.body = body

        meta.lang = locality
        meta.size = body.length()
        meta.dateCreate = LocalDateTime.now()
      } catch (Exception e) {
        log.error("Failed to perform template: ${e.message}")
      }

    } catch (Exception e) {
      log.error("Failed to get localised template for locality ${locality}: ${e.message}")
    }

    output.result = result
    output.meta = meta

    output
  }

  public static safelyRemoveSettings(String tcId) {
    // If deleting a TemplateContainer, ensure you reset all settings using it
    AppSetting.withNewSession {
      AppSetting.withNewTransaction {
        ArrayList<AppSetting> settingList = AppSetting.findAllBySettingTypeAndValue("Template", tcId)
        log.debug("LOGDEBUG SETTINGLIST: ${settingList}")
        settingList.forEach{setting ->
          setting.value = null
          setting.save(failOnError: true)
        }
      }
    }
  }
}
