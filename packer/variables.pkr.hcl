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
  default = "t2.small"
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

variable DB_URL {
  type    = string
  default = "jdbc:mysql://localhost:3306/clouddemo_csye_6225"
}

variable DB_NAME {
  type    = string
  default = "clouddemo_csye_6225"
}

variable DB_USERNAME {
  type    = string
  default = "admin"
}

variable DB_PASSWORD {
  type    = string
  default = "admin"
}

variable ARTIFACT_PATH {
  type    = string
  default = "./CloudDemo_CSYE_6225/target/*.jar"
}
