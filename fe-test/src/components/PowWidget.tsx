/**
 * PowWidget – Visual indicator for the Proof-of-Work captcha process.
 *
 * Shows three states:
 *   idle    – "Get Captcha" button
 *   solving – animated spinner + progress counter
 *   done    – green checkmark
 *   error   – red error message
 */

import { usePow, type UsePowReturn } from "../hooks/usePow";

interface PowWidgetProps {
  /** Called with the solved result so the parent form can use it */
  onSolved: (challengeId: string, nonce: string) => void;
  /** Reset pow when parent form resets */
  powApi: UsePowReturn;
}

export function PowWidget({ onSolved, powApi }: PowWidgetProps) {
  const { status, progress, error, solve } = powApi;

  const handleClick = async () => {
    const result = await solve();
    if (result) {
      onSolved(result.challengeId, result.nonce);
    }
  };

  return (
    <div className="pow-widget">
      <div className="pow-header">
        <span className="pow-label">🔐 Anti-bot verification (PoW CAPTCHA)</span>
      </div>

      <div className="pow-body">
        {status === "idle" && (
          <button
            type="button"
            id="btn-get-captcha"
            className="pow-btn pow-btn-start"
            onClick={handleClick}
          >
            Get Captcha &amp; Solve
          </button>
        )}

        {status === "fetching" && (
          <div className="pow-status">
            <span className="pow-spinner" />
            <span>Fetching challenge…</span>
          </div>
        )}

        {status === "solving" && (
          <div className="pow-status">
            <span className="pow-spinner" />
            <span>
              Solving… <strong>{progress.toLocaleString()}</strong> attempts
            </span>
          </div>
        )}

        {status === "done" && (
          <div className="pow-status pow-done">
            <span className="pow-check">✓</span>
            <span>Verified! ({progress.toLocaleString()} attempts)</span>
          </div>
        )}

        {status === "error" && (
          <div className="pow-status pow-error">
            <span>⚠ {error}</span>
            <button
              type="button"
              className="pow-btn pow-btn-retry"
              onClick={handleClick}
            >
              Retry
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
