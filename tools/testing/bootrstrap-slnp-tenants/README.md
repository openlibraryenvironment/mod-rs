# Postman newman CLI script - bootrstrap SLNP tenants
* Newman version: 6.1.1
* Ensure you're using Node.js v16 or later.

# Install Newman from npm globally on your system, enabling you to run it from anywhere:

    npm install -g newman

# You can run collection and pass environment file:

    newman run mod-rs-setup.collection.json -e mod-rs-env.dev_environment.json

# Start application with test data
To start up application you need to do these steps:
* Start docker-compose file
* Set up environment variables
* Start mod-rs service
* Insert test data

If you have installed `newman` then all these actions are added in script `up-all.sh` and it will start `mod-rs` service on port `9177` with all test data

To shut down running application you can use script `down-all.sh`