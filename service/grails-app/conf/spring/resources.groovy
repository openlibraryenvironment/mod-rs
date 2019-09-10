// Place your Spring DSL code here
import grails.util.Environment

beans = {

  switch(Environment.current) {
    case Environment.TEST:
      sharedIndexService(org.olf.rs.MockSharedIndexImpl)
      break
  }
}
