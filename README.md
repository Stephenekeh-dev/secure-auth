## SecureAuth: Robust Authentication & Authorization System ##
# API Endpoints #
POST /api/auth/register
GET /api/auth/verify-email?token=...
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
POST /api/auth/forgot-password
POST /api/auth/reset-password
GET /api/auth/mfa/setup (requires auth)
POST /api/auth/mfa/verify (requires auth)
GET /api/users/me (ROLE_USER)
GET /api/admin/users (ROLE_ADMIN)