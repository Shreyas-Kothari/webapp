#!/bin/bash

set -e

# Create the directory
sudo mkdir -p "/opt/myapp"

# Add a system user with no login shell and add it to the group csye6225
sudo groupadd csye6225
sudo useradd --system -s /usr/sbin/nologin -g csye6225 csye6225

# # Set ownership to the new user
sudo chown -R csye6225:csye6225 /opt/myapp

# # Set the permissions to 744
sudo chmod -R 777 /opt/myapp