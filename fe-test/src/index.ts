import { serve } from "bun";
import index from "./index.html";
import {
  handlePowChallenge,
  handleLogin,
  handleRegister,
  handleLogout,
  handleMe,
} from "./mock/authRoutes";
import { generateToken, buildCsrfCookie, clearCsrfCookie } from "./mock/helpers";

/**
 * Bun.serve does not support multiple Set-Cookie headers natively in a
 * Response object, so we post-process responses that need to set both the
 * access_token and XSRF-TOKEN cookies.
 */
function appendCsrfCookie(res: Response, csrfToken: string): Response {
  const headers = new Headers(res.headers);
  headers.append("Set-Cookie", buildCsrfCookie(csrfToken));
  return new Response(res.body, {
    status: res.status,
    headers,
  });
}

function appendClearCsrfCookie(res: Response): Response {
  const headers = new Headers(res.headers);
  headers.append("Set-Cookie", clearCsrfCookie());
  return new Response(res.body, {
    status: res.status,
    headers,
  });
}

/**
 * Wrap an auth handler to:
 *  1. Run the handler.
 *  2. If the response contains X-CSRF-SET hint → append XSRF-TOKEN Set-Cookie.
 *  3. If the response contains X-CSRF-CLEAR hint → append cookie clear.
 */
async function withCookieHandling(
  handler: (req: Request) => Promise<Response>,
  req: Request
): Promise<Response> {
  let res = await handler(req);

  const csrfSet = res.headers.get("x-csrf-set");
  const csrfClear = res.headers.get("x-csrf-clear");

  if (csrfSet) {
    res = appendCsrfCookie(res, csrfSet);
  } else if (csrfClear) {
    res = appendClearCsrfCookie(res);
  }

  return res;
}

/**
 * On any GET that returns 200, issue a fresh XSRF-TOKEN cookie if the request
 * doesn't already have one.  This mirrors Spring Security's behaviour of
 * emitting the CSRF cookie on the first request.
 */
function injectCsrfCookieIfMissing(req: Request, res: Response): Response {
  if (res.status !== 200) return res;

  const cookieHeader = req.headers.get("cookie") ?? "";
  if (cookieHeader.includes("XSRF-TOKEN=")) return res;

  const token = generateToken();
  const headers = new Headers(res.headers);
  headers.append("Set-Cookie", buildCsrfCookie(token));
  return new Response(res.body, { status: res.status, headers });
}

const server = serve({
  routes: {
    // Serve React app for everything that isn't an API route
    "/*": index,

    // ── Auth API ────────────────────────────────────────────────────────────
    "/api/auth/pow/challenge": {
      async GET(req) {
        const res = await handlePowChallenge(req);
        return injectCsrfCookieIfMissing(req, res);
      },
    },

    "/api/auth/login": {
      async POST(req) {
        return withCookieHandling(handleLogin, req);
      },
    },

    "/api/auth/register": {
      async POST(req) {
        return withCookieHandling(handleRegister, req);
      },
    },

    "/api/auth/logout": {
      async POST(req) {
        return withCookieHandling(handleLogout, req);
      },
    },

    "/api/auth/me": {
      async GET(req) {
        return handleMe(req);
      },
    },
  },

  development: process.env.NODE_ENV !== "production" && {
    hmr: true,
    console: true,
  },
});

console.log(`🚀 Mock server running at ${server.url}`);
console.log(`   📡 Auth API  → ${server.url}api/auth/`);
console.log(`   🔑 POW CAPTCHA difficulty = 3 leading-zero hex digits`);
console.log(`   🍪 Sets: access_token (session) + XSRF-TOKEN (CSRF) cookies`);
