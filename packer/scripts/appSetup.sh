#!/bin/bash

set -e

PATH=${ARTIFACT_PATH}

# Create the directory
sudo mkdir -p /opt/myapp

echo "==> Setting up the application ==>"

echo "Copyfile from $PATH to /opt/myapp/app.jar"

sudo cp "$PATH" /opt/myapp/app.jar

sudo mv app.service /etc/systemd/system/app.service

sudo systemctl daemon-reload
sudo systemctl enable myapp
sudo systemctl start myapp