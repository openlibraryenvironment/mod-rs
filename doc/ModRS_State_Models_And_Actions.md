
# Event Propogation

##Event Walkthrough
1. Patron Request
		A thing happens
2. Application Listener Service
		Another thing happens
3. Event Publication Service
		Publish as JSON
4. Kafka Topic
		Something happens outside the scope of this module and returns consumer poll
5. Event Consumer Service
		Notified
6. Reshare Application Event Handler
		Handle new patron request

##Event Sequence Diagram
Image goes here