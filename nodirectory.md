Changes have been implemented to allow mod-rs to function without an external directory, allowing the rota and selection of peers to be handled by an external broker.

In order to put mod-rs in this mode of operation, the `routing_adapter` setting in the **Routing** section needs to be set to 'disabled'. This will inform mod-rs that we will not attempt to build a rota, nor will we pull from a rota when sending messages on to a lender.

To accomodate the lack of a rota, there are two crucial settings that need to be populated. The first is `iso18626_gateway_address` in the **Network** section. This should be the URL of the iso18626 endpoint of the broker that will be handling sending and receiving of iso message to and from mod-rs.

The second setting is `default_peer_symbol` in the **Requests** section. This should be populated to be the qualified symbol name that the broker will be using, so that mod-rs can populate the appropriate sections of the iso18626 message headers.

It is worth noting that mod-rs will still need entries in its local directory entry that correspond with the sending and recieving symbols from the external broker, but these entries can simply be stubs and do not require services to be defined.
