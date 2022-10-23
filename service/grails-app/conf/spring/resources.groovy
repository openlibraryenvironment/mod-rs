// Place your Spring DSL code here
import grails.util.Environment
import org.olf.rs.sharedindex.jiscdiscover.JiscDiscoverApiConnectionImpl
import org.olf.rs.*;

beans = {

  switch(Environment.current) {
    case Environment.TEST:
    //  sharedIndexService(MockSharedIndexImpl)
    //   hostLMSService(MockHostLMSServiceImpl)
      emailService(MockEmailServiceImpl)
      jiscDiscoverApiConnection(JiscDiscoverApiConnectionMock)
      break
    default:
      emailService(FolioEmailServiceImpl)
      jiscDiscoverApiConnection(JiscDiscoverApiConnectionImpl)
      break
  }
}
