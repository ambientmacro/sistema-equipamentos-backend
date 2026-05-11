// src/routes/AppRoutes.jsx

import { Navigate, Route, Routes } from "react-router-dom";
import { lazy, Suspense } from "react";
import PrivateRoute from "./PrivateRoute";

const Login = lazy(() => import("../pages/Login"));
const Painel = lazy(() => import("../pages/Painel"));
const Estoque = lazy(() => import("../pages/Estoque"));
const Oficina = lazy(() => import("../pages/Oficina"));
const Layout = lazy(() => import("../components/Layout"));

function Fallback() {
    return <div className="operacoes-feedback">Carregando...</div>;
}

export default function AppRoutes() {
    return (
        <Suspense fallback={<Fallback />}>
            <Routes>
                <Route path="/" element={<Login />} />

                <Route
                    path="/painel"
                    element={
                        <PrivateRoute>
                            <Layout />
                        </PrivateRoute>
                    }
                >
                    <Route index element={<Painel />} />

                    {/* ADMIN */}
                    <Route
                        path="estoque"
                        element={
                            <PrivateRoute roles={["ADMIN", "DEVELOPER"]}>
                                <Estoque />
                            </PrivateRoute>
                        }
                    />

                    <Route
                        path="oficina"
                        element={
                            <PrivateRoute roles={["ADMIN", "DEVELOPER"]}>
                                <Oficina />
                            </PrivateRoute>
                        }
                    />

                    {/* fallback */}
                    <Route path="*" element={<Navigate to="/painel" />} />
                </Route>
            </Routes>
        </Suspense>
    );
}