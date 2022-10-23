// Place your Spring DSL code here
import grails.util.Environment
import org.olf.rs.*;
import org.olf.rs.sharedindex.*;
import org.olf.rs.sharedindex.jiscdiscover.*;

beans = {

  switch(Environment.current) {
    case Environment.TEST:
      emailService(MockEmailServiceImpl)
      jiscDiscoverApiConnection(JiscDiscoverApiConnectionMock)
      break
    default:
      emailService(FolioEmailServiceImpl)
      jiscDiscoverApiConnection(JiscDiscoverApiConnectionImpl)
      break
  }
}
