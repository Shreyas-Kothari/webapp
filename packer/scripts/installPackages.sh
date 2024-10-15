#!/bin/bash
set -e

echo "==> Installing packages all the required packages ==>"

## Install Java
sudo apt install openjdk-17-jdk -y
echo "Java installed!"

sleep 10

## Install Maven
# sudo apt install maven -y
# echo "Maven installed!"

sleep 10

# Install MySQL Server
echo "Installing MySQL server..."
sudo apt install mysql-server -y

sleep 10

# Start and enable MySQL service to start automatically on reboot
echo "Starting MySQL service..."
sudo systemctl start mysql.service
sudo systemctl enable mysql.service

# Set MySQL root password
echo "Setting MySQL root password and securing installation"
sudo mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;
EOF

echo "Setting up database and user..."
sudo mysql -u root -proot <<EOF
CREATE DATABASE ${DB_NAME};
CREATE USER '${DB_USERNAME}'@'localhost' IDENTIFIED BY "${DB_PASSWORD}";
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO "${DB_USERNAME}"@'localhost';
FLUSH PRIVILEGES;
EOF

echo "MySQL installed and configured successfully!"

# add system variables. used for startup
echo "export SPRING_DATASOURCE_DB=${DB_NAME}" | sudo tee -a /etc/environment
echo "export SPRING_DATASOURCE_URL=${DB_URL}" | sudo tee -a /etc/environment
echo "export SPRING_DATASOURCE_USERNAME=${DB_USERNAME}" | sudo tee -a /etc/environment
echo "export SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}" | sudo tee -a /etc/environment
source /etc/environment

# sudo mkdir -p /opt/myapp