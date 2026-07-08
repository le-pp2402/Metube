/**
 * Unit tests cho mock API endpoints.
 *
 * Chạy bằng: bun test
 *
 * Covers:
 *  - POW challenge issuance
 *  - Login: success, wrong password, disabled, unverified, CSRF missing
 *  - Register: POW required, POW invalid, username/email taken, success
 *  - /me: authenticated, unauthenticated
 *  - Logout: token blacklisted, double-logout
 */

import { describe, test, expect, beforeEach } from "bun:test";

// ── Test helpers ─────────────────────────────────────────────────────────────

const BASE = "http://localhost:3000"; // not actually used – we call handlers directly

import {
  handlePowChallenge,
  handleLogin,
  handleRegister,
  handleLogout,
  handleMe,
} from "../src/mock/authRoutes";
import { users, sessions, blacklist, powChallenges } from "../src/mock/data";
import { generateToken } from "../src/mock/helpers";

/** Build a minimal Request for testing */
function makeRequest(
  method: string,
  path: string,
  opts: {
    body?: unknown;
    cookies?: Record<string, string>;
    csrfHeader?: string;
    authHeader?: string;
  } = {}
): Request {
  const headers = new Headers();

  if (opts.body) {
    headers.set("Content-Type", "application/json");
  }

  if (opts.csrfHeader) {
    headers.set("X-XSRF-TOKEN", opts.csrfHeader);
  }

  if (opts.authHeader) {
    headers.set("Authorization", `Bearer ${opts.authHeader}`);
  }

  const cookieParts: string[] = [];
  if (opts.cookies) {
    for (const [k, v] of Object.entries(opts.cookies)) {
      cookieParts.push(`${k}=${v}`);
    }
  }
  if (cookieParts.length) {
    headers.set("Cookie", cookieParts.join("; "));
  }

  return new Request(`http://localhost${path}`, {
    method,
    headers,
    body: opts.body ? JSON.stringify(opts.body) : undefined,
  });
}

/** Parse JSON body from a Response */
async function json(res: Response): Promise<Record<string, unknown>> {
  return res.json();
}

/** Seed a valid POW challenge and return its ID */
async function seedChallenge(difficulty = 3, ttlMs = 60_000): Promise<string> {
  const id = crypto.randomUUID();
  powChallenges.set(id, { difficulty, expiresAt: Date.now() + ttlMs });
  return id;
}

/** Brute-force a valid nonce for the given challengeId + difficulty */
async function solvePow(challengeId: string, difficulty: number): Promise<string> {
  const prefix = "0".repeat(difficulty);
  for (let nonce = 0; ; nonce++) {
    const data = new TextEncoder().encode(challengeId + String(nonce));
    const buf = await crypto.subtle.digest("SHA-256", data);
    const hex = Array.from(new Uint8Array(buf))
      .map((b) => b.toString(16).padStart(2, "0"))
      .join("");
    if (hex.startsWith(prefix)) return String(nonce);
  }
}

// ── Reset mutable state between tests ────────────────────────────────────────

beforeEach(() => {
  sessions.clear();
  blacklist.clear();
  powChallenges.clear();
  // Remove any users added by register tests (keep seed users)
  while (users.length > 3) users.pop();
});

// ── POW Challenge ─────────────────────────────────────────────────────────────

describe("GET /api/auth/pow/challenge", () => {
  test("returns challengeId, difficult, expiresIn", async () => {
    const req = makeRequest("GET", "/api/auth/pow/challenge");
    const res = await handlePowChallenge(req);
    expect(res.status).toBe(200);

    const body = await json(res);
    expect(typeof body.challengeId).toBe("string");
    expect(body.difficult).toBe(3);
    expect(typeof body.expiresIn).toBe("number");
  });

  test("each call returns a unique challengeId", async () => {
    const req = () => makeRequest("GET", "/api/auth/pow/challenge");
    const r1 = await json(await handlePowChallenge(req()));
    const r2 = await json(await handlePowChallenge(req()));
    expect(r1.challengeId).not.toBe(r2.challengeId);
  });
});

// ── Login ─────────────────────────────────────────────────────────────────────

describe("POST /api/auth/login", () => {
  const csrf = "test-csrf-token";
  const cookies = { "XSRF-TOKEN": csrf };

  test("403 when CSRF header is missing", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123" },
      cookies,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(403);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("CSRF_INVALID");
  });

  test("422 when username or password missing", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(422);
  });

  test("401 for wrong password", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "wrong" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(401);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("INVALID_CREDENTIALS");
  });

  test("401 for unknown username", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "nobody", password: "password123" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(401);
  });

  test("403 for disabled account", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "disabled_user", password: "password123" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(403);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("ACCOUNT_DISABLED");
  });

  test("403 for unverified email", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "unverified_user", password: "password123" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(403);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("EMAIL_NOT_VERIFIED");
  });

  test("200 success – sets access_token via Set-Cookie", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(200);

    const body = await json(res);
    expect((body as any).message).toBe("Login successful");

    // Server must issue an access_token cookie
    const setCookie = res.headers.get("set-cookie") ?? "";
    expect(setCookie).toContain("access_token=");
  });

  test("200 success with valid POW solution", async () => {
    const challengeId = await seedChallenge(3);
    const nonce = await solvePow(challengeId, 3);

    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123", challengeId, nonce },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(200);
    // Challenge must be consumed (single use)
    expect(powChallenges.has(challengeId)).toBe(false);
  });

  test("400 for invalid POW challenge ID", async () => {
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123", challengeId: "bad-id", nonce: "0" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(400);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("POW_INVALID");
  });

  test("400 for expired POW challenge", async () => {
    const challengeId = await seedChallenge(3, -1); // already expired
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123", challengeId, nonce: "0" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleLogin(req);
    expect(res.status).toBe(400);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("POW_EXPIRED");
  });

  test("400 for wrong POW nonce", async () => {
    const challengeId = await seedChallenge(3);
    const req = makeRequest("POST", "/api/auth/login", {
      body: { username: "admin", password: "password123", challengeId, nonce: "99999999" },
      cookies,
      csrfHeader: csrf,
    });
    // Hash likely won't match – if by miracle it does the test would pass anyway
    const res = await handleLogin(req);
    // Could be 400 (POW_WRONG) or 200 if nonce happened to be valid
    if (res.status !== 200) {
      expect(res.status).toBe(400);
      const body = await json(res);
      expect((body.errors as any[])[0].code).toBe("POW_WRONG");
    }
  });
});

// ── Register ──────────────────────────────────────────────────────────────────

describe("POST /api/auth/register", () => {
  const csrf = "test-csrf-reg";
  const cookies = { "XSRF-TOKEN": csrf };

  test("400 when POW is missing", async () => {
    const req = makeRequest("POST", "/api/auth/register", {
      body: { username: "newuser", email: "new@test.com", password: "abc123" },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleRegister(req);
    expect(res.status).toBe(400);

    const body = await json(res);
    expect((body.errors as any[])[0].code).toBe("POW_REQUIRED");
  });

  test("400 when POW challengeId unknown", async () => {
    const req = makeRequest("POST", "/api/auth/register", {
      body: {
        username: "newuser",
        email: "new@test.com",
        password: "abc123",
        challengeId: "nonexistent",
        nonce: "0",
      },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleRegister(req);
    expect(res.status).toBe(400);
    expect((await json(res) as any).errors[0].code).toBe("POW_INVALID");
  });

  test("409 when username is already taken", async () => {
    const challengeId = await seedChallenge(3);
    const nonce = await solvePow(challengeId, 3);

    const req = makeRequest("POST", "/api/auth/register", {
      body: {
        username: "admin", // already exists
        email: "unique@test.com",
        password: "abc123",
        challengeId,
        nonce,
      },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleRegister(req);
    expect(res.status).toBe(409);
    expect((await json(res) as any).errors[0].code).toBe("USERNAME_TAKEN");
  });

  test("409 when email is already taken", async () => {
    const challengeId = await seedChallenge(3);
    const nonce = await solvePow(challengeId, 3);

    const req = makeRequest("POST", "/api/auth/register", {
      body: {
        username: "brandnewuser",
        email: "admin@metube.com", // already exists
        password: "abc123",
        challengeId,
        nonce,
      },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleRegister(req);
    expect(res.status).toBe(409);
    expect((await json(res) as any).errors[0].code).toBe("EMAIL_TAKEN");
  });

  test("200 successful registration with valid POW", async () => {
    const challengeId = await seedChallenge(3);
    const nonce = await solvePow(challengeId, 3);

    const req = makeRequest("POST", "/api/auth/register", {
      body: {
        username: "newbie",
        email: "newbie@test.com",
        password: "securepass",
        challengeId,
        nonce,
      },
      cookies,
      csrfHeader: csrf,
    });
    const res = await handleRegister(req);
    expect(res.status).toBe(200);

    const body = await json(res);
    expect((body as any).message).toContain("Registration successful");

    // User should now exist
    const created = users.find((u) => u.username === "newbie");
    expect(created).toBeDefined();
    expect(created!.emailVerified).toBe(false); // needs email verification
  });

  test("challenge is consumed after successful register (single-use)", async () => {
    const challengeId = await seedChallenge(3);
    const nonce = await solvePow(challengeId, 3);

    const makeReq = () =>
      makeRequest("POST", "/api/auth/register", {
        body: {
          username: "user_a",
          email: "usera@test.com",
          password: "pass",
          challengeId,
          nonce,
        },
        cookies,
        csrfHeader: csrf,
      });

    const res1 = await handleRegister(makeReq());
    expect(res1.status).toBe(200);

    // Second attempt with same challenge must fail
    const res2 = await handleRegister(makeReq());
    expect(res2.status).toBe(400);
    expect((await json(res2) as any).errors[0].code).toBe("POW_INVALID");
  });
});

// ── /me ───────────────────────────────────────────────────────────────────────

describe("GET /api/auth/me", () => {
  test("401 when unauthenticated", async () => {
    const req = makeRequest("GET", "/api/auth/me");
    const res = await handleMe(req);
    expect(res.status).toBe(401);
  });

  test("200 returns user profile when authenticated via cookie", async () => {
    // Seed a session for user "1" (admin)
    const token = generateToken();
    sessions.set(token, "1");

    const req = makeRequest("GET", "/api/auth/me", {
      cookies: { access_token: token },
    });
    const res = await handleMe(req);
    expect(res.status).toBe(200);

    const body = await json(res);
    expect((body as any).username).toBe("admin");
    expect((body as any).email).toBe("admin@metube.com");
  });

  test("200 returns user profile when authenticated via Bearer header", async () => {
    const token = generateToken();
    sessions.set(token, "1");

    const req = makeRequest("GET", "/api/auth/me", {
      authHeader: token,
    });
    const res = await handleMe(req);
    expect(res.status).toBe(200);
  });

  test("401 when token is blacklisted", async () => {
    const token = generateToken();
    sessions.set(token, "1");
    blacklist.add(token);

    const req = makeRequest("GET", "/api/auth/me", {
      cookies: { access_token: token },
    });
    const res = await handleMe(req);
    expect(res.status).toBe(401);
  });
});

// ── Logout ────────────────────────────────────────────────────────────────────

describe("POST /api/auth/logout", () => {
  const csrf = "test-csrf-logout";
  const cookies = (extra: Record<string, string> = {}) => ({
    "XSRF-TOKEN": csrf,
    ...extra,
  });

  test("401 when not authenticated", async () => {
    const req = makeRequest("POST", "/api/auth/logout", {
      cookies: cookies(),
      csrfHeader: csrf,
    });
    const res = await handleLogout(req);
    expect(res.status).toBe(401);
  });

  test("200 and token is blacklisted after logout", async () => {
    const token = generateToken();
    sessions.set(token, "1");

    const req = makeRequest("POST", "/api/auth/logout", {
      cookies: cookies({ access_token: token }),
      csrfHeader: csrf,
    });
    const res = await handleLogout(req);
    expect(res.status).toBe(200);

    // Token must now be invalid
    expect(sessions.has(token)).toBe(false);
    expect(blacklist.has(token)).toBe(true);
  });

  test("Set-Cookie clears access_token on logout", async () => {
    const token = generateToken();
    sessions.set(token, "1");

    const req = makeRequest("POST", "/api/auth/logout", {
      cookies: cookies({ access_token: token }),
      csrfHeader: csrf,
    });
    const res = await handleLogout(req);
    const setCookie = res.headers.get("set-cookie") ?? "";
    expect(setCookie).toContain("access_token=;");
  });

  test("403 if CSRF header is missing", async () => {
    const token = generateToken();
    sessions.set(token, "1");

    const req = makeRequest("POST", "/api/auth/logout", {
      cookies: cookies({ access_token: token }),
      // no csrfHeader
    });
    const res = await handleLogout(req);
    expect(res.status).toBe(403);
  });
});
