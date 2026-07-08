import { useState, type FormEvent } from "react";
import { apiFetch } from "../lib/api";
import { usePow } from "../hooks/usePow";
import { PowWidget } from "./PowWidget";

interface LoginFormProps {
  onSuccess: (username: string) => void;
}

export function LoginForm({ onSuccess }: LoginFormProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [csrfReady, setCsrfReady] = useState(false);

  // POW hook
  const powApi = usePow("/api/auth/pow/challenge");
  const [powResult, setPowResult] = useState<{ challengeId: string; nonce: string } | null>(null);

  const handlePowSolved = (challengeId: string, nonce: string) => {
    setPowResult({ challengeId, nonce });
  };

  // Initialise CSRF cookie by hitting a GET endpoint first
  const ensureCsrf = async () => {
    if (!csrfReady) {
      await apiFetch("/api/auth/pow/challenge"); // triggers Set-Cookie: XSRF-TOKEN
      setCsrfReady(true);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setLoading(true);

    await ensureCsrf();

    try {
      const body: Record<string, string> = { username, password };
      if (powResult) {
        body.challengeId = powResult.challengeId;
        body.nonce = powResult.nonce;
      }

      const res = await apiFetch("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(body),
      });

      const data = await res.json();

      if (!res.ok) {
        const err = data?.errors?.[0];
        setErrorMsg(err?.detail ?? err?.title ?? "Login failed");
        return;
      }

      // On success the server sets access_token + XSRF-TOKEN cookies
      // via Set-Cookie (handled by browser). The X-CSRF-SET header is a
      // hint from our mock to apply the csrf cookie.
      onSuccess(username);
    } catch (e) {
      setErrorMsg(`Network error: ${e}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form
      id="login-form"
      className="auth-form"
      onSubmit={handleSubmit}
      noValidate
    >
      <h2 className="auth-form-title">Sign In</h2>

      {errorMsg && (
        <div id="login-error" className="form-error" role="alert">
          {errorMsg}
        </div>
      )}

      <div className="form-group">
        <label htmlFor="login-username">Username</label>
        <input
          id="login-username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="admin"
          autoComplete="username"
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="login-password">Password</label>
        <input
          id="login-password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="password123"
          autoComplete="current-password"
          required
        />
      </div>

      {/* POW Captcha (optional for login, required for register) */}
      <PowWidget onSolved={handlePowSolved} powApi={powApi} />

      <button
        id="btn-login"
        type="submit"
        className="submit-btn"
        disabled={loading}
      >
        {loading ? "Signing in…" : "Sign In"}
      </button>

      <div className="form-hint">
        <strong>Test credentials:</strong> admin / password123
      </div>
    </form>
  );
}
