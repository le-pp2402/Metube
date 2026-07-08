/**
 * Mock API helpers – build JSON:API-style error responses and set cookies
 * to simulate the real Spring Security / JWT backend.
 */

import { sessions, blacklist } from "./data";

// ── Cookie names (must match BE) ─────────────────────────────────────────────
export const ACCESS_TOKEN_COOKIE = "access_token";   // HttpOnly in real BE
export const CSRF_COOKIE = "XSRF-TOKEN";             // readable by JS

// ── Simple token generator ───────────────────────────────────────────────────
export function generateToken(): string {
  return crypto.randomUUID().replace(/-/g, "");
}

// ── Session helpers ──────────────────────────────────────────────────────────
export function createSession(userId: string): string {
  const token = generateToken();
  sessions.set(token, userId);
  return token;
}

export function resolveSession(req: Request): string | null {
  // 1. Try HttpOnly cookie "access_token"
  const cookieHeader = req.headers.get("cookie") ?? "";
  const match = cookieHeader.match(/(?:^|;\s*)access_token=([^;]+)/);
  if (match) {
    const token = match[1];
    if (sessions.has(token) && !blacklist.has(token)) return token;
  }

  // 2. Fallback: Authorization: Bearer <token>
  const auth = req.headers.get("authorization") ?? "";
  if (auth.startsWith("Bearer ")) {
    const token = auth.slice(7).trim();
    if (sessions.has(token) && !blacklist.has(token)) return token;
  }

  return null;
}

// ── Cookie builders ──────────────────────────────────────────────────────────
export function buildAccessTokenCookie(token: string): string {
  // In a real backend this would be HttpOnly, but mock keeps it accessible
  // so the test UI can display it.
  return `${ACCESS_TOKEN_COOKIE}=${token}; Path=/; SameSite=Strict; Max-Age=900`;
}

export function buildCsrfCookie(token: string): string {
  return `${CSRF_COOKIE}=${token}; Path=/; SameSite=Strict; Max-Age=900`;
}

export function clearAccessTokenCookie(): string {
  return `${ACCESS_TOKEN_COOKIE}=; Path=/; Max-Age=0`;
}

export function clearCsrfCookie(): string {
  return `${CSRF_COOKIE}=; Path=/; Max-Age=0`;
}

// ── CSRF validation ──────────────────────────────────────────────────────────
export function validateCsrf(req: Request): boolean {
  const cookieHeader = req.headers.get("cookie") ?? "";
  const csrfCookieMatch = cookieHeader.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  const csrfHeader =
    req.headers.get("x-xsrf-token") ?? req.headers.get("csrf-token") ?? "";

  if (!csrfCookieMatch) return false;
  return csrfCookieMatch[1] === csrfHeader;
}

// ── Response helpers ─────────────────────────────────────────────────────────
type JsonApiErrorOpts = {
  status: number;
  code: string;
  title: string;
  detail?: string;
  source?: string;
};

export function jsonError(opts: JsonApiErrorOpts): Response {
  const body = {
    errors: [
      {
        status: String(opts.status),
        code: opts.code,
        title: opts.title,
        detail: opts.detail ?? opts.title,
        source: opts.source ? { pointer: opts.source } : undefined,
      },
    ],
  };
  return new Response(JSON.stringify(body), {
    status: opts.status,
    headers: { "Content-Type": "application/json" },
  });
}

export function jsonOk(data: unknown, extraHeaders?: Record<string, string>): Response {
  return new Response(JSON.stringify(data), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
      ...(extraHeaders ?? {}),
    },
  });
}

export function requiresCsrf(req: Request): Response | null {
  if (!validateCsrf(req)) {
    return jsonError({
      status: 403,
      code: "CSRF_INVALID",
      title: "CSRF token mismatch",
      detail: "X-XSRF-TOKEN header is missing or does not match the XSRF-TOKEN cookie",
    });
  }
  return null;
}
