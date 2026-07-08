// In-memory mock data store

export interface MockUser {
  id: string;
  username: string;
  email: string;
  passwordHash: string;
  enabled: boolean;
  emailVerified: boolean;
}

// Dummy users seeded at startup
export const users: MockUser[] = [
  {
    id: "1",
    username: "admin",
    email: "admin@metube.com",
    passwordHash: "hashed:password123", // fake hash – compare plain text in mock
    enabled: true,
    emailVerified: true,
  },
  {
    id: "2",
    username: "disabled_user",
    email: "disabled@metube.com",
    passwordHash: "hashed:password123",
    enabled: false,
    emailVerified: true,
  },
  {
    id: "3",
    username: "unverified_user",
    email: "unverified@metube.com",
    passwordHash: "hashed:password123",
    enabled: true,
    emailVerified: false,
  },
];

export function findUserByUsername(username: string): MockUser | undefined {
  return users.find((u) => u.username === username);
}

export function findUserByEmail(email: string): MockUser | undefined {
  return users.find((u) => u.email === email);
}

export function checkPassword(user: MockUser, plain: string): boolean {
  // Mock: stored hash is "hashed:<plain>" so just compare
  return user.passwordHash === `hashed:${plain}`;
}

// Active sessions: token → userId
export const sessions = new Map<string, string>();

// Blacklisted tokens (logout)
export const blacklist = new Set<string>();

// POW challenges: challengeId → { difficulty, expiresAt }
export interface PowEntry {
  difficulty: number;
  expiresAt: number; // unix ms
}
export const powChallenges = new Map<string, PowEntry>();
