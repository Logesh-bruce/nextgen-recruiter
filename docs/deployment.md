# HireFlow AI — AWS Free Tier Deployment Guide

This guide outlines the production deployment architecture for HireFlow AI targeted strictly at **AWS Free Tier** limits to prevent accidental cloud billing charges.

---

## 1. AWS Free Tier Infrastructure Components

| Component | AWS Resource | Free Tier Allowance | Details |
|---|---|---|---|
| **Compute** | EC2 `t2.micro` / `t3.micro` | 750 hrs/month (12 months) | Single instance running Docker + Nginx reverse proxy |
| **Database** | RDS PostgreSQL `db.t3.micro` | 750 hrs/month + 20GB storage | Multi-AZ disabled (Single-AZ for free tier) |
| **Container Registry** | ECR | 500 MB storage/month | Single repository with lifecycle policy purging old tags |
| **File Storage** | S3 | 5 GB standard storage | Bucket for candidate resume PDF/DOCX uploads |
| **Cache (Optional)** | Local Redis Container | 0 AWS cost | Redis container running directly on EC2 instance (avoids ElastiCache costs) |

---

## 2. Infrastructure Setup Instructions

### A. EC2 Instance Provisioning
1. Launch an `Amazon Linux 2023` `t2.micro` or `t3.micro` instance in `us-east-1`.
2. Configure Security Group Rules:
   - Inbound `HTTP` (Port 80) from `0.0.0.0/0`
   - Inbound `HTTPS` (Port 443) from `0.0.0.0/0`
   - Inbound `SSH` (Port 22) from your IP
3. SSH into instance and install Docker & Nginx:
   ```bash
   sudo dnf update -y
   sudo dnf install -y docker nginx
   sudo systemctl enable --now docker
   sudo usermod -aG docker ec2-user
   ```

### B. Environment Secrets Configuration
Create `/etc/hireflow/env.list` on the EC2 instance:
```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://<your-rds-endpoint>:5432/hireflow_db
SPRING_DATASOURCE_USERNAME=hireflow_admin
SPRING_DATASOURCE_PASSWORD=<secure-password>
HIREFLOW_JWT_PRIVATE_KEY=<base64-private-key>
HIREFLOW_JWT_PUBLIC_KEY=<base64-public-key>
HIREFLOW_AI_OPENAI_API_KEY=<openai-key>
```

### C. Nginx Reverse Proxy Setup
Copy `nginx.conf` to `/etc/nginx/conf.d/hireflow.conf` and restart Nginx:
```bash
sudo systemctl enable --now nginx
```

---

## 3. GitHub Actions CI/CD Secrets

Add the following Repository Secrets in GitHub (`Settings` -> `Secrets and variables` -> `Actions`):

- `AWS_ACCESS_KEY_ID`: IAM user access key with ECR access
- `AWS_SECRET_ACCESS_KEY`: IAM user secret key
- `EC2_HOST`: Elastic IP address of your EC2 instance
- `EC2_SSH_KEY`: Private SSH key (`.pem`) for `ec2-user`

---

## 4. Free Tier Monitoring Reminder

> [!WARNING]
> Always set up an **AWS Billing Alarm** in the AWS Billing Console for **$1.00 USD** threshold. Monitor the **AWS Billing → Free Tier Usage Dashboard** weekly to verify your active usage stays within limits.
