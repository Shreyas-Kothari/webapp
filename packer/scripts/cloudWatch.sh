#!/bin/bash
set -e

# Download and install Amazon CloudWatch Agent
curl -O https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
echo "==> Downloaded Amazon CloudWatch Agent! ==>"

sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
echo "==> Amazon CloudWatch Agent installed! ==>"

# Start and enable CloudWatch Agent with configuration
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/cloudwatch-config.json
echo "==> Amazon CloudWatch Agent configuration fetched and started! ==>"
