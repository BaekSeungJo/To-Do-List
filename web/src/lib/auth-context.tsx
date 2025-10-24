'use client';

import {
  onIdTokenChanged,
  signOut as firebaseSignOut,
  type User,
  type IdTokenResult
} from 'firebase/auth';
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type PropsWithChildren
} from 'react';
import { getFirebaseAuth } from './firebase';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  lastTokenResult: IdTokenResult | null;
  getIdToken: (forceRefresh?: boolean) => Promise<string | null>;
  signOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: PropsWithChildren): JSX.Element {
  const auth = useMemo(() => getFirebaseAuth(), []);
  const [user, setUser] = useState<User | null>(auth.currentUser);
  const [loading, setLoading] = useState<boolean>(true);
  const [lastTokenResult, setLastTokenResult] = useState<IdTokenResult | null>(null);
  const initialized = useRef(false);

  useEffect(() => {
    const unsubscribe = onIdTokenChanged(auth, async (firebaseUser) => {
      setUser(firebaseUser);
      if (firebaseUser) {
        try {
          const tokenResult = await firebaseUser.getIdTokenResult();
          setLastTokenResult(tokenResult);
        } catch (error) {
          console.error('Failed to resolve Firebase token result', error);
          setLastTokenResult(null);
        }
      } else {
        setLastTokenResult(null);
      }
      if (!initialized.current) {
        initialized.current = true;
        setLoading(false);
      }
    });

    return () => unsubscribe();
  }, [auth]);

  const getIdToken = useCallback(
    async (forceRefresh = false) => {
      if (!auth.currentUser) {
        return null;
      }
      try {
        const token = await auth.currentUser.getIdToken(forceRefresh);
        if (forceRefresh) {
          try {
            const tokenResult = await auth.currentUser.getIdTokenResult();
            setLastTokenResult(tokenResult);
          } catch (error) {
            console.error('Failed to refresh Firebase token result', error);
          }
        }
        return token;
      } catch (error) {
        console.error('Unable to retrieve Firebase ID token', error);
        return null;
      }
    },
    [auth]
  );

  const signOut = useCallback(async () => {
    try {
      await firebaseSignOut(auth);
    } catch (error) {
      console.error('Failed to sign out from Firebase', error);
      throw error;
    }
  }, [auth]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      lastTokenResult,
      getIdToken,
      signOut
    }),
    [getIdToken, lastTokenResult, loading, signOut, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
