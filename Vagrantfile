# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
# Vagrant::DEFAULT_SERVER_URL.replace('https://vagrantcloud.com')

Vagrant.configure(2) do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # Every Vagrant development environment requires a box. You can search for
  # boxes at https://atlas.hashicorp.com/search.
  config.vm.box = "folio/testing-backend"
    
  config.vm.provider "virtualbox" do |v|
    v.memory = 12448
    v.cpus = 5
  end

  # Disable automatic box update checking. If you disable this, then
  # boxes will only be checked for updates when the user runs
  # `vagrant box outdated`. This is not recommended.
  # config.vm.box_check_update = false

  # Expose the postgres instance that is installed so that apps running outside the 
  # vbox instance can use it.
  config.vm.network "forwarded_port", guest: 5432, host: 54321
  config.vm.network "forwarded_port", guest: 9130, host: 9130

  # Connection oriented ISO 10161
  # config.vm.network "forwarded_port", guest: 8999, host: 8999

  # Zookeeper 2181
  config.vm.network "forwarded_port", guest: 2181, host: 2181
  
  # Kafka
  config.vm.network "forwarded_port", guest: 9092, host: 9092

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # config.vm.network "forwarded_port", guest: 80, host: 8080

  # Create a private network, which allows host-only access to the machine
  # using a specific IP.
  # config.vm.network "private_network", ip: "192.168.33.10"

  # Create a public network, which generally matched to bridged network.
  # Bridged networks make the machine appear as another physical device on
  # your network.
  # config.vm.network "public_network"

  # Share an additional folder to the guest VM. The first argument is
  # the path on the host to the actual folder. The second argument is
  # the path on the guest to mount the folder. And the optional third
  # argument is a set of non-required options.
  # config.vm.synced_folder "../data", "/vagrant_data"

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  # Example for VirtualBox:
  #
  # config.vm.provider "virtualbox" do |vb|
  #   # Display the VirtualBox GUI when booting the machine
  #   vb.gui = true
  #
  #   # Customize the amount of memory on the VM:
  #   vb.memory = "1024"
  # end
  #
  # View the documentation for the provider you are using for more
  # information on available options.

  # Define a Vagrant Push strategy for pushing to Atlas. Other push strategies
  # such as FTP and Heroku are also available. See the documentation at
  # https://docs.vagrantup.com/v2/push/atlas.html for more information.
  # config.push.define "atlas" do |push|
  #   push.app = "YOUR_ATLAS_USERNAME/YOUR_APPLICATION_NAME"
  # end

  # Enable provisioning with a shell script. Additional provisioners such as
  # Puppet, Chef, Ansible, Salt, and Docker are also available. Please see the
  # documentation for more information about their specific syntax and use.
  config.vm.provision "shell", inline: <<-SHELL
    # sudo apt-get -y update
    # sudo apt-get -y dist-upgrade
    docker pull wurstmeister/zookeeper
    docker pull wurstmeister/kafka
    docker run --name zookeeper-1 --restart always -p 2181:2181 -d wurstmeister/zookeeper
    docker run --name kafka-1 --restart always -p 9092:9092 \
             -e "KAFKA_ADVERTISED_HOST_NAME=localhost" \
             -e "KAFKA_ZOOKEEPER_CONNECT=zookeeper-1:2181" \
             -e "KAFKA_BROKER_ID=1" \
             -e "KAFKA_LOG_RETENTION_BYTES=-1" \
             -e "KAFKA_LOG_RETENTION_HOURS=-1" \
             -d wurstmeister/kafka

  SHELL
end
