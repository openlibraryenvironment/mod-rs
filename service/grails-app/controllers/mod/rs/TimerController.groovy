package mod.rs

import org.olf.rs.Timer

import io.swagger.annotations.Api;

@Api(value = "/rs/timers", tags = ["Timer Controller"], description = "API for all things to do with timers")
class TimerController extends OkapiTenantAwareSwaggerController<Timer> {

  static responseFormats = ['json', 'xml']

  TimerController() {
    super(Timer)
  }
}
