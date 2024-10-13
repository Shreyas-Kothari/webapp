#!/bin/bash

set -e

PATH=${ARTIFACT_PATH}

echo "==> Setting up the application ==>"

sudo cp "$PATH" /opt/myapp/app.jar

sudo mv app.service /etc/systemd/system/app.service

sudo systemctl daemon-reload
sudo systemctl enable myapp
sudo systemctl start myapp