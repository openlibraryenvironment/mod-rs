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
  # config.vm.box = "projectreshare/development"
    
  config.vm.provider "virtualbox" do |v|
    v.memory = 12288
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
  config.vm.network "forwarded_port", guest: 8999, host: 8999

  # RabbitMQ ports
  config.vm.network "forwarded_port", guest: 15672, host: 15672
  config.vm.network "forwarded_port", guest: 5672, host: 5672
  # Erlang and inet_dist_listen_min/max cluster ports - probably not needed for normal dev operation
  config.vm.network "forwarded_port", guest: 4369, host: 4369
  config.vm.network "forwarded_port", guest: 35197, host: 35197


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
    sudo apt-get -y update
    sudo apt-get -y install rabbitmq-server
    # sudo apt-get install -y apache2

    # Create the rabbit user for message passing
    if [ `rabbitmqctl list_users | grep -i "^rsms" | wc -l` -eq 0 ]
    then
      echo Creating message queue user rsms for resource sharing message services
      rabbitmqctl add_user rsms rsms
      rabbitmqctl set_user_tags rsms administrator
      rabbitmqctl set_permissions -p / rsms ".*" ".*" ".*"
    else
      echo RabbitMQ user rsms already present
    fi

    # The medium term goal is to remove this user and use the rsms account above. This account
    # will be deprecated in a future release, do not rely upon it
    if [ `rabbitmqctl list_users | grep -i "^adm" | wc -l` -eq 0 ]
    then
      echo Creating message queue user adm for resource sharing message services
      rabbitmqctl add_user adm admpass
      rabbitmqctl set_user_tags adm administrator
      rabbitmqctl set_permissions -p / adm ".*" ".*" ".*"
    else
      echo RabbitMQ user adm already present
    fi
  SHELL
end
