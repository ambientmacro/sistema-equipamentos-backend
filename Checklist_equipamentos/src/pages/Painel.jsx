// src/pages/Painel.jsx

import { useContext, useEffect, useMemo, useState } from "react";
import { AuthContext } from "../context/AuthContext";
import { api } from "../services/api";

import PainelHeader from "../components/painel/PainelHeader";
import PainelFeedback from "../components/painel/PainelFeedback";

import "../Styles/operacoes.css";

const fmtDate = (v = new Date()) =>
  new Intl.DateTimeFormat("pt-BR", { dateStyle: "short" }).format(new Date(v));

export default function Painel() {
  const { usuario } = useContext(AuthContext);

  const [equipamentos, setEquipamentos] = useState(() => {
    const cache = localStorage.getItem("equipamentos_cache");
    return cache ? JSON.parse(cache) : [];
  });

  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState("");
  const [ok, setOk] = useState("");

  const tipo = String(usuario?.tipoCategoria || "")
    .trim()
    .toUpperCase();

  const isAdminOrDev =
    tipo === "ADMIN" || tipo === "DEVELOPER";

  // ✅ pega última data salva
  function getLastDate() {
    const cache = JSON.parse(localStorage.getItem("equipamentos_cache") || "[]");
    if (!cache.length) return null;

    const max = cache.reduce((acc, item) => {
      const d = new Date(item.dataCriacao || item.createdAt || 0);
      return d > acc ? d : acc;
    }, new Date(0));

    return max.toISOString();
  }

  // ✅ merge inteligente
  function mergeEquipamentos(oldList, newList) {
    const map = new Map();

    [...oldList, ...newList].forEach((item) => {
      map.set(item.id, item);
    });

    return Array.from(map.values());
  }

  // ✅ LOAD COM SYNC INCREMENTAL
  useEffect(() => {
    async function load() {
      try {
        setLoading(true);

        const lastDate = getLastDate();

        // ✅ aqui está o segredo
        const url = lastDate
          ? `/estoques?criadoDepois=${lastDate}`
          : `/estoques`;

        const data = await api.get(url);

        console.log("DELTA API:", data);

        const listaNova = Array.isArray(data) ? data : [];

        const merged = mergeEquipamentos(equipamentos, listaNova);

        // ✅ salva cache
        localStorage.setItem(
          "equipamentos_cache",
          JSON.stringify(merged)
        );

        setEquipamentos(merged);

        setErro("");
      } catch (e) {
        setErro(e?.message || JSON.stringify(e));
      } finally {
        setLoading(false);
      }
    }

    load();

    // ✅ auto sync leve
    const interval = setInterval(load, 10000); // a cada 10s

    return () => clearInterval(interval);
  }, []);

  // ✅ filtro igual produção
  const equipamentosFiltrados = useMemo(() => {
    return equipamentos.filter(
      (e) =>
        e?.equipeResponsavel?.id === usuario?.equipeId
    );
  }, [equipamentos, usuario]);

  return (
    <div className="operacoes-page">
      <PainelHeader
        isAdminOrDev={isAdminOrDev}
        totalEquipes={equipamentosFiltrados.length}
        hoje={fmtDate(new Date())}
      />

      <PainelFeedback
        loading={loading}
        erro={erro}
        ok={ok}
      />

      {!loading && !erro && (
        <div className="painel-grid">
          {equipamentosFiltrados.length === 0 ? (
            <div className="operacoes-feedback">
              Nenhum equipamento encontrado
            </div>
          ) : (
            equipamentosFiltrados.map((eq) => (
              <div key={eq.id} className="card">
                <strong>{eq.nomeEquipamento}</strong>

                <p>
                  TAG: {eq.tagPatrimonio || "Sem tag"}
                </p>

                <p>
                  Equipe: {eq?.equipeResponsavel?.nome || "-"}
                </p>

                <p>
                  Empresa: {eq?.empresa?.nome || "-"}
                </p>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}