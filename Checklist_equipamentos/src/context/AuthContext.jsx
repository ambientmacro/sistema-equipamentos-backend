// src/context/AuthContext.jsx

import { createContext, useState, useEffect } from "react";
import { api } from "../services/api";

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => {
    const usuarioSalvo = localStorage.getItem("usuario");
    return usuarioSalvo ? JSON.parse(usuarioSalvo) : null;
  });

  useEffect(() => {
    if (usuario) {
      localStorage.setItem("usuario", JSON.stringify(usuario));
      return;
    }

    localStorage.removeItem("usuario");
  }, [usuario]);

  function logout() {
    // ✅ SAFE CALL (não quebra se não existir)
    if (typeof api.clearCache === "function") {
      api.clearCache();
    }

    setUsuario(null);
    localStorage.removeItem("usuario");
  }

  return (
    <AuthContext.Provider value={{ usuario, setUsuario, logout }}>
      {children}
    </AuthContext.Provider>
  );
}