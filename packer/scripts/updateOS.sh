#!/bin/bash
set -e

# set the environment variables
export DEBIAN_FRONTEND=noninteractive
export CHECKPOINT_DISABLE=1

echo "==> Updating the ubuntu system ==>"

# update the ubuntu
sudo apt-get update

sudo apt-get upgrade -y

echo "==> Cleaning up the ubuntu system ==>"
# cleanup pkg
sudo apt-get clean
echo "==> Ubuntu system cleaned up successfully ==>"
sleep 5
