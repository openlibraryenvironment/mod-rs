// Place your Spring DSL code here
import grails.util.Environment

import org.olf.rs.*;

beans = {

  switch(Environment.current) {
    case Environment.TEST:
      sharedIndexService(MockSharedIndexImpl)
    //   hostLMSService(MockHostLMSServiceImpl)
      break
  }
}
