import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

const STORAGE_KEY = "onlyfeed-auth";

function readStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuthState] = useState(() => readStorage());

  const setAuth = (nextAuth) => {
    setAuthState(nextAuth);
    if (!nextAuth) {
      localStorage.removeItem(STORAGE_KEY);
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
  };

  const logout = () => setAuth(null);

  const value = useMemo(
    () => ({
      auth,
      token: auth?.token || null,
      userId: auth?.userId || null,
      username: auth?.username || null,
      isAuthenticated: Boolean(auth?.token),
      setAuth,
      logout,
    }),
    [auth],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return ctx;
}
