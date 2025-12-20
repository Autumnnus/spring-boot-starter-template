# Authentication System Setup Guide

This document provides detailed information about the authentication system implementation including OAuth2 (Google) integration and email service configuration.

## Table of Contents

- [Features](#features)
- [OAuth2 Setup (Google)](#oauth2-setup-google)
- [Email Service Setup](#email-service-setup)
- [Authentication Endpoints](#authentication-endpoints)
- [Email Templates](#email-templates)
- [Environment Variables](#environment-variables)
- [Testing](#testing)

## Features

### ✅ Implemented Features

1. **Traditional Authentication**
   - User registration with email verification
   - Login with JWT token (access + refresh)
   - Logout with token revocation
   - Password reset flow
   - Password change (authenticated users)
   - Account lockout after failed attempts

2. **OAuth2 Integration**
   - Google OAuth2 login
   - Automatic user creation/linking
   - Profile picture sync from OAuth2 provider
   - Email auto-verification for OAuth2 users

3. **Email Service**
   - Email verification emails
   - Password reset emails
   - Welcome emails (after verification)
   - Password changed notification emails
   - HTML email templates with beautiful design
   - Async email sending

4. **Security Features**
   - JWT-based authentication
   - Refresh token rotation
   - Device tracking
   - IP address logging
   - Rate limiting
   - Account lockout mechanism

## OAuth2 Setup (Google)

### Step 1: Create Google OAuth2 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**
5. Configure OAuth consent screen if not already done
6. Application type: **Web application**
7. Add authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google` (Development)
   - `https://yourdomain.com/login/oauth2/code/google` (Production)
8. Copy the **Client ID** and **Client Secret**

### Step 2: Configure Environment Variables

Add to your `.env` file:

```env
# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:3000/auth/callback
OAUTH2_FAILURE_REDIRECT_URL=http://localhost:3000/auth/error
```

### Step 3: Frontend Integration

After successful OAuth2 login, the user will be redirected to the success URL with tokens as query parameters:

```
http://localhost:3000/auth/callback?accessToken=xxx&refreshToken=yyy
```

Your frontend should:
1. Extract tokens from URL
2. Store them securely (e.g., httpOnly cookies or secure storage)
3. Remove tokens from URL
4. Redirect to dashboard

### OAuth2 Flow

```
User clicks "Login with Google"
    ↓
Frontend redirects to: /oauth2/authorization/google
    ↓
User authenticates on Google
    ↓
Google redirects back with authorization code
    ↓
Backend exchanges code for user info
    ↓
CustomOAuth2UserService processes user
    ↓
OAuth2AuthenticationSuccessHandler generates JWT tokens
    ↓
User redirected to frontend with tokens
```

## Email Service Setup

### Gmail SMTP Setup

#### Step 1: Enable 2-Factor Authentication

1. Go to your [Google Account](https://myaccount.google.com/)
2. Navigate to **Security**
3. Enable **2-Step Verification**

#### Step 2: Generate App Password

1. In Google Account → **Security**
2. Under "Signing in to Google", select **App passwords**
3. Select app: **Mail**
4. Select device: **Other** (Custom name: "Spring Boot App")
5. Click **Generate**
6. Copy the 16-character password

#### Step 3: Configure Environment Variables

Add to your `.env` file:

```env
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
EMAIL_FROM=noreply@yourdomain.com
EMAIL_FROM_NAME=Your App Name
APP_BASE_URL=http://localhost:8080
```

### Alternative Email Providers

#### AWS SES

```env
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=your-smtp-username
MAIL_PASSWORD=your-smtp-password
```

#### SendGrid

```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
```

## Authentication Endpoints

### Traditional Authentication

#### Register
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response:** User object + verification email sent

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "deviceInfo": "Chrome on Windows",
  "ipAddress": "192.168.1.1"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "550e8400...",
  "accessTokenExpiresAt": "2024-01-01T12:00:00Z",
  "refreshTokenExpiresAt": "2024-01-08T12:00:00Z",
  "tokenType": "Bearer"
}
```

#### Verify Email
```http
GET /api/v1/auth/verify-email?token=550e8400-e29b-41d4-a716-446655440000
```

**Response:** 200 OK + welcome email sent

#### Request Password Reset
```http
POST /api/v1/auth/request-password-reset
Content-Type: application/json

{
  "email": "user@example.com"
}
```

**Response:** 200 OK + reset email sent

#### Reset Password
```http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "NewSecurePass123!"
}
```

**Response:** 200 OK + password changed email sent

#### Change Password (Authenticated)
```http
POST /api/v1/auth/change-password
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "oldPassword": "CurrentPass123!",
  "newPassword": "NewSecurePass123!"
}
```

**Response:** 200 OK + password changed email sent

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:** New access token + new refresh token (rotation)

#### Logout
```http
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:** 200 OK + token revoked

### OAuth2 Authentication

#### Initiate Google Login
```
GET /oauth2/authorization/google
```

Browser redirects to Google login, then back to your app with tokens.

## Email Templates

The system includes 4 beautifully designed HTML email templates:

### 1. Email Verification (`verification.html`)
- Sent after user registration
- Contains verification link (valid for 24 hours)
- Purple gradient theme

### 2. Password Reset (`password-reset.html`)
- Sent when user requests password reset
- Contains reset link (valid for 1 hour)
- Pink/red gradient theme
- Security warnings included

### 3. Welcome Email (`welcome.html`)
- Sent after email verification
- Getting started guide
- Security tips
- Blue gradient theme

### 4. Password Changed (`password-changed.html`)
- Sent after password change/reset
- Account security notice
- Contact support if unauthorized
- Green gradient theme

All templates are responsive and include:
- Professional design
- Clear call-to-action buttons
- Security notices
- Fallback plain-text links

## Environment Variables

Complete list of required environment variables:

```env
# Database
POSTGRES_DB=postgres
POSTGRES_USER=spring-boot-starter-user
POSTGRES_PASSWORD=spring-boot-starter-password
POSTGRES_PORT=5432
POSTGRES_URI=jdbc:postgresql://postgres:5432

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-min-32-chars-long

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
RABBITMQ_PORT=5672

# OAuth2 (Google)
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_SUCCESS_REDIRECT_URL=http://localhost:3000/auth/callback
OAUTH2_FAILURE_REDIRECT_URL=http://localhost:3000/auth/error

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
EMAIL_FROM=noreply@example.com
EMAIL_FROM_NAME=Spring Boot Starter
APP_BASE_URL=http://localhost:8080
```

## Testing

### Test Email Service

Create a test endpoint or use existing registration flow:

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "username": "testuser",
    "password": "Test123!"
  }'
```

Check your email inbox for the verification email.

### Test OAuth2 Login

1. Start the application
2. Navigate to: `http://localhost:8080/oauth2/authorization/google`
3. Login with your Google account
4. You should be redirected to your frontend with tokens

### Test Password Reset

```bash
# Request password reset
curl -X POST http://localhost:8080/api/v1/auth/request-password-reset \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'
```

Check your email for the reset link.

## Database Schema Updates

The authentication system added the following fields to the `users` table:

- `oauth2_provider` - VARCHAR (GOOGLE, FACEBOOK, GITHUB, LOCAL)
- `oauth2_provider_id` - VARCHAR (unique ID from OAuth2 provider)
- `oauth2_profile_picture_url` - VARCHAR(1024) (profile picture URL)

## Security Considerations

1. **Email Security**
   - Use app passwords, not main account password
   - Enable 2FA on email account
   - Use environment variables for credentials

2. **OAuth2 Security**
   - Keep client secret secure
   - Use HTTPS in production
   - Validate redirect URIs

3. **Token Security**
   - Access tokens expire in 1 day (configurable)
   - Refresh tokens expire in 7 days (configurable)
   - Tokens are revoked on logout and password change
   - Refresh token rotation prevents reuse

4. **Password Security**
   - BCrypt password hashing
   - Minimum password requirements (implement on frontend)
   - Account lockout after 5 failed attempts
   - All user tokens revoked on password change

## Troubleshooting

### Email not sending

1. Check SMTP credentials in `.env`
2. Verify app password (not regular password for Gmail)
3. Check application logs for errors
4. Test SMTP connection manually
5. Ensure port 587 is not blocked by firewall

### OAuth2 not working

1. Verify redirect URIs in Google Console match exactly
2. Check client ID and secret in `.env`
3. Ensure OAuth consent screen is configured
4. Check application logs for errors

### Tokens not working

1. Verify JWT_SECRET is at least 32 characters
2. Check token expiration times
3. Ensure clock sync between services
4. Verify token format in Authorization header: `Bearer {token}`

## Production Deployment

Before deploying to production:

1. ✅ Use HTTPS for all endpoints
2. ✅ Update OAuth2 redirect URIs to production domain
3. ✅ Use production email service (not Gmail personal)
4. ✅ Set strong JWT_SECRET (64+ characters)
5. ✅ Configure proper CORS settings
6. ✅ Enable rate limiting
7. ✅ Set up monitoring and alerts
8. ✅ Use environment-specific configurations
9. ✅ Secure environment variables (use secrets manager)
10. ✅ Test all authentication flows in staging

## Support

For issues or questions:
- Check application logs
- Review this documentation
- Check Spring Boot documentation
- Review OAuth2 provider documentation

---

**Note:** This authentication system is production-ready but should be reviewed and tested thoroughly for your specific use case.
