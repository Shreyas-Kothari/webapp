#!/bin/bash

set -e

sudo chmod 755 /temp/app.service
sudo mv /temp/app.service /etc/systemd/system/myapp.service

sudo systemctl daemon-reload
sudo systemctl enable myapp
sudo systemctl start myapp