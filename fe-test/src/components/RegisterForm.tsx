import { useState, type FormEvent } from "react";
import { apiFetch } from "../lib/api";
import { usePow } from "../hooks/usePow";
import { PowWidget } from "./PowWidget";

interface RegisterFormProps {
  onSuccess: () => void;
}

export function RegisterForm({ onSuccess }: RegisterFormProps) {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const powApi = usePow("/api/auth/pow/challenge");
  const [powResult, setPowResult] = useState<{ challengeId: string; nonce: string } | null>(null);

  const handlePowSolved = (challengeId: string, nonce: string) => {
    setPowResult({ challengeId, nonce });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setFieldErrors({});
    setLoading(true);

    if (!powResult) {
      setErrorMsg("Please complete the CAPTCHA verification first.");
      setLoading(false);
      return;
    }

    try {
      const res = await apiFetch("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          username,
          email,
          password,
          challengeId: powResult.challengeId,
          nonce: powResult.nonce,
        }),
      });

      const data = await res.json();

      if (!res.ok) {
        const err = data?.errors?.[0];
        const source = err?.source?.pointer as string | undefined;

        // Map JSON:API source pointer to form field
        if (source === "/data/attributes/username") {
          setFieldErrors({ username: err.title });
        } else if (source === "/data/attributes/email") {
          setFieldErrors({ email: err.title });
        } else {
          setErrorMsg(err?.detail ?? err?.title ?? "Registration failed");
        }

        // POW was consumed even on failure; reset so user can get a new one
        setPowResult(null);
        powApi.reset();
        return;
      }

      onSuccess();
    } catch (e) {
      setErrorMsg(`Network error: ${e}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form
      id="register-form"
      className="auth-form"
      onSubmit={handleSubmit}
      noValidate
    >
      <h2 className="auth-form-title">Create Account</h2>

      {errorMsg && (
        <div id="register-error" className="form-error" role="alert">
          {errorMsg}
        </div>
      )}

      <div className="form-group">
        <label htmlFor="reg-username">Username</label>
        <input
          id="reg-username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="myusername"
          autoComplete="username"
          required
        />
        {fieldErrors.username && (
          <span id="reg-username-error" className="field-error">
            {fieldErrors.username}
          </span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="reg-email">Email</label>
        <input
          id="reg-email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="me@example.com"
          autoComplete="email"
          required
        />
        {fieldErrors.email && (
          <span id="reg-email-error" className="field-error">
            {fieldErrors.email}
          </span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="reg-password">Password</label>
        <input
          id="reg-password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          autoComplete="new-password"
          minLength={6}
          required
        />
      </div>

      {/* POW Captcha – required for registration */}
      <PowWidget onSolved={handlePowSolved} powApi={powApi} />

      <button
        id="btn-register"
        type="submit"
        className="submit-btn"
        disabled={loading || !powResult}
      >
        {loading ? "Creating account…" : "Create Account"}
      </button>
    </form>
  );
}
