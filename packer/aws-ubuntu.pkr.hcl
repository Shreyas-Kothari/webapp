packer {
  required_plugins {
    amazon = {
      version = " >= 1.0.0, < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "shreyas-ubuntu" {
  ami_name        = "Shreyas Ubuntu ${formatdate("YYYY_MM_DD", timestamp())}"
  ami_description = "AMI for CSYE 6225 A04 created at ${formatdate("YYYY/MM/DD HH:mm", timestamp())} by Packer"
  instance_type   = "${var.instance_type}"
  region          = "${var.aws_region}"
  source_ami      = "${var.source_ami}"
  ssh_username    = "${var.ssh_username}"
  subnet_id       = "${var.subnet_id}"

  # Only sharing with the demo account
  ami_users = var.ami_users

  aws_polling {
    delay_seconds = 120
    max_attempts  = 50
  }

  tags = {
    Name = "shreyas-ubuntu-A04"
  }

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/sda1"
    volume_size           = 8
    volume_type           = "gp2"
  }
}

build {
  sources = [
    "source.amazon-ebs.shreyas-ubuntu"
  ]


  provisioner "shell" {
    script = "scripts/updateOS.sh"
  }

  provisioner "shell" {
    environment_vars = [
      "DB_URL=${var.DB_URL}",
      "DB_NAME=${var.DB_NAME}",
      "DB_USERNAME=${var.DB_USERNAME}",
      "DB_PASSWORD=${var.DB_PASSWORD}",
    ]
    script = "scripts/installPackages.sh"
  }
}