#!/bin/bash

set -e

# Create the directory
sudo mkdir -p "/opt/myapp"

# Add a system user with no login shell and add it to the group csye6225
sudo groupadd csye6225
sudo useradd --system -s /usr/sbin/nologin -g csye6225 csye6225

# # Set ownership to the new user
sudo chown -R csye6225:csye6225 /opt/myapp

## Set the permissions
sudo chmod -R 755 /opt/myapp

# move the application files to the new directory from /tmp
sudo mv /tmp/app.jar /opt/myapp/app.jar

# Create the necessary directories for CloudWatch Agent
sudo mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/

# Move the CloudWatch configuration file to the appropriate directory
sudo mv /tmp/cloudwatch-config.json /opt/aws/amazon-cloudwatch-agent/etc/cloudwatch-config.json