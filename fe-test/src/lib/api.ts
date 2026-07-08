/**
 * Cookie utility – reads XSRF-TOKEN from document.cookie
 * so we can send it as X-XSRF-TOKEN on mutating requests.
 */

export function getCsrfToken(): string {
  const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  return match ? decodeURIComponent(match[1]) : "";
}

/**
 * Thin fetch wrapper that:
 *  - Reads XSRF-TOKEN cookie and attaches it as X-XSRF-TOKEN header.
 *  - Sets credentials: "include" so cookies are sent cross-origin.
 *  - Defaults Content-Type to application/json.
 */
export async function apiFetch(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const headers = new Headers(options.headers ?? {});

  if (!headers.has("Content-Type") && options.body) {
    headers.set("Content-Type", "application/json");
  }

  const csrf = getCsrfToken();
  if (csrf) {
    headers.set("X-XSRF-TOKEN", csrf);
  }

  return fetch(url, {
    ...options,
    headers,
    credentials: "include",
  });
}
