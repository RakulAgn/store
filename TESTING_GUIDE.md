# Authentication Testing Guide

Complete step-by-step guide to test your authenticated API with Postman.

## Prerequisites

- Auth0 Account created
- Spring Boot application running: `./gradlew bootRun`
- Postman installed

---

## Part 1: Configure Auth0 SPA for Postman Testing

Since you created a **Single Page Application** in Auth0, we need to enable password grant for Postman testing.

### Step 1.1: Enable Password Grant (For Testing Only)

1. Go to **Auth0 Dashboard**: https://manage.auth0.com
2. Navigate to **Applications → Applications**
3. Find your **Single Page Application**
4. Go to **Settings** tab
5. Scroll down to **Advanced Settings**
6. Click on **Grant Types** tab
7. **Enable**: ✅ **Password** (for testing with Postman)
8. ✅ **Implicit** should already be checked (for SPAs)
9. ✅ **Authorization Code** should already be checked
10. ✅ **Refresh Token** (optional, for long sessions)
11. Click **Save Changes**

**Note:** Password grant is only for testing. In production, SPAs use Authorization Code with PKCE.

### Step 1.2: Get Your Credentials

From the same Settings page, copy:
- **Domain**: `dev-ckpiff0e.us.auth0.com`
- **Client ID**: `abc123...` (long string)
- **Client Secret**: Usually **empty** for SPAs (that's OK!)

**For SPA testing:** You don't need the Client Secret. We'll only use Client ID.

---

## Part 2: Create a Test User in Auth0

### Step 2.1: Create User Manually

1. In Auth0 Dashboard, go to **User Management → Users**
2. Click **Create User**
3. Fill in:
   - **Email**: `testuser@example.com`
   - **Password**: `Test123!@#` (strong password)
   - **Connection**: `Username-Password-Authentication`
4. Click **Create**

### Step 2.2: Verify User

- You should see the new user in the Users list
- Note the **user_id** (looks like `auth0|123456789`)

---

## Part 3: Test Authentication Flow with Postman

### Step 3.1: Login to Get Access Token (SPA Password Grant)

**Request:**
```
POST https://dev-ckpiff0e.us.auth0.com/oauth/token
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON) - FOR SPA (No Client Secret):**
```json
{
  "grant_type": "password",
  "username": "testuser@example.com",
  "password": "Test123!@#",
  "audience": "https://buggybot-api.com",
  "client_id": "YOUR_SPA_CLIENT_ID",
  "scope": "openid profile email"
}
```

**Note:** SPAs don't have a client_secret, so we **don't include it**!

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "scope": "openid profile email",
  "expires_in": 86400,
  "token_type": "Bearer"
}
```

**Copy the `access_token`** - this is your JWT token!

---

### Alternative: If Password Grant Doesn't Work

If you get an error like "grant_type not allowed", try this alternative method:

**Option A:** Use Auth0's Test Token
1. In Auth0 Dashboard, go to **Applications → APIs**
2. Click on your API (`Store API`)
3. Go to **Test** tab
4. You'll see a test token - copy it!
5. Use this token in Postman

**Option B:** Create a Machine-to-Machine App
1. Create a new **Machine to Machine** application
2. Authorize it for your API
3. Use client credentials grant (see original guide)

---

## Part 4: Test Your API Endpoints

### Step 4.1: Get Current User Profile

This automatically creates the user in your database on first call.

**Request:**
```
GET http://localhost:8080/api/user/me
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "auth0Id": "auth0|123456789",
    "email": "testuser@example.com",
    "name": "testuser@example.com",
    "createdAt": "2025-11-22T14:00:00.000Z",
    "updatedAt": "2025-11-22T14:00:00.000Z"
  }
}
```

**Check your database!** A new user should be created in the `users` table.

---

### Step 4.2: Update User Profile

**Request:**
```
PATCH http://localhost:8080/api/user/me
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json
```

**Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

### Step 4.3: Create a Store

Now the user can create stores!

**Request:**
```
POST http://localhost:8080/api/store
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json
```

**Body:**
```json
{
  "storeName": "My Awesome Store",
  "storeLocation": "New York, NY"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Store Created Successfully",
  "data": {
    "storeId": "123e4567-e89b-12d3-a456-426614174000",
    "storeName": "My Awesome Store",
    "storeLocation": "New York, NY",
    "storeCreatedAt": "2025-11-22T14:05:00.000Z",
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      ...
    }
  }
}
```

**Create multiple stores!** Try creating 3-5 stores to test pagination.

---

### Step 4.4: Get All Stores (User's Stores Only)

**Request:**
```
GET http://localhost:8080/api/store/all?page=1&size=10
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "data": [
      {
        "storeId": "...",
        "storeName": "My Awesome Store",
        "storeLocation": "New York, NY",
        ...
      }
    ],
    "page": 1,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

---

### Step 4.5: Get a Specific Store

**Request:**
```
GET http://localhost:8080/api/store/{storeId}
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

### Step 4.6: Update a Store

**Request:**
```
PATCH http://localhost:8080/api/store/{storeId}
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
Content-Type: application/json
```

**Body:**
```json
{
  "storeName": "Updated Store Name"
}
```

---

### Step 4.7: Delete a Store

**Request:**
```
DELETE http://localhost:8080/api/store/{storeId}
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

### Step 4.8: Get All Users (Admin)

**Request:**
```
GET http://localhost:8080/api/user/all
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Expected Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "userId": "...",
      "auth0Id": "auth0|123456789",
      "email": "testuser@example.com",
      "name": "John Doe",
      ...
    }
  ]
}
```

---

### Step 4.9: Delete User Account

**WARNING:** This deletes the user and ALL their stores!

**Request:**
```
DELETE http://localhost:8080/api/user/me
```

**Headers:**
```
Authorization: Bearer YOUR_ACCESS_TOKEN
```

---

## Part 5: Test Multi-User Scenario

### Create a Second User

1. In Auth0 Dashboard, create another user: `user2@example.com`
2. Login as user2 (Step 3.1) to get a different access token
3. Create stores with user2's token
4. List stores - you should only see user2's stores!

**This proves user isolation works!**

---

## Part 6: Test Security

### Test 6.1: No Token (Should Fail)

**Request:**
```
GET http://localhost:8080/api/store/all
```

**NO Authorization header**

**Expected:** `401 Unauthorized`

---

### Test 6.2: Invalid Token (Should Fail)

**Request:**
```
GET http://localhost:8080/api/store/all
```

**Headers:**
```
Authorization: Bearer invalid_token_here
```

**Expected:** `401 Unauthorized`

---

### Test 6.3: Expired Token (Should Fail)

Wait for your token to expire (24 hours) or modify the expiration time in Auth0.

**Expected:** `401 Unauthorized`

Get a new token by logging in again.

---

## Complete API Reference

### Public Endpoints (No Auth Required)
- `GET /api/auth/info` - Get authentication instructions

### User Endpoints (Auth Required)
- `GET /api/user/me` - Get current user profile
- `GET /api/user/all` - Get all users
- `PATCH /api/user/me` - Update current user
- `DELETE /api/user/me` - Delete current user

### Store Endpoints (Auth Required)
- `GET /api/store/all` - Get all stores (user's stores only)
- `GET /api/store/{id}` - Get specific store
- `POST /api/store` - Create store
- `PUT /api/store/{id}` - Replace store
- `PATCH /api/store/{id}` - Update store
- `DELETE /api/store/{id}` - Delete store

---

## Troubleshooting

### Problem: "Invalid grant_type"
**Solution:** Enable Password grant in Auth0 Application settings

### Problem: "Access denied"
**Solution:** Check that audience matches `https://buggybot-api.com`

### Problem: "Unauthorized"
**Solution:** Make sure your token is in the header as `Authorization: Bearer YOUR_TOKEN`

### Problem: "User email is null"
**Solution:** This is OK! Machine-to-machine tokens don't have email. Use password grant for user tokens.

### Problem: Token expired
**Solution:** Get a new token by logging in again (tokens expire after 24 hours)

---

## Summary

**Complete Flow:**
1. Create user in Auth0 → **Signup**
2. Login with email/password → **Get access token**
3. Call `/api/user/me` → **Sync user to database**
4. Create stores → **User-specific data**
5. All operations are isolated per user! 🎉

**You've learned:**
- ✅ JWT authentication with Auth0
- ✅ User signup and login
- ✅ Protected API endpoints
- ✅ User-specific data isolation
- ✅ CRUD operations with authentication
- ✅ Spring Security configuration
- ✅ OAuth2 Resource Server setup

Great job! 🚀
