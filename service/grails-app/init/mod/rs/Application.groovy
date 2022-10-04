package mod.rs

import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.validation.ValidatorRegistry
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j;
import io.undertow.Undertow.Builder


@EnableScheduling
@CompileStatic
@Slf4j
class Application extends GrailsAutoConfiguration {

  static void main(String[] args) {
    GrailsApp.run(Application, args)
  }

  void doWithApplicationContext() {
    try {
      // Register the pending action constraint
      MappingContext mc = grailsApplication.getMappingContext();
      ValidatorRegistry reg = mc.getValidatorRegistry()
    } catch (Exception e) {
      log.error("Exception thrown", e);
    }
  }

  @Bean
  public UndertowServletWebServerFactory embeddedServletContainerFactory() {

    UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory()
    factory.builderCustomizers << new UndertowBuilderCustomizer() {

      @Override
      public void customize(Builder builder) {

        // Min of 2, Max of 4 I/O threads
        final int ioThreadCount = Math.min(Math.max(Runtime.getRuntime().availableProcessors(), 2), 4)
        builder.ioThreads = ioThreadCount


        final int heap_coef = ((Runtime.getRuntime().maxMemory() / 1024 / 1024)/256) as int
        int workers_per_io = 8
        if (heap_coef <= 2) {
          workers_per_io = 6
        } else if (heap_coef <= 1) {
          workers_per_io = 4
        }

        // 8 Workers per I/O thread
        final int workerThreadCount = ioThreadCount * workers_per_io
        builder.workerThreads = workerThreadCount
        // Enable HTTP2
        // builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true)

        println "Runtime memory reported ${Runtime.getRuntime().maxMemory() / 1024 / 1024} mb"
        println "Runtime CPUs reported ${Runtime.getRuntime().availableProcessors()}"
        println "Allocated ${ioThreadCount} IO Threads"
        println "Allocated ${workerThreadCount} worker threads"

      }
    }

    return factory
  }

}
