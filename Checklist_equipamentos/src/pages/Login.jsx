import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../services/api";
import { AuthContext } from "../context/AuthContext";
import bg from "../assets/Capa_global.png";

const fieldStyle = {
  width: "100%",
  height: "40px",
  padding: "0 14px",
  borderRadius: "9px",
  border: "1px solid rgba(109, 126, 166, 0.45)",
  background: "#22365f",
  color: "#f5f7ff",
  fontSize: "15px",
  outline: "none",
};

const SHOW_REGISTER = true;
const IS_ADMIN = true;

export default function Login() {
  const navigate = useNavigate();
  const { setUsuario } = useContext(AuthContext);

  const [username, setUsername] = useState("");
  const [senha, setSenha] = useState("");

  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [nome, setNome] = useState("");

  const [loading, setLoading] = useState(false);
  const [isRegisterMode, setIsRegisterMode] = useState(false);

  function normalizeUsername(value) {
    return value.toUpperCase();
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);

    try {
      // ================= CADASTRO =================
      if (IS_ADMIN && isRegisterMode) {
        if (!username || !senha || !confirmarSenha || !nome) {
          alert("Preencha todos os campos");
          return;
        }

        if (senha !== confirmarSenha) {
          alert("Senhas não conferem");
          return;
        }

        await api.post("/usuarios", {
          username: normalizeUsername(username.trim()),
          senha,
          nome,
          tipoCadastroId: 1,
          equipeId: 1
        });

        alert("✅ Usuário criado com sucesso!");

        setUsername("");
        setSenha("");
        setConfirmarSenha("");
        setNome("");

        setIsRegisterMode(false);
        return;
      }

      // ================= LOGIN =================
      const response = await api.post("/login", {
        username: normalizeUsername(username.trim()),
        senha
      });

      setUsuario(response);
      navigate("/painel", { replace: true });

    } catch (error) {
      console.error(error);
      alert(error.message || "Erro no processo");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "24px",
        backgroundImage: `linear-gradient(rgba(7, 16, 36, 0.62), rgba(7, 16, 36, 0.78)), url(${bg})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      <form
        onSubmit={handleSubmit}
        style={{
          width: "100%",
          maxWidth: "340px",
          padding: "20px",
          borderRadius: "18px",
          backdropFilter: "blur(8px)",
          background:
            "linear-gradient(180deg, rgba(6, 30, 86, 0.8), rgba(10, 28, 68, 0.6))",
          border: "1px solid rgba(29, 84, 211, 0.28)",
          boxShadow: "0 20px 44px rgba(95, 99, 126, 0.34)",
        }}
      >
        <h1
          style={{
            margin: "0 0 22px",
            color: "#f3f6ff",
            fontSize: "34px",
            fontWeight: "800",
            textAlign: "center",
            lineHeight: "1.05",
          }}
        >
          Controle de Fluxo
          <br />
          Equipamentos Pequenos
        </h1>

        <div style={{ display: "grid", gap: "14px" }}>

          {/* USERNAME */}
          <input
            placeholder={isRegisterMode ? "Novo Username" : "Username"}
            value={username}
            onChange={(e) => setUsername(normalizeUsername(e.target.value))}
            style={{ ...fieldStyle, textTransform: "uppercase" }}
          />

          {/* SENHA */}
          <input
            type="password"
            placeholder="Senha"
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            style={fieldStyle}
          />

          {/* CAMPOS DE CADASTRO */}
          {IS_ADMIN && isRegisterMode && (
            <>
              <input
                type="password"
                placeholder="Confirmar senha"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                style={fieldStyle}
              />

              <input
                placeholder="Nome"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                style={fieldStyle}
              />
            </>
          )}

          {/* BOTÃO */}
          <button
            type="submit"
            disabled={loading}
            style={{
              height: "40px",
              borderRadius: "9px",
              border: "1px solid rgba(94, 161, 255, 0.75)",
              background: "linear-gradient(180deg, #2f62f3, #2a4fc7)",
              color: "#ffffff",
              fontSize: "16px",
              fontWeight: "700",
              cursor: loading ? "wait" : "pointer",
              opacity: loading ? 0.75 : 1,
            }}
          >
            {loading
              ? "Processando..."
              : isRegisterMode
                ? "Cadastrar"
                : "Entrar"}
          </button>
        </div>

        {/* BOTÃO DEV */}
        {IS_ADMIN && SHOW_REGISTER && (
          <div style={{ marginTop: "12px", textAlign: "center" }}>
            <button
              type="button"
              onClick={() => setIsRegisterMode(!isRegisterMode)}
              style={{
                background: "transparent",
                border: "none",
                color: "#aaa",
                fontSize: "12px",
                cursor: "pointer",
              }}
            >
              {isRegisterMode
                ? "← Voltar para login"
                : "Modo desenvolvedor"}
            </button>
          </div>
        )}

      </form>
    </div>
  );
}