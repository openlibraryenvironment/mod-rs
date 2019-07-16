import groovy.transform.*
import org.olf.rs.workflow.StateTransition

@Field StateTransition stateTransition
json g.render(stateTransition, [expand: ['action'
                                       ],
                              excludes: []])

