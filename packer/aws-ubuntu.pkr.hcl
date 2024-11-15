packer {
  required_plugins {
    amazon = {
      version = " >= 1.0.0, < 2.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

source "amazon-ebs" "shreyas-ubuntu" {
  ami_name        = "Shreyas Ubuntu ${formatdate("YYYY_MM_DD-HH_mm", timestamp())}"
  ami_description = "AMI for CSYE 6225 A06 created at ${formatdate("YYYY/MM/DD HH:mm", timestamp())} by Packer"
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
    Name = "Shreyas Ubuntu A06-${var.environment} ${formatdate("YYYY/MM/DD HH:mm", timestamp())}"
  }

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = var.block_device_name
    volume_size           = var.volume_size
    volume_type           = var.volume_type
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
    script = "scripts/installPackages.sh"
  }

  provisioner "file" {
    source      = "../CloudDemo_CSYE_6225/target/${var.ARTIFACT_NAME}.jar"
    destination = "/tmp/app.jar"
  }

  provisioner "file" {
    sources     = ["./cloudwatch-config.json"]
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "scripts/appDirSetup.sh"
  }

  provisioner "file" {
    sources     = ["./scripts/app.service"]
    destination = "/tmp/"
  }

  provisioner "shell" {
    script = "scripts/cloudWatch.sh"
  }

  provisioner "shell" {
    script = "scripts/appSetup.sh"
  }

  post-processor "manifest" {
    output = "manifest.json"
  }
}