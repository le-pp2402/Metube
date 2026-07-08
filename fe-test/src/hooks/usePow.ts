/**
 * POW (Proof-of-Work) solver hook.
 *
 * Fetches a challenge from the server then brute-forces a nonce such that:
 *   SHA-256(challengeId + nonce).startsWith("0".repeat(difficulty))
 *
 * Uses a Web Worker-style approach via setTimeout to avoid blocking the UI,
 * reporting progress back to the component.
 */

import { useState, useCallback, useRef } from "react";

export type PowStatus = "idle" | "fetching" | "solving" | "done" | "error";

export interface PowResult {
  challengeId: string;
  nonce: string;
}

export interface UsePowReturn {
  status: PowStatus;
  result: PowResult | null;
  progress: number; // nonce attempts so far
  error: string | null;
  solve: () => Promise<PowResult | null>;
  reset: () => void;
}

async function sha256Hex(input: string): Promise<string> {
  const data = new TextEncoder().encode(input);
  const buf = await crypto.subtle.digest("SHA-256", data);
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

export function usePow(challengeEndpoint = "/api/auth/pow/challenge"): UsePowReturn {
  const [status, setStatus] = useState<PowStatus>("idle");
  const [result, setResult] = useState<PowResult | null>(null);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const cancelRef = useRef(false);

  const reset = useCallback(() => {
    cancelRef.current = true; // signal any in-flight solve to stop
    setStatus("idle");
    setResult(null);
    setProgress(0);
    setError(null);
    // Allow new solve after reset
    setTimeout(() => { cancelRef.current = false; }, 0);
  }, []);

  const solve = useCallback(async (): Promise<PowResult | null> => {
    cancelRef.current = false;
    setStatus("fetching");
    setResult(null);
    setProgress(0);
    setError(null);

    // 1. Fetch challenge
    let challengeId: string;
    let difficulty: number;
    try {
      const res = await fetch(challengeEndpoint);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      challengeId = data.challengeId;
      difficulty = data.difficult;
    } catch (e) {
      setStatus("error");
      setError(`Failed to fetch challenge: ${e}`);
      return null;
    }

    // 2. Brute-force nonce
    setStatus("solving");
    const prefix = "0".repeat(difficulty);
    let nonce = 0;

    // Batch size to avoid blocking render
    const BATCH = 500;

    while (true) {
      if (cancelRef.current) {
        setStatus("idle");
        return null;
      }

      // Process a batch
      for (let i = 0; i < BATCH; i++) {
        const nonceStr = String(nonce);
        const hash = await sha256Hex(challengeId + nonceStr);
        nonce++;

        if (hash.startsWith(prefix)) {
          const found: PowResult = { challengeId, nonce: nonceStr };
          setResult(found);
          setProgress(nonce);
          setStatus("done");
          return found;
        }
      }

      setProgress(nonce);
      // Yield to browser
      await new Promise<void>((r) => setTimeout(r, 0));
    }
  }, [challengeEndpoint]);

  return { status, result, progress, error, solve, reset };
}
