package mod.rs

import grails.boot.GrailsApp;
import grails.boot.config.GrailsAutoConfiguration;

import com.google.common.base.Predicate
import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//import springfox.documentation.RequestHandler
//import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.grails.SpringfoxGrailsIntegrationConfiguration;
//import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.ant;

// Enable SpringFox on your project
//@EnableSwagger2

// Import the springfox grails integration configuration
//@Import([SpringfoxGrailsIntegrationConfiguration])

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

	// 3. **Optionally** define a custom docket or omit this step to use the default
	// For grails it is preferrable to use use the following settings.
//	@Bean
//	Docket api() {
//	  new Docket(DocumentationType.SWAGGER_2)
//		  .ignoredParameterTypes(MetaClass)
//		  .select()
//		  .paths(Predicates.not(ant("/error")))
//		  .build();

//		return new Docket(DocumentationType.SWAGGER_2).apiInfo(ApiInfo.DEFAULT).select() 
//          .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
//		  .paths(Predicates.not(ant("/error")))
//             .build();
//
//		return new Docket(DocumentationType.SWAGGER_2).apiInfo(ApiInfo.DEFAULT).select() 
//          .apis(IgnoreValidateController())
//             .build();
//	}
  
    //  **Optionally** in the absense of asset pipeline configure the swagger-ui webjar to serve the scaffolded swagger UI
//    @Bean 
//    static WebMvcConfigurerAdapter webConfigurer() { 
//        new WebMvcConfigurerAdapter() { 
//            @Override 
//            void addResourceHandlers(ResourceHandlerRegistry registry) { 
//                if (!registry.hasMappingForPattern("/webjars/**")) { 
//                    registry.addResourceHandler("/webjars/**") 
//                            .addResourceLocations("classpath:/META-INF/resources/webjars/") 
//                } 
//                if (!registry.hasMappingForPattern("/swagger-ui.html")) { 
//                    registry.addResourceHandler("/swagger-ui.html") 
//                            .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html") 
//                } 
//            } 
//        } 
//    }
	 
//	public static Predicate<RequestHandler> IgnoreValidateController() {
//		return new Predicate<RequestHandler>() {
//			@Override
//			public boolean apply(RequestHandler input) {
//				boolean result = input.getName() != "com.k_int.web.toolkit.ValidateController";
//				return result;
//			}
//		};
//	}
}
