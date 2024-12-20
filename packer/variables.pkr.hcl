variable aws_region {
  type    = string
  default = "us-east-1"
}

variable source_ami {
  type    = string
  default = "ami-0866a3c8686eaeeba"
}

variable instance_type {
  type    = string
  default = "t2.micro"
}

variable ssh_username {
  type    = string
  default = "ubuntu"
}

variable subnet_id {
  type    = string
  default = "subnet-013de0ed8cb2bb722"
}

variable ami_users {
  description = "List of AWS account IDs that can launch this AMI"
  type        = list(string)
  default     = ["762233742104"]
}

variable environment {
  type    = string
  default = ""
}

variable block_device_name {
  type    = string
  default = "/dev/sda1"
}

variable volume_size {
  type    = number
  default = 8
}

variable volume_type {
  type    = string
  default = "gp2"
}

variable ARTIFACT_NAME {
  type    = string
  default = "app"
}