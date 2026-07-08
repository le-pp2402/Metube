/**
 * Mock implementations for auth routes.
 *
 * Endpoints mirroring the expected real BE surface:
 *   GET  /api/auth/pow/challenge          – issue a POW challenge
 *   POST /api/auth/login                  – login with username/password + POW
 *   POST /api/auth/register               – register a new user + POW
 *   POST /api/auth/logout                 – invalidate session (requires auth)
 *   GET  /api/auth/me                     – return current user (requires auth)
 */

import {
  users,
  sessions,
  blacklist,
  powChallenges,
  findUserByUsername,
  findUserByEmail,
  checkPassword,
  type MockUser,
} from "./data";
import {
  generateToken,
  createSession,
  resolveSession,
  buildAccessTokenCookie,
  buildCsrfCookie,
  clearAccessTokenCookie,
  clearCsrfCookie,
  jsonOk,
  jsonError,
  requiresCsrf,
} from "./helpers";

// ── POW helpers ───────────────────────────────────────────────────────────────
const POW_DIFFICULTY = 3;          // number of leading zero hex digits required
const POW_TTL_MS = 5 * 60 * 1000; // 5 minutes

/**
 * Verify that SHA-256(challengeId + nonce) starts with POW_DIFFICULTY zeros.
 * This mirrors the real POWHashAlgo.SHA256 implementation on the BE.
 */
async function verifySolution(
  challengeId: string,
  nonce: string,
  difficulty: number
): Promise<boolean> {
  const data = new TextEncoder().encode(challengeId + nonce);
  const hashBuffer = await crypto.subtle.digest("SHA-256", data);
  const hashHex = Array.from(new Uint8Array(hashBuffer))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
  const prefix = "0".repeat(difficulty);
  return hashHex.startsWith(prefix);
}

// ── Route handlers ────────────────────────────────────────────────────────────

/** GET /api/auth/pow/challenge */
export async function handlePowChallenge(_req: Request): Promise<Response> {
  const challengeId = crypto.randomUUID();
  powChallenges.set(challengeId, {
    difficulty: POW_DIFFICULTY,
    expiresAt: Date.now() + POW_TTL_MS,
  });

  // Return shape mirrors PowChallenge record: challengeId, difficult, expiresIn
  return jsonOk({
    challengeId,
    difficult: POW_DIFFICULTY,
    expiresIn: POW_TTL_MS,
  });
}

/** POST /api/auth/login – body: { username, password, challengeId, nonce } */
export async function handleLogin(req: Request): Promise<Response> {
  // CSRF check
  const csrfError = requiresCsrf(req);
  if (csrfError) return csrfError;

  let body: Record<string, string>;
  try {
    body = await req.json();
  } catch {
    return jsonError({ status: 400, code: "BAD_REQUEST", title: "Invalid JSON body" });
  }

  const { username, password, challengeId, nonce } = body;

  // Validate required fields
  if (!username || !password) {
    return jsonError({
      status: 422,
      code: "VALIDATION_ERROR",
      title: "Missing required fields",
      detail: "username and password are required",
    });
  }

  // POW verification (required when present)
  if (challengeId && nonce) {
    const entry = powChallenges.get(challengeId);
    if (!entry) {
      return jsonError({
        status: 400,
        code: "POW_INVALID",
        title: "Invalid or expired challenge",
        detail: "challengeId not found or already used",
      });
    }
    if (Date.now() > entry.expiresAt) {
      powChallenges.delete(challengeId);
      return jsonError({
        status: 400,
        code: "POW_EXPIRED",
        title: "Challenge expired",
        detail: "The POW challenge has expired. Please request a new one.",
      });
    }
    const valid = await verifySolution(challengeId, nonce, entry.difficulty);
    if (!valid) {
      return jsonError({
        status: 400,
        code: "POW_WRONG",
        title: "Incorrect POW solution",
        detail: `SHA-256(challengeId + nonce) must start with ${"0".repeat(entry.difficulty)}`,
      });
    }
    // Consume challenge (single use)
    powChallenges.delete(challengeId);
  }

  // Credential check
  const user = findUserByUsername(username);
  if (!user || !checkPassword(user, password)) {
    return jsonError({
      status: 401,
      code: "INVALID_CREDENTIALS",
      title: "Invalid credentials",
      detail: "Username or password is incorrect",
    });
  }

  if (!user.enabled) {
    return jsonError({
      status: 403,
      code: "ACCOUNT_DISABLED",
      title: "Account is disabled",
      detail: "Your account has been disabled. Please contact support.",
    });
  }

  if (!user.emailVerified) {
    return jsonError({
      status: 403,
      code: "EMAIL_NOT_VERIFIED",
      title: "Email not verified",
      detail: "Please verify your email before logging in.",
    });
  }

  // Issue tokens
  const accessToken = createSession(user.id);
  const csrfToken = generateToken();

  return new Response(
    JSON.stringify({ message: "Login successful", userId: user.id }),
    {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Set-Cookie": buildAccessTokenCookie(accessToken),
        // Second Set-Cookie must be appended separately; Bun supports array
        // headers but for simplicity we append the csrf cookie too.
        // NOTE: In Bun.serve you can only set one Set-Cookie per Response
        // directly; we work around by appending in index.ts after the fact.
        "X-XSRF-TOKEN": csrfToken,
        "X-CSRF-SET": csrfToken, // hint to index.ts to append the csrf cookie
      },
    }
  );
}

/** POST /api/auth/register – body: { username, email, password, challengeId, nonce } */
export async function handleRegister(req: Request): Promise<Response> {
  const csrfError = requiresCsrf(req);
  if (csrfError) return csrfError;

  let body: Record<string, string>;
  try {
    body = await req.json();
  } catch {
    return jsonError({ status: 400, code: "BAD_REQUEST", title: "Invalid JSON body" });
  }

  const { username, email, password, challengeId, nonce } = body;

  if (!username || !email || !password) {
    return jsonError({
      status: 422,
      code: "VALIDATION_ERROR",
      title: "Missing required fields",
      detail: "username, email, and password are required",
    });
  }

  // POW verification (required for register)
  if (!challengeId || !nonce) {
    return jsonError({
      status: 400,
      code: "POW_REQUIRED",
      title: "POW challenge required",
      detail: "Register requires a valid POW solution",
    });
  }

  const entry = powChallenges.get(challengeId);
  if (!entry) {
    return jsonError({
      status: 400,
      code: "POW_INVALID",
      title: "Invalid or expired challenge",
    });
  }
  if (Date.now() > entry.expiresAt) {
    powChallenges.delete(challengeId);
    return jsonError({ status: 400, code: "POW_EXPIRED", title: "Challenge expired" });
  }

  const valid = await verifySolution(challengeId, nonce, entry.difficulty);
  if (!valid) {
    return jsonError({ status: 400, code: "POW_WRONG", title: "Incorrect POW solution" });
  }
  powChallenges.delete(challengeId);

  // Uniqueness checks
  if (findUserByUsername(username)) {
    return jsonError({
      status: 409,
      code: "USERNAME_TAKEN",
      title: "Username already taken",
      source: "/data/attributes/username",
    });
  }
  if (findUserByEmail(email)) {
    return jsonError({
      status: 409,
      code: "EMAIL_TAKEN",
      title: "Email already taken",
      source: "/data/attributes/email",
    });
  }

  // Create user (in-memory)
  const newUser: MockUser = {
    id: crypto.randomUUID(),
    username,
    email,
    passwordHash: `hashed:${password}`,
    enabled: true,
    emailVerified: false, // Needs email verification
  };
  users.push(newUser);

  return jsonOk({ message: "Registration successful. Please verify your email." });
}

/** POST /api/auth/logout – requires authentication */
export async function handleLogout(req: Request): Promise<Response> {
  const csrfError = requiresCsrf(req);
  if (csrfError) return csrfError;

  const token = resolveSession(req);
  if (!token) {
    return jsonError({ status: 401, code: "UNAUTHORIZED", title: "Not authenticated" });
  }

  blacklist.add(token);
  sessions.delete(token);

  return new Response(JSON.stringify({ message: "Logged out" }), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      "Set-Cookie": clearAccessTokenCookie(),
      "X-CSRF-CLEAR": "1",
    },
  });
}

/** GET /api/auth/me – requires authentication */
export async function handleMe(req: Request): Promise<Response> {
  const token = resolveSession(req);
  if (!token) {
    return jsonError({ status: 401, code: "UNAUTHORIZED", title: "Not authenticated" });
  }

  const userId = sessions.get(token)!;
  const user = users.find((u) => u.id === userId);
  if (!user) {
    return jsonError({ status: 401, code: "UNAUTHORIZED", title: "User not found" });
  }

  return jsonOk({ id: user.id, username: user.username, email: user.email });
}
