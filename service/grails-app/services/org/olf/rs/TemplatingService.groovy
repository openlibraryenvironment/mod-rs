package org.olf.rs

import uk.co.cacoethes.handlebars.HandlebarsTemplateEngine
import com.github.jknack.handlebars.Handlebars

import com.github.jknack.handlebars.helper.StringHelpers
import com.github.jknack.handlebars.EscapingStrategy

public class TemplatingService {

  public static String performTemplate(String template, Map binding, String resolver) {
    switch (resolver) {
      case 'handlebars':
        return performHandlebarsTemplate(template, binding)
        break;
      default:
        log.error("No method defined for template resolver (${resolver})")
        break;
    }
  }

  public static String performHandlebarsTemplate(String template, Map binding) {
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

}
