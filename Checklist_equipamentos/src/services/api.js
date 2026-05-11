export const API_BASE_URL = import.meta.env.VITE_API_URL;

// ================= AUTH =================
function getAuthHeaders() {
  try {
    const usuario = JSON.parse(localStorage.getItem("usuario") || "{}");
    const headers = {};

    if (usuario?.id) headers["X-User-Id"] = String(usuario.id);
    if (usuario?.tipoCategoria) headers["X-User-Tipo"] = String(usuario.tipoCategoria);
    if (usuario?.username) headers["X-User-Username"] = String(usuario.username);

    return headers;
  } catch {
    return {};
  }
}

// ================= ERROR =================
function buildNetworkError(path) {
  return new Error(`Não foi possível conectar ao servidor. Endpoint: ${path}`);
}

// ================= CIRCUIT BREAKER =================
let failureCount = 0;
let lastFailureTime = 0;

const FAILURE_THRESHOLD = 3;
const COOLDOWN = 5000;

function isCircuitOpen() {
  return failureCount >= FAILURE_THRESHOLD &&
    Date.now() - lastFailureTime < COOLDOWN;
}

function registerFailure() {
  failureCount++;
  lastFailureTime = Date.now();
}

function resetCircuit() {
  failureCount = 0;
}

// ================= CORE REQUEST =================
async function request(path, options = {}, retry = 2) {
  if (isCircuitOpen()) {
    throw new Error("Sistema temporariamente indisponível. Tente novamente.");
  }

  let controller = new AbortController();
  let timeout = setTimeout(() => controller.abort(), 10000);

  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: options.method || "GET",
      headers: {
        "Content-Type": "application/json",
        ...getAuthHeaders(),
        ...(options.headers || {}),
      },
      body: options.body || undefined,
      signal: controller.signal,
    });

    clearTimeout(timeout);

    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
      ? await response.json()
      : await response.text();

    if (!response.ok) {
      registerFailure();

      throw new Error(
        data?.mensagem ||
        data?.message ||
        data ||
        "Erro na requisição"
      );
    }

    resetCircuit();
    return data;

  } catch (error) {
    clearTimeout(timeout);

    // retry automático
    if (retry > 0) {
      return request(path, options, retry - 1);
    }

    registerFailure();

    if (error.name === "AbortError") {
      throw new Error("Tempo de resposta excedido.");
    }

    throw error.message ? error : buildNetworkError(path);
  }
}

// ================= METHODS =================
function get(path) {
  return request(path, { method: "GET" });
}

function post(path, body) {
  return request(path, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

function put(path, body) {
  return request(path, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

function patch(path, body) {
  return request(path, {
    method: "PATCH",
    body: JSON.stringify(body),
  });
}

function remove(path) {
  return request(path, { method: "DELETE" });
}

// ✅ limpeza real pra AuthContext
function clearCache() {
  failureCount = 0;
  lastFailureTime = 0;
}

// ================= EXPORT =================
export const api = {
  get,
  post,
  put,
  patch,
  delete: remove,
  clearCache, // ✅ agora existe e funciona
};
