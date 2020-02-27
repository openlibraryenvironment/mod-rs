package mod.rs

import grails.gorm.multitenancy.CurrentTenant
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import grails.converters.JSON

@Slf4j
@CurrentTenant
class StatisticsController {
  
  def index() {
    def result = [
      asAt:new Date(),
      current:[
        totalLoans:10,
        totalBorrowing:5,
        LoanToBorrowScore:2,
        LoanToBorrowBucket:-2
      ],
      statisticsPeriod:'7d',
      aggregated:[
        requestsReceived:0,
        respondWillSupply:0,
        respondNotSupplied:0,
        shipped:0,
        requestsMade:0,
        requestToSupplyRatio:0.1234
      ]
    ]

    render result as JSON
  }
  
}
