#!/bin/bash

set -e

sudo chmod 755 /tmp/app.service
sudo mv /tmp/app.service /etc/systemd/system/

sudo systemctl daemon-reload
sudo systemctl enable app
sudo systemctl restart app