// src/routes/PrivateRoute.jsx

import { Navigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

export default function PrivateRoute({ children, roles = [] }) {
    const { usuario } = useContext(AuthContext);

    if (!usuario) return <Navigate to="/" replace />;

    const tipo = String(usuario?.tipoCategoria || "").toUpperCase();

    if (roles.length > 0) {
        const autorizado = roles.some((r) => tipo.includes(r));

        if (!autorizado) {
            return <Navigate to="/painel" replace />;
        }
    }

    return children;
}