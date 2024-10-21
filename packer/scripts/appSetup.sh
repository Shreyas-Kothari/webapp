#!/bin/bash

set -e

sudo mv /tmp/app.service /etc/systemd/system/app.service

sudo systemctl daemon-reload
sudo systemctl enable app
sudo systemctl restart app