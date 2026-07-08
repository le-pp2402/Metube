/**
 * Dashboard shown after a successful login.
 * Lets the user see cookie state and test /api/auth/me + logout.
 */

import { useState, useEffect } from "react";
import { apiFetch } from "../lib/api";

interface DashboardProps {
  username: string;
  onLogout: () => void;
}

interface MeResponse {
  id: string;
  username: string;
  email: string;
}

export function Dashboard({ username, onLogout }: DashboardProps) {
  const [meData, setMeData] = useState<MeResponse | null>(null);
  const [meError, setMeError] = useState<string | null>(null);
  const [logoutError, setLogoutError] = useState<string | null>(null);
  const [cookieDisplay, setCookieDisplay] = useState("");

  const refreshCookies = () => {
    setCookieDisplay(document.cookie || "(no readable cookies)");
  };

  useEffect(() => {
    refreshCookies();
    fetchMe();
  }, []);

  const fetchMe = async () => {
    setMeError(null);
    try {
      const res = await apiFetch("/api/auth/me");
      const data = await res.json();
      if (!res.ok) {
        setMeError(data?.errors?.[0]?.title ?? "Failed to fetch profile");
      } else {
        setMeData(data);
      }
    } catch (e) {
      setMeError(`Network error: ${e}`);
    }
    refreshCookies();
  };

  const handleLogout = async () => {
    setLogoutError(null);
    try {
      const res = await apiFetch("/api/auth/logout", { method: "POST" });
      if (!res.ok) {
        const data = await res.json();
        setLogoutError(data?.errors?.[0]?.title ?? "Logout failed");
        return;
      }
      refreshCookies();
      onLogout();
    } catch (e) {
      setLogoutError(`Network error: ${e}`);
    }
  };

  return (
    <div id="dashboard" className="dashboard">
      <h2>👋 Welcome, <strong>{username}</strong>!</h2>
      <p className="dashboard-subtitle">Login successful — session is active.</p>

      {/* Cookie Inspector */}
      <section className="info-card">
        <h3>🍪 Browser Cookies</h3>
        <p className="hint">
          <code>access_token</code> is set by the server (would be HttpOnly in
          production — invisible to JS). <code>XSRF-TOKEN</code> is readable so
          the frontend can attach it as <code>X-XSRF-TOKEN</code> on mutations.
        </p>
        <pre id="cookie-display" className="code-block">{cookieDisplay}</pre>
        <button
          id="btn-refresh-cookies"
          type="button"
          className="secondary-btn"
          onClick={refreshCookies}
        >
          Refresh Cookie Display
        </button>
      </section>

      {/* /api/auth/me */}
      <section className="info-card">
        <h3>👤 GET /api/auth/me</h3>
        {meError && (
          <div id="me-error" className="form-error">
            {meError}
          </div>
        )}
        {meData && (
          <pre id="me-data" className="code-block">
            {JSON.stringify(meData, null, 2)}
          </pre>
        )}
        <button
          id="btn-fetch-me"
          type="button"
          className="secondary-btn"
          onClick={fetchMe}
        >
          Re-fetch /me
        </button>
      </section>

      {/* Logout */}
      <section className="info-card">
        <h3>🚪 Logout</h3>
        <p className="hint">
          Sends <code>POST /api/auth/logout</code> with <code>X-XSRF-TOKEN</code>{" "}
          header. The server blacklists the token and clears cookies.
        </p>
        {logoutError && (
          <div id="logout-error" className="form-error">
            {logoutError}
          </div>
        )}
        <button
          id="btn-logout"
          type="button"
          className="danger-btn"
          onClick={handleLogout}
        >
          Logout
        </button>
      </section>
    </div>
  );
}
