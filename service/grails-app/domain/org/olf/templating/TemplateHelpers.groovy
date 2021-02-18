package org.olf.templating

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.net.URLEncoder

public enum TemplateHelpers implements Helper<Object> {

  insertBefore {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      String op0 = options.param(0, "")
      String op1 = options.param(1, "")
      def regex = "(?<=(?=${regexSafe(op0)}))"
      return value.toString().replaceFirst(~/${regex}/, op1)
    }
  },

  insertAfter {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      String op0 = options.param(0, "")
      String op1 = options.param(1, "")
      def regex = "(?=(?<=${regexSafe(op0)}))"
      return value.toString().replaceFirst(~/${regex}/, op1)
    }
  },

  insertBeforeAll {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      String op0 = options.param(0, "")
      String op1 = options.param(1, "")
      def regex = "(?<=(?=${regexSafe(op0)}))"
      return value.toString().replaceAll(~/${regex}/, op1)
    }
  },

  insertAfterAll {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      String op0 = options.param(0, "")
      String op1 = options.param(1, "")
      def regex = "(?=(?<=${regexSafe(op0)}))"
      return value.toString().replaceAll(~/${regex}/, op1)
    }
  },

  removeProtocol {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      return value.toString().replaceAll("https://", "").replaceAll("http://", "")
    }
  },

  urlEncode {
    @Override
    protected CharSequence safeApply(final Object value, final Options options) {
      return URLEncoder.encode(value.toString(), "UTF-8")
    }
  },
  // Handlebars helper stuff
  @Override
  public CharSequence apply(final Object context, final Options options) throws IOException {
    if (options.isFalsy(context)) {
      Object param = options.param(0, null);
      return param == null ? null : param.toString();
    }
    return safeApply(context, options);
  }

  protected abstract CharSequence safeApply(Object context, Options options)

  public void registerHelper(final Handlebars handlebars) {
    notNull(handlebars, "The handlebars is required.");
    handlebars.registerHelper(name(), this);
  }

  public static void register(final Handlebars handlebars) {
    notNull(handlebars, "A handlebars object is required.");
    TemplateHelpers[] helpers = values();
    for (TemplateHelpers helper : helpers) {
      helper.registerHelper(handlebars);
    }
  }

  public String regexSafe(String inputString) {
    String outputString = inputString.replace("\\", "\\\\")
      .replace("*", "\\*")
      .replace("(", "\\(")
      .replace(")", "\\)")
      .replace(".", "\\.")
      .replace("|", "\\|")
      .replace("?", "\\?")
      .replace("!", "\\!")
      .replace("+", "\\+")
      .replace("}", "\\}")
      .replace("{", "\\{")
      .replace("]", "\\]")
      .replace("[", "\\[")
      .replace("^", "\\^")
      .replace("\$", "\\\$")

    return outputString
  }
}