package mod.rs

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;
import groovy.util.logging.Slf4j;

import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry;
import org.grails.datastore.mapping.model.MappingContext;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.gorm.validation.constraints.registry.DefaultValidatorRegistry;

import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
import io.micronaut.context.annotation.Bean
import io.undertow.Undertow.Builder
import io.undertow.UndertowOptions


@Slf4j 
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    void doWithApplicationContext() {
      try {
        // Register the pending action constraint
        MappingContext mc = grailsApplication.getMappingContext();
        DefaultValidatorRegistry reg = mc.validatorRegistry
      } catch (Exception e) {
        log.error("Exception thrown", e);
      }
    }

  @Bean
  UndertowServletWebServerFactory embeddedServletContainerFactory(){
    UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory()
    factory.builderCustomizers << new UndertowBuilderCustomizer() {

      @Override
      public void customize(Builder builder) {

        // Min of 2, Max of 4 I/O threads
        builder.ioThreads = Math.min(Math.max(Runtime.getRuntime().availableProcessors(), 2), 4)

        final int heap_coef = (Runtime.getRuntime().maxMemory() / 1024 / 1024)/256
        int workers_per_io = 8
        if (heap_coef <= 2) {
          workers_per_io = 6
        } else if (heap_coef <= 1) {
          workers_per_io = 4
        }

        // 8 Workers per I/O thread
        builder.workerThreads = builder.workerThreads = builder.ioThreads * workers_per_io

        // Enable HTTP2
        builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true)

        println("Runtime memory reported ${Runtime.getRuntime().maxMemory() / 1024 / 1024} mb")
        println("Runtime CPUs reported ${Runtime.getRuntime().availableProcessors()}")
        println("Allocated ${builder.ioThreads} IO Threads")
        println("Allocated ${builder.workerThreads} worker threads")
        println("Allocated ${builder.bufferSize} bytes of ${builder.directBuffers ? 'direct' : 'indirect'} buffer space per thread")
      }
    }
  }

}
