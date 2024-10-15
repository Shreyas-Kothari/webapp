#!/bin/bash
set -e

# set the environment variables
export DEBIAN_FRONTEND=noninteractive
export CHECKPOINT_DISABLE=1

echo "==> Updating the ubuntu system ==>"

# update the ubuntu
sudo apt-get update

sleep 5

sudo apt-get upgrade -y

sleep 5

echo "==> Cleaning up the ubuntu system ==>"
# cleanup pkg
sudo apt-get clean
echo "==> Ubuntu system cleaned up successfully ==>"
sleep 5
