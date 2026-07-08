import { useState } from "react";
import { LoginForm } from "./components/LoginForm";
import { RegisterForm } from "./components/RegisterForm";
import { Dashboard } from "./components/Dashboard";
import "./index.css";

type View = "login" | "register" | "dashboard";

export function App() {
  const [view, setView] = useState<View>("login");
  const [loggedInUser, setLoggedInUser] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const handleLoginSuccess = (username: string) => {
    setLoggedInUser(username);
    setSuccessMsg(null);
    setView("dashboard");
  };

  const handleRegisterSuccess = () => {
    setSuccessMsg("✅ Account created! Please verify your email, then log in.");
    setView("login");
  };

  const handleLogout = () => {
    setLoggedInUser(null);
    setView("login");
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1 className="app-title">🎬 Metube</h1>
        <p className="app-subtitle">Auth Flow Test — Mock API</p>

        {/* Tab Navigation */}
        {view !== "dashboard" && (
          <nav className="tab-nav">
            <button
              id="tab-login"
              className={`tab-btn ${view === "login" ? "active" : ""}`}
              onClick={() => { setView("login"); setSuccessMsg(null); }}
            >
              Sign In
            </button>
            <button
              id="tab-register"
              className={`tab-btn ${view === "register" ? "active" : ""}`}
              onClick={() => { setView("register"); setSuccessMsg(null); }}
            >
              Register
            </button>
          </nav>
        )}
      </header>

      <main className="app-main">
        {successMsg && (
          <div id="success-msg" className="success-banner">
            {successMsg}
          </div>
        )}

        {view === "login" && (
          <LoginForm onSuccess={handleLoginSuccess} />
        )}

        {view === "register" && (
          <RegisterForm onSuccess={handleRegisterSuccess} />
        )}

        {view === "dashboard" && loggedInUser && (
          <Dashboard username={loggedInUser} onLogout={handleLogout} />
        )}
      </main>

      {/* Mock API reference */}
      <footer className="app-footer">
        <details>
          <summary>📡 Mock API Endpoints</summary>
          <table className="api-table">
            <thead>
              <tr>
                <th>Method</th>
                <th>Path</th>
                <th>Auth</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><code>GET</code></td>
                <td><code>/api/auth/pow/challenge</code></td>
                <td>—</td>
                <td>Issue POW challenge</td>
              </tr>
              <tr>
                <td><code>POST</code></td>
                <td><code>/api/auth/login</code></td>
                <td>CSRF</td>
                <td>Login (POW optional)</td>
              </tr>
              <tr>
                <td><code>POST</code></td>
                <td><code>/api/auth/register</code></td>
                <td>CSRF</td>
                <td>Register (POW required)</td>
              </tr>
              <tr>
                <td><code>POST</code></td>
                <td><code>/api/auth/logout</code></td>
                <td>JWT + CSRF</td>
                <td>Logout &amp; blacklist token</td>
              </tr>
              <tr>
                <td><code>GET</code></td>
                <td><code>/api/auth/me</code></td>
                <td>JWT</td>
                <td>Current user profile</td>
              </tr>
            </tbody>
          </table>

          <div className="seed-users">
            <strong>Seeded test users:</strong>
            <table className="api-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Password</th>
                  <th>State</th>
                </tr>
              </thead>
              <tbody>
                <tr><td>admin</td><td>password123</td><td>✅ Active</td></tr>
                <tr><td>disabled_user</td><td>password123</td><td>🚫 Disabled</td></tr>
                <tr><td>unverified_user</td><td>password123</td><td>📧 Email unverified</td></tr>
              </tbody>
            </table>
          </div>
        </details>
      </footer>
    </div>
  );
}

export default App;
